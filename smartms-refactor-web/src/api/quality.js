import http from '../utils/http'

// 模板分页
export function pageTemplates(params) {
  return http.get('/api/quality/templates', { params })
}

// 启用模板
export function activeTemplates() {
  return http.get('/api/quality/templates/active')
}

// 模板详情
export function getTemplate(id) {
  return http.get(`/api/quality/templates/${id}`)
}

// 模板新增/编辑
export function saveTemplate(data) {
  return http.post('/api/quality/templates', data)
}

// 启停用
export function changeTemplateStatus(id, status) {
  return http.put(`/api/quality/templates/${id}/status`, null, { params: { status } })
}

// 待质检通话
export function pageInspectCalls(params) {
  return http.get('/api/quality/calls', { params })
}

// 成绩分页
export function pageResults(params) {
  return http.get('/api/quality/results', { params })
}

// 生成质检成绩
export function generateResult(data) {
  return http.post('/api/quality/results', data)
}

// 撤销质检
export function reInspect(params) {
  return http.delete('/api/quality/results', { params })
}
