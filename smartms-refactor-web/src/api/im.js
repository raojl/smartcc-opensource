import http from '../utils/http'

// 智能消息
export function pageTopics(keyword) {
  return http.get('/api/im/topics', { params: keyword ? { keyword } : {} })
}
export function saveTopic(data) {
  return http.post('/api/im/topics', data)
}
export function deleteTopic(id) {
  return http.delete(`/api/im/topics/${id}`)
}

export function pageReplies(keyword) {
  return http.get('/api/im/replies', { params: keyword ? { keyword } : {} })
}
export function saveReply(data) {
  return http.post('/api/im/replies', data)
}
export function deleteReply(id) {
  return http.delete(`/api/im/replies/${id}`)
}

export function pageMsgRecords(keyword, limit = 50) {
  return http.get('/api/im/records', { params: { keyword, limit } })
}

export function pageInterfaces() {
  return http.get('/api/im/interfaces')
}
export function saveInterface(data) {
  return http.post('/api/im/interfaces', data)
}
export function deleteInterface(id) {
  return http.delete(`/api/im/interfaces/${id}`)
}
