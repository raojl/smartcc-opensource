import http from '../utils/http'

// 角色分页
export function pageRoles(params) {
  return http.get('/api/roles', { params })
}

// 全部角色
export function allRoles() {
  return http.get('/api/roles/all')
}

// 角色详情（含菜单授权）
export function getRole(id) {
  return http.get(`/api/roles/${id}`)
}

// 新增
export function createRole(data) {
  return http.post('/api/roles', data)
}

// 编辑
export function updateRole(id, data) {
  return http.put(`/api/roles/${id}`, data)
}

// 删除
export function deleteRole(id) {
  return http.delete(`/api/roles/${id}`)
}

// 全部菜单树（授权勾选）
export function menuTree() {
  return http.get('/api/roles/menu-tree')
}
