import { defineStore } from 'pinia'
import { login as apiLogin, getMenuTree, getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    // 刷新后从 localStorage 恢复用户（isAgent/agentCode 依赖它，否则电话条不显示）
    user: (() => {
      try { return JSON.parse(localStorage.getItem('user') || 'null') } catch (e) { return null }
    })(),
    menus: []
  }),
  getters: {
    isLoggedIn: state => !!state.token,
    userName: state => state.user?.userName || state.user?.loginName || '',
    isAgent: state => state.user?.roleType === 1,
    agentCode: state => state.user?.qualityAgCode || ''
  },
  actions: {
    async login(username, password) {
      const res = await apiLogin({ username, password })
      this.token = res.token
      this.user = res.user
      localStorage.setItem('token', res.token)
      localStorage.setItem('user', JSON.stringify(res.user))
      return res
    },
    async fetchMenus() {
      this.menus = await getMenuTree()
      return this.menus
    },
    async fetchMe() {
      this.user = await getMe()
      return this.user
    },
    logout() {
      this.token = ''
      this.user = null
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
