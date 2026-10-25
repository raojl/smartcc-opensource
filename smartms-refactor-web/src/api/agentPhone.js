import http from '../utils/http'

// 软电话指令（移植老系统 softphoneBar AMI 协议，后端代理到 CTI /softphone/agentaction）
export function phoneAction(payload) {
  return http.post('/api/agent-phone/action', payload)
}

// 事件长轮询（GetState）
export function phoneEvents(exten, workNo, sessionId) {
  return http.get('/api/agent-phone/events', { params: { exten, workNo, sessionId } })
}
