import http from '../utils/http'

// 任务聚合列表
export function pageTasks(params) {
  return http.get('/api/outbound/tasks', { params })
}

// 任务详单
export function pageTaskDetails(params) {
  return http.get('/api/outbound/details', { params })
}

// 启动
export function startTask(taskId) {
  return http.post(`/api/outbound/tasks/${taskId}/start`)
}

// 停止
export function stopTask(taskId) {
  return http.post(`/api/outbound/tasks/${taskId}/stop`)
}

// 删除
export function deleteTask(taskId) {
  return http.delete(`/api/outbound/tasks/${taskId}`)
}

// 号码导入
export function importNumbers(data) {
  return http.post('/api/outbound/tasks/import', data)
}
