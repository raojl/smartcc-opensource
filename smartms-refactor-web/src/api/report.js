import http from '../utils/http'

// 呼入话务统计
export function inboundTalk(params) {
  return http.get('/api/report/talk/inbound', { params })
}

// 外呼话务统计
export function outboundTalk(params) {
  return http.get('/api/report/talk/outbound', { params })
}

// 坐席 KPI
export function agentKpi(params) {
  return http.get('/api/report/talk/agent-kpi', { params })
}

// 满意度统计
export function satisfaction(params) {
  return http.get('/api/report/satisfaction', { params })
}
