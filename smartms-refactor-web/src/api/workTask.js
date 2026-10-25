import http from '../utils/http'

// 工单分页（belongAgent/workAgent=my 我的过滤）
export function pageWorkTasks(params) {
  return http.get('/api/work-tasks', { params })
}

// 详情
export function getWorkTask(id) {
  return http.get(`/api/work-tasks/${id}`)
}

// 新增
export function createWorkTask(data) {
  return http.post('/api/work-tasks', data)
}

// 编辑
export function updateWorkTask(id, data) {
  return http.put(`/api/work-tasks/${id}`, data)
}

// 办结
export function finishWorkTask(id) {
  return http.post(`/api/work-tasks/${id}/finish`)
}

// 转办
export function transferWorkTask(id, transferAgent) {
  return http.post(`/api/work-tasks/${id}/transfer`, { transferAgent })
}

// 坐席候选
export function workTaskAgents(searchKey) {
  return http.get('/api/work-tasks/agents', { params: { searchKey } })
}
