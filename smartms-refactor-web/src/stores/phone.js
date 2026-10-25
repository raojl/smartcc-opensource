import { defineStore } from 'pinia'
import { phoneAction, phoneEvents } from '../api/agentPhone'

/** 软电话会话持久化键（老系统 cookie sessionID/workNo 等价，刷新恢复避免 203 工号已签入） */
const SESSION_KEY = 'smartcc.phone.session'

/**
 * 软电话全局状态（移植老系统 _header_new.jsp 公共头的软电话条）
 *
 * 关键设计：轮询生命周期在 store 内管理（setTimeout 递归），
 * 不绑定任何组件 —— 坐席在任意页面跳转，软电话状态与事件轮询不断。
 * 组件（PhoneBar/坐席工作台）只负责渲染和调用 action。
 */
export const usePhoneStore = defineStore('phone', {
  state: () => ({
    loggedIn: false,
    loginLoading: false,
    workNo: '',
    extenNo: '',
    skills: [],
    agentName: '',
    sessionId: '',
    // 状态机（翻译老系统 softphone_constants.js 0-26）
    connStatus: 0,
    talking: false,
    holding: false,
    dialNumber: '',
    currentNumber: '',
    ctiError: '',
    polling: false,
    pollTimer: null,
    collapsed: false
  }),

  getters: {
    statusText: s => (STATUS_MAP[s.connStatus]?.text || '空闲'),
    statusIcon: s => (STATUS_MAP[s.connStatus]?.icon || '⚪')
  },

  actions: {
    /**
     * 恢复会话（老系统 login() 的 cookie 恢复逻辑）：
     * localStorage 有 sessionId 且工号一致 → 不重复 Login，直接恢复签入态 + 事件轮询。
     * 否则刷新后重新 Login 会触发 CTI "203 工号已签入"。
     */
    restoreSession() {
      try {
        const s = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null')
        if (s && s.sessionId && s.workNo) {
          this.workNo = s.workNo
          this.extenNo = s.extenNo || ''
          this.skills = s.skills || []
          this.agentName = s.agentName || ''
          this.sessionId = s.sessionId
          this.loggedIn = true
          this.connStatus = 2
          this.currentNumber = this.extenNo
          this.startPolling()
          return true
        }
      } catch (e) { /* 损坏则忽略 */ }
      return false
    },

    // ============ 登入 / 登出 ============
    async login({ workNo, extenNo, skills, agentName }) {
      this.loginLoading = true
      this.ctiError = ''
      try {
        // 官方 WS 协议（§4.1）：skilldesc 为分号分隔技能名，如 "1002;1003"
        const skillDesc = skills.join(';')
        const res = await phoneAction({
          action: 'Login',
          exten: extenNo,
          workNo,
          agentName,
          skills: skillDesc
        })
        // 签入失败：一律不记录 sessionId
        if (res.Succeed === false || res.Succeed === 0 || res.Succeed === 'false') {
          const code = Number(res.Result)
          // 203 = 已登录（重复登录被拒，§6）→ 提示引导
          const alreadyLogged = code === 203
          this.sessionId = ''
          this.clearSession()
          if (alreadyLogged) return { inService: true }
          throw new Error(phoneErrorMsg(res))
        }
        // 签入成功才记录会话
        this.workNo = workNo
        this.extenNo = extenNo
        this.skills = skills
        this.agentName = agentName || ''
        this.sessionId = res.SessionID || ''
        this.loggedIn = true
        // IPC 登录默认示忙(NotReady, status=2)，等事件推送确认实际状态
        this.connStatus = 2
        this.currentNumber = extenNo
        this.saveSession()
        this.startPolling()
        return true
      } catch (e) {
        this.ctiError = e?.message || 'CTI 软电话服务不可达'
        throw e
      } finally {
        this.loginLoading = false
      }
    },

    /** 持久化会话（老系统 cookie sessionID/workNo 等价） */
    saveSession() {
      localStorage.setItem(SESSION_KEY, JSON.stringify({
        workNo: this.workNo, extenNo: this.extenNo, skills: this.skills,
        agentName: this.agentName, sessionId: this.sessionId
      }))
    },

    clearSession() {
      localStorage.removeItem(SESSION_KEY)
    },

    async logout() {
      try {
        await phoneAction({ action: 'Logout', exten: this.extenNo, workNo: this.workNo, sessionId: this.sessionId })
      } catch (e) { /* 登出失败也继续 */ }
      this.stopPolling()
      this.clearSession()
      this.reset()
    },

    reset() {
      this.loggedIn = false
      this.workNo = ''
      this.extenNo = ''
      this.skills = []
      this.agentName = ''
      this.sessionId = ''
      this.connStatus = 0
      this.talking = false
      this.holding = false
      this.dialNumber = ''
      this.currentNumber = ''
      this.ctiError = ''
      this._reLoginAttempted = false
    },

    // ============ 事件长轮询（老系统 _waitEvent 递归 GetState） ============
    startPolling() {
      if (this.polling) return
      this.polling = true
      this.pollOnce()
    },

    stopPolling() {
      this.polling = false
      if (this.pollTimer) { clearTimeout(this.pollTimer); this.pollTimer = null }
    },

    async pollOnce() {
      if (!this.polling || !this.loggedIn) return
      try {
        const res = await phoneEvents(this.extenNo, this.workNo, this.sessionId)
        const code = Number(res.Result)
        if (code === 208) {
          // 工号被强制签出（如 CTI 管理端踢下线）→ 清会话回登入界面
          this.ctiError = '工号已被强制签出，请重新签入'
          this.stopPolling()
          this.clearSession()
          this.loggedIn = false
          this.connStatus = 0
          return
        }
        // 205 = 会话失效（老系统：清除 cookie 自动重新签入）
        if (code === 205 || (code === 201 && String(res.Response || '').includes('session count[0]'))) {
          if (!this._reLoginAttempted) {
            this._reLoginAttempted = true
            this.ctiError = '会话已失效，自动重新签入…'
            const r = await this.login({
              workNo: this.workNo, extenNo: this.extenNo,
              skills: this.skills, agentName: this.agentName
            }).catch(() => null)
            this._reLoginAttempted = false
            // 重签遇"工号处于服务状态"（无会话钥匙）→ 回登入，让用户核对配置
            if (r && r.inService) {
              this.stopPolling()
              this.clearSession()
              this.loggedIn = false
              this.connStatus = 0
              return
            }
            return
          }
          // 重签也失败 → 放弃，回登入界面
          this.ctiError = '会话失效且重新签入失败，请手动签入'
          this.stopPolling()
          this.clearSession()
          this.loggedIn = false
          this.connStatus = 0
          return
        }
        this._reLoginAttempted = false
        const events = res.Event || res.events || []
        events.forEach(evt => this.handleEvent(evt))
        this.ctiError = ''
      } catch (e) {
        this.ctiError = e?.message || 'CTI 服务不可达，事件轮询中断'
      }
      // 递归长轮询（不依赖组件生命周期）
      if (this.polling && this.loggedIn) {
        this.pollTimer = setTimeout(() => this.pollOnce(), 300)
      }
    },

    handleEvent(item) {
      // WS 协议事件用 EventName 字段；老 HTTP 协议用 Event/AgentStatus/MediaControl
      const evtName = item.EventName || item.Event || item.event || item.MediaControl || item.AgentStatus
      if (!evtName) return
      // IPC 状态码 → 老系统状态机映射（aas/main.go：1已登录/2示忙/3就绪/4锁定/5通话/6话后/7其他/8登出）
      const IPC_STATUS_MAP = {
        1: 1,   // 已登录 → 空闲
        2: 2,   // 示忙（NotReady）
        3: 1,   // 就绪（Ready）→ 空闲
        4: 2,   // 锁定 → 近似示忙
        5: 5,   // 通话中
        6: 15,  // 话后整理
        7: 16,  // 其他工作
        8: 14   // 登出
      }
      // 事件名映射优先（更精确）；IPC 状态码兜底（状态变更事件常只带 status）
      const st = STATUS_EVENT[evtName]
      if (st !== undefined && st !== null) {
        this.connStatus = st
        this.talking = st === 5 || st === 6 || st === 7 || st === 20 || st === 21
        this.holding = st === 8 || st === 18
      } else if (item.status !== undefined && item.status !== null) {
        const mapped = IPC_STATUS_MAP[Number(item.status)]
        if (mapped !== undefined) {
          this.connStatus = mapped
          this.talking = mapped === 5 || mapped === 6 || mapped === 7 || mapped === 20 || mapped === 21
          this.holding = mapped === 8 || mapped === 18
        }
      }
      // 来电 → 抛事件给所有订阅者（弹屏/全局通知，多订阅不互相覆盖）
      if (evtName === 'OnEvtOffering' || evtName === 'OnEvtIncoming' || evtName === 'OnEvt0x20100002') {
        const ani = item.ani || item.callerid || item.CallerID || ''
        this.currentNumber = ani || this.currentNumber
        ;(this._incomingListeners || []).forEach(fn => {
          try { fn(ani) } catch (e) { /* 单个订阅者异常不影响其他 */ }
        })
      }
      if (evtName === 'OnEvtReleased' || evtName === 'OnEvtAgentLogout') {
        this.talking = false
        this.holding = false
        this.dialNumber = ''
        this.currentNumber = this.extenNo
      }
    },

    /** 来电回调订阅（多订阅者：坐席工作台弹屏 + PhoneBar 全局通知） */
    onIncoming(fn) {
      if (!this._incomingListeners) this._incomingListeners = []
      if (fn && !this._incomingListeners.includes(fn)) this._incomingListeners.push(fn)
    },

    /** 取消来电订阅 */
    offIncoming(fn) {
      if (this._incomingListeners) this._incomingListeners = this._incomingListeners.filter(f => f !== fn)
    },

    // ============ 控制指令 ============
    async execAction(action, extra = {}) {
      this.ctiError = ''
      try {
        const res = await phoneAction({
          action,
          exten: this.extenNo,
          workNo: this.workNo,
          sessionId: this.sessionId,
          ...extra
        })
        if (res.Succeed === false || res.Succeed === 0 || res.Succeed === 'false') {
          throw new Error(phoneErrorMsg(res))
        }
        return true
      } catch (e) {
        this.ctiError = e?.message || 'CTI 服务不可达'
        throw e
      }
    },

    pressKey(k) {
      this.dialNumber += k
      this.currentNumber = this.dialNumber
    },

    clearDial() {
      this.dialNumber = ''
    },

    async dial() {
      if (!this.dialNumber) throw new Error('请输入号码')
      const ok = await this.execAction('MakeCall', { destType: 0, destNum: this.dialNumber, deviceDN: this.extenNo })
      if (ok) {
        this.connStatus = 10
        this.talking = true
        this.currentNumber = this.dialNumber
      }
    },

    async hangup() {
      const ok = await this.execAction('HangupCall')
      if (ok) {
        this.connStatus = 1
        this.talking = false
        this.holding = false
        this.dialNumber = ''
        this.currentNumber = this.extenNo
      }
    },

    async holdTalk() {
      const action = this.holding ? 'RetrieveCall' : 'HoldCall'
      const ok = await this.execAction(action)
      if (ok) {
        this.holding = !this.holding
        this.connStatus = this.holding ? 8 : 5
      }
    },

    async transferCall() {
      await this.execAction('SingleStepTransfer', { destNum: this.dialNumber || this.currentNumber, deviceDN: this.extenNo })
      this.connStatus = 9
      setTimeout(() => { this.connStatus = 1; this.talking = false; this.holding = false }, 1000)
    },

    async setIdle() {
      const ok = await this.execAction('Idle')
      if (ok) this.connStatus = 1
    },

    async setBusy() {
      const ok = await this.execAction('Busy')
      if (ok) this.connStatus = 2
    }
  }
})

