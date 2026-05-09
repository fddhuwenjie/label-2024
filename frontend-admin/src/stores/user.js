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
        localStorage.setItem('admin_user', JSON.stringify(res.data))
      }
    } catch (e) {
      console.error(e)
    }
  }

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('admin_refreshToken')
      await auth.logout(refreshToken)
    } catch (e) {
      console.error('Logout error:', e)
    } finally {
      user.value = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_refreshToken')
      localStorage.removeItem('admin_user')
    }
  }

  return { user, fetchUser, logout }
})
