import http from '../utils/http'

/**
 * WebRTC 分机管理 API（混合方案语音通道）
 * 语音：浏览器 sip.js 直连 Asterisk WSS；控制：走 agentPhone（CTI 8082）
 */

/** 创建坐席 WebRTC 分机（写 Asterisk pjsip.conf + reload） */
export function createWebRtcExtension(data) {
  return http.post('/api/webrtc/extensions', data)
}

/** WebRTC 分机列表 */
export function listWebRtcExtensions() {
  return http.get('/api/webrtc/extensions')
}

/** 删除 WebRTC 分机 */
export function deleteWebRtcExtension(exten) {
  return http.delete(`/api/webrtc/extensions/${exten}`)
}

/** 坐席 WebRTC 接入配置（PhoneBar 初始化：WSS 地址/分机） */
export function getWebRtcConfig(exten) {
  return http.get('/api/webrtc/config', { params: { exten } })
}
