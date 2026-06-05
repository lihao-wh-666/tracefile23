import { defineStore } from 'pinia'
import { login as loginApi } from '../api/auth'
import { getUserInfo as getUserInfoApi } from '../api/user'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),

  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      router.push('/login')
    },

    async getUserInfo() {
      const res = await getUserInfoApi()
      this.userInfo = res.data
      return res
    }
  }
})
