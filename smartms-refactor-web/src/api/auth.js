import http from '../utils/http'

// 登录
export function login(data) {
  return http.post('/api/auth/login', data)
}

// 当前用户信息
export function getMe() {
  return http.get('/api/auth/me')
}

// 菜单树
export function getMenuTree() {
  return http.get('/api/menu/tree')
}
