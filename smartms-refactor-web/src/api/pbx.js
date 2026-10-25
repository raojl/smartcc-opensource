import http from '../utils/http'

// SIP 分机
export function pageDevices(params) {
  return http.get('/api/pbx/devices', { params })
}
export function saveDevice(data) {
  return http.post('/api/pbx/devices', data)
}
export function deleteDevice(id) {
  return http.delete(`/api/pbx/devices/${id}`)
}

// 出局中继
export function pageTrunks(params) {
  return http.get('/api/pbx/trunks', { params })
}
export function saveTrunk(data) {
  return http.post('/api/pbx/trunks', data)
}
export function deleteTrunk(id) {
  return http.delete(`/api/pbx/trunks/${id}`)
}

// 路由
export function pageRoutes(params) {
  return http.get('/api/pbx/routes', { params })
}
export function saveRoute(data) {
  return http.post('/api/pbx/routes', data)
}
export function deleteRoute(id) {
  return http.delete(`/api/pbx/routes/${id}`)
}

// 局向
export function pageTrunkDistributes(params) {
  return http.get('/api/pbx/trunk-distributes', { params })
}
export function saveTrunkDistribute(data) {
  return http.post('/api/pbx/trunk-distributes', data)
}
export function deleteTrunkDistribute(id) {
  return http.delete(`/api/pbx/trunk-distributes/${id}`)
}