// 状态机（翻译老系统 softphone_constants.js 0-26）
export const STATUS_MAP = {
  0: { text: '未登入', icon: '⚪' },
  1: { text: '示闲', icon: '🟢' },
  2: { text: '示忙', icon: '🔴' },
  3: { text: '振铃', icon: '📞' },
  4: { text: '等待 IVR', icon: '🕓' },
  5: { text: '通话中', icon: '💬' },
  6: { text: '会议', icon: '👥' },
  7: { text: '咨询', icon: '🤝' },
  8: { text: '保持', icon: '⏸' },
  9: { text: '转移中', icon: '↪️' },
  10: { text: '外呼拨号', icon: '📳' },
  11: { text: '会议拨号', icon: '📳' },
  12: { text: '咨询拨号', icon: '📳' },
  13: { text: '保持拨号', icon: '📳' },
  14: { text: '已登出', icon: '⚪' },
  15: { text: '话后整理', icon: '🧹' },
  16: { text: '休息', icon: '😴' },
  17: { text: '休息超时', icon: '⏰' },
  18: { text: '保持中', icon: '⏸' },
  19: { text: '静音', icon: '🔇' },
  20: { text: '被咨询通话', icon: '🤝' },
  21: { text: '被会议通话', icon: '👥' },
  22: { text: '监听中', icon: '👂' },
  23: { text: '强插中', icon: '⚡' },
  24: { text: '外呼中', icon: '📳' },
  25: { text: '咨询中', icon: '🤝' },
  26: { text: '强插通话', icon: '⚡' }
}

