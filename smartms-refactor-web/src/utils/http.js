import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '',
  timeout: 30000
})

// 请求拦截：带 token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一错误处理
http.interceptors.response.use(
  response => response.data,
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 400 || status === 403) {
      ElMessage.error(message || '请求失败')
    } else {
      ElMessage.error(message || '服务器内部错误')
    }
    return Promise.reject(error)
  }
)

export default http
