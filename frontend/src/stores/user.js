import { defineStore } from 'pinia'
import { ref } from 'vue'
import { users, auth } from '../api'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)

  const fetchUser = async () => {
    try {
      const res = await users.me()
      if (res.code === 200) {
        user.value = res.data
        // 保存到 localStorage 供路由守卫使用
        localStorage.setItem('user', JSON.stringify(res.data))
      }
    } catch (e) {
      console.error(e)
    }
  }

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      await auth.logout(refreshToken)
    } catch (e) {
      console.error('Logout error:', e)
    } finally {
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }

  return { user, fetchUser, logout }
})