// 事件 → 状态（官方协议 §7 事件表；值对应老系统状态机 0-26）
// 注：OnEvtAgentLogin（登录/状态变更）不带固定状态，走 IPC_STATUS_MAP 按 status 字段映射
const STATUS_EVENT = {
  OnEvtAgentReady: 1,          // 就绪
  OnEvtAgentLogout: 14,        // 登出
  OnEvtAgentNotReady: 2,       // 忙碌
  OnEvtAgentWorking: 5,        // 通话中
  OnEvtAgentLocked: 2,         // 锁定（近似置忙）
  OnEvtAgentOtherWork: 16,     // 其他工作
  OnEvtAgentAfterCallWork: 15, // 话后整理
  OnEvtOffHook: 1,             // 摘机
  OnEvtOffering: 3,            // 来话振铃
  OnEvtDialing: 10,            // 外呼拨号
  OnEvtConnected: 5,           // 通话接通
  OnEvtReleased: 1,            // 挂断释放
  OnEvtConsultDialing: 12,     // 咨询拨号
  OnEvtBeConsultOffering: 3,   // 被咨询振铃
  OnEvtBeConsultConnected: 7,  // 被咨询接通
  OnEvtConsultFailed: 2,       // 咨询失败
  OnEvtConsultConnected: 7,
  OnEvtConferenced: 6,         // 三方会议
  OnEvtHeld: 8,
  OnEvtHold: 8,
  OnEvtConferenceDialing: 11,
  OnEvtTransferDailing: 9,
  OnEvtListened: 22,
  OnEvtInsertedTalking: 26
}

