import { defineStore } from 'pinia'
import { getWebRtcConfig } from '../api/webrtc'

/**
 * WebRTC 语音通道（混合方案：语音直连 Asterisk WSS，与 CTI 控制并行）
 *
 * 基于 PoC 验证链路（2026-08-22）：
 *  - sip.js 0.16.1 核心 API（UserAgent + Registerer + Inviter）
 *  - 远端音频播放：直接监听 peerConnection.ontrack（delegate.onTrack 不可靠）
 *  - 必须 audio-only constraints（否则 SDP 带 video 导致 Asterisk 488）
 *  - DTLS-SRTP/ICE 由 Asterisk endpoint（webrtc=yes）保证，本端仅需注册与 INVITE
 *
 * 与 phoneStore（CTI 控制）关系：并行 —— 登入后本 store 自动注册语音；
 * 拨打/接听走 WebRTC 媒体，坐席状态/事件仍走 CTI。
 */
export const useWebrtcPhoneStore = defineStore('webrtcPhone', {
  state: () => ({
    enabled: false,        // 是否初始化（拿到分机配置后 true）
    ready: false,          // sip.js 已加载
    status: 'idle',        // idle | connecting | registered | calling | talking | error | unregistered
    statusText: '未连接',
    exten: '',
    displayName: '',
    server: '',
    wssUri: '',
    session: null,         // 当前 SIP 会话（Inviter/Invitation）
    errorMsg: '',
    audioEls: [],          // 远端音频元素（解锁播放用）
    // 外部事件回调（组件绑定）
    onIncoming: null,      // 来电 (number)
    onTalking: null,       // 通话中 (number)
    onHangup: null         // 通话结束
  }),

  getters: {
    registered: (s) => s.status === 'registered' || s.status === 'talking',
    talking: (s) => s.status === 'talking'
  },

  actions: {
    /**
     * 初始化并注册 WebRTC 语音（坐席登入后调用）
     * @param exten 分机号（与 CTI 登入一致）
     */
    async register(exten) {
      if (!window.SIP) {
        this.errorMsg = 'sip.js 未加载（检查 /vendor/sip.js）'
        this.status = 'error'
        return false
      }
      this.exten = exten
      this.status = 'connecting'
      this.statusText = '连接中...'
      try {
        const cfg = await getWebRtcConfig(exten)
        this.server = cfg.server
        this.wssUri = cfg.wssUri
        this.displayName = cfg.displayName || 'WebRTC-' + exten

        const SIP = window.SIP
        this._ua = new SIP.UserAgent({
          uri: SIP.UserAgent.makeURI('sip:' + exten + '@' + this.server),
          displayName: this.displayName,
          transportOptions: { server: this.wssUri },
          authorizationUsername: exten,
          authorizationPassword: cfg.password || '', // 分机密码由后端从 Asterisk 配置读取
          logLevel: 'warn'
        })
        this._ua.delegate = {
          onInvite: (invitation) => {
            this.session = invitation
            this._bindIncomingMedia(invitation)
            this.status = 'calling'
            this.statusText = '来电 ' + invitation.remoteIdentity.uri.user
            if (this.onIncoming) this.onIncoming(invitation.remoteIdentity.uri.user)
          },
          onRegistrationExpired: () => {
            this.status = 'unregistered'
            this.statusText = '注册过期'
          }
        }
        await this._ua.start()
        this._registerer = new SIP.Registerer(this._ua)
        await this._registerer.register()
        this.enabled = true
        this.status = 'registered'
        this.statusText = '语音已注册'
        return true
      } catch (e) {
        this.status = 'error'
        this.statusText = '语音注册失败'
        this.errorMsg = e && e.message ? e.message : String(e)
        return false
      }
    },

    /** 外呼 */
    async call(number) {
      if (!this._ua || !this.registered) {
        this.errorMsg = '语音未注册'
        return false
      }
      try {
        const SIP = window.SIP
        const targetURI = SIP.UserAgent.makeURI('sip:' + number + '@' + this.server)
        const inviter = new SIP.Inviter(this._ua, targetURI, {
          sessionDescriptionHandlerOptions: { constraints: { audio: true, video: false } },
          delegate: { onTrack: (track) => this._playRemoteTrack(track) }
        })
        this.session = inviter
        await inviter.invite()
        // 必须在 invite() 之后绑定 pc.ontrack（此前 peerConnection 尚未创建，绑定无效）
        this._bindOutgoingMedia(inviter)
        this.status = 'calling'
        this.statusText = '拨打 ' + number
        return true
      } catch (e) {
        this.errorMsg = '拨号失败: ' + (e && e.message ? e.message : e)
        return false
      }
    },

    /** 应答来电 */
    async answer() {
      if (this.session && this.session.accept) {
        await this.session.accept({
          sessionDescriptionHandlerOptions: { constraints: { audio: true, video: false } }
        })
      }
    },

    /** 挂断 */
    async hangup() {
      if (this.session && this.session.bye) {
        await this.session.bye()
      }
      this.session = null
      this._clearAudio()
      this.status = this.enabled ? 'registered' : 'idle'
      this.statusText = this.enabled ? '语音已注册' : '未连接'
      if (this.onHangup) this.onHangup()
    },

    /** 解锁播放（浏览器 autoplay 策略） */
    unlockAudio() {
      this.audioEls.forEach(el => {
        el.play().catch(() => {})
      })
    },

    /** 注销 */
    async unregister() {
      try {
        if (this._registerer) await this._registerer.unregister()
        if (this._ua) this._ua.stop()
      } catch (e) {}
      this.session = null
      this._ua = null
      this._registerer = null
      this.enabled = false
      this.status = 'unregistered'
      this.statusText = '语音已注销'
      this._clearAudio()
    },

    // ---- 内部 ----

    _bindIncomingMedia(invitation) {
      try {
        const sdh = invitation.sessionDescriptionHandler
        if (sdh && sdh.peerConnection) {
          sdh.peerConnection.ontrack = (event) => {
            const track = event.streams && event.streams[0] ? event.streams[0].getAudioTracks()[0] : null
            if (track) this._playRemoteTrack(track)
          }
        }
      } catch (e) {}
    },

    _bindOutgoingMedia(inviter) {
      try {
        const sdh = inviter.sessionDescriptionHandler
        if (sdh && sdh.peerConnection) {
          sdh.peerConnection.ontrack = (event) => {
            const track = event.streams && event.streams[0] ? event.streams[0].getAudioTracks()[0] : null
            if (track) this._playRemoteTrack(track)
          }
        }
      } catch (e) {}
      if (inviter.stateChange && inviter.stateChange.addListener) {
        inviter.stateChange.addListener((state) => {
          const SIP = window.SIP
          if (state === SIP.SessionState.Established) {
            this.status = 'talking'
            this.statusText = '通话中'
            if (this.onTalking) this.onTalking(this.session && this.session.remoteIdentity ? this.session.remoteIdentity.uri.user : '')
          } else if (state === SIP.SessionState.Terminated) {
            this.status = this.enabled ? 'registered' : 'idle'
            this.statusText = this.enabled ? '语音已注册' : '未连接'
            if (this.onHangup) this.onHangup()
          }
        })
      }
    },

    _playRemoteTrack(track) {
      if (track.kind !== 'audio') return
      const el = document.createElement('audio')
      el.srcObject = new MediaStream([track])
      el.autoplay = true
      el.style.display = 'none'
      document.body.appendChild(el)
      this.audioEls.push(el)
      el.play().catch(() => {})
    },

    _clearAudio() {
      this.audioEls.forEach(el => { el.pause(); el.srcObject = null })
      this.audioEls = []
    }
  }
})
