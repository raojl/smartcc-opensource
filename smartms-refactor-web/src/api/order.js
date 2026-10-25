import http from '../utils/http'

// 订单分页（agentId=my 我的订单）
export function pageOrders(params) {
  return http.get('/api/orders', { params })
}

// 订单保存
export function saveOrder(data) {
  return http.post('/api/orders', data)
}

// 订单删除
export function deleteOrder(id) {
  return http.delete(`/api/orders/${id}`)
}

// 产品分页
export function pageProducts(params) {
  return http.get('/api/orders/products', { params })
}

// 产品保存
export function saveProduct(data) {
  return http.post('/api/orders/products', data)
}

// 产品删除
export function deleteProduct(id) {
  return http.delete(`/api/orders/products/${id}`)
}
