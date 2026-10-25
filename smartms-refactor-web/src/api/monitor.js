import http from '../utils/http'

// 实时监控
export function rtdata(queryId) {
  return http.get('/api/monitor/rtdata-query', { params: { queryId } })
}
export function monitorAgents() {
  return http.get('/api/monitor/agents')
}
export function idleAgents() {
  return http.get('/api/monitor/agents/idle')
}
export function agentResults(agentId) {
  return http.get('/api/monitor/agents/results', { params: agentId ? { agentId } : {} })
}
export function agentDns(agentDN) {
  return http.get('/api/monitor/agents/dns', { params: agentDN ? { agentDN } : {} })
}
export function skillDomains() {
  return http.get('/api/monitor/skill-domains')
}
