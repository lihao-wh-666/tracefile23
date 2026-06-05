import { defineStore } from 'pinia'
import { getPublicKey, login as loginApi } from '../api/auth'
import { getUserInfo as getUserInfoApi } from '../api/user'
import { encryptPassword } from '../utils/rsa'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),

  actions: {
    async login(loginForm) {
      const keyRes = await getPublicKey()
      const publicKey = keyRes.data.publicKey
      const encryptedPassword = encryptPassword(publicKey, loginForm.password)
      const loginData = {
        username: loginForm.username,
        password: encryptedPassword
      }
      const res = await loginApi(loginData)
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