// Result 码翻译（官方协议 §6）
export const RESULT_MSG = {
  200: '成功',
  201: '会话不存在，请重新签入',
  203: '工号已登录（重复签入被拒）',
  204: '参数缺失或格式错误',
  205: '会话已过期，请重新签入',
  206: 'CTI 链路不可用，请稍后再试',
  207: '分机未注册，请检查分机号是否已注册',
  208: '工号已被强制签出'
}

// 错误事件翻译（老系统 softphone_constants.js errMsgDic，兼容旧协议）
export const FAIL_MSG = {
  525: '未注册分机',
  1343225864: '未注册分机',
  1343225862: '工号已处于服务状态（已登录或与分机不匹配）',
  1343225881: '对方状态忙',
  1343225874: '无效的目标号码',
  1343225873: '无效的工号',
  1343225863: '分机不存在',
  1343225865: '技能组错误'
}

/** 从软电话响应提取可读错误信息（Result 码 → Event 错误码 → 原始描述） */
export function phoneErrorMsg(res) {
  if (!res) return 'CTI 拒绝操作'
  // 1. 事件里的通用错误码最精确
  const ev = res.Event || []
  for (const e of ev) {
    if (e.universalfailureid && FAIL_MSG[e.universalfailureid]) {
      return FAIL_MSG[e.universalfailureid]
    }
    if (e.universalfailuredesc) return e.universalfailuredesc
  }
  // 2. Result 码
  const code = Number(res.Result)
  if (RESULT_MSG[code]) return RESULT_MSG[code]
  // 3. 原始 Response 描述
  if (res.Response && res.Response !== 'OK') return res.Response
  return `CTI 拒绝(码 ${res.Result})`
}
