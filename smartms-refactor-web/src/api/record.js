import http from '../utils/http'

// 通话详单
export function pageRecords(params) {
  return http.get('/api/records', { params })
}

// 未接记录 type=1坐席未接 2队列未接 3客户未接 4留言
export function pageMissedCalls(params) {
  return http.get('/api/records/missed', { params })
}
