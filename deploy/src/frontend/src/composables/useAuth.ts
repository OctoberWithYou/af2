import { ref, computed } from 'vue'
import { api } from '@/api'
import type { User } from '@/types'
import router from '@/router'

let authToken = localStorage.getItem('authToken') || ''
const currentUser = ref<User | null>(null)

export function useAuth() {
  const isAuthenticated = computed(() => !!authToken)
  const mustChangePassword = computed(() => currentUser.value?.mustChangePassword ?? false)

  const login = async (username: string, password: string) => {
    const result = await api.login({ username, password })
    if (result.success && result.token) {
      authToken = result.token
      localStorage.setItem('authToken', authToken)
      if (result.username) {
        localStorage.setItem('currentUser', result.username)
      }
      return result
    }
    return result
  }

  const logout = async () => {
    await api.logout().catch(() => {})
    authToken = ''
    currentUser.value = null
    localStorage.removeItem('authToken')
    localStorage.removeItem('currentUser')
    router.push('/login')
  }

  const checkAuth = async (): Promise<boolean> => {
    if (!authToken) {
      return false
    }
    try {
      const result = await api.getCurrentUser()
      if (result.success && result.data) {
        currentUser.value = result.data
        return true
      }
    } catch {
      // Token 无效
    }
    return false
  }

  const changePassword = async (oldPassword: string, newPassword: string) => {
    const result = await api.changePassword({ oldPassword, newPassword })
    if (result.success) {
      currentUser.value = null
    }
    return result
  }

  return {
    isAuthenticated,
    mustChangePassword,
    currentUser,
    login,
    logout,
    checkAuth,
    changePassword,
  }
}
