import http from '../utils/http'

// 用户分页
export function pageUsers(params) {
  return http.get('/api/users', { params })
}

// 用户详情
export function getUser(id) {
  return http.get(`/api/users/${id}`)
}

// 新增
export function createUser(data) {
  return http.post('/api/users', data)
}

// 编辑
export function updateUser(id, data) {
  return http.put(`/api/users/${id}`, data)
}

// 删除
export function deleteUser(id) {
  return http.delete(`/api/users/${id}`)
}

// 重置密码
export function resetPassword(id) {
  return http.post(`/api/users/${id}/reset-password`)
}

// 角色列表（全部）
export function allRoles() {
  return http.get('/api/roles/all')
}
