import http from '../utils/http'

// 客户分页（deleted=true 回收站）
export function pageCustomers(params) {
  return http.get('/api/customers', { params })
}

// 详情
export function getCustomer(id) {
  return http.get(`/api/customers/${id}`)
}

// 新增/编辑
export function saveCustomer(data) {
  return http.post('/api/customers', data)
}

// 软删除
export function deleteCustomer(id) {
  return http.delete(`/api/customers/${id}`)
}

// 恢复
export function restoreCustomer(id) {
  return http.post(`/api/customers/${id}/restore`)
}

// 弹屏查询
export function dialSearch(params) {
  return http.get('/api/customers/dial-search', { params })
}
