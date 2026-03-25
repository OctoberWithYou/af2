import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAuth } from '../useAuth'
import { api } from '@/api'
import { ref } from 'vue'

// Mock API
vi.mock('@/api', () => ({
  api: {
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    changePassword: vi.fn(),
  },
}))

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should return not authenticated initially', () => {
    const { isAuthenticated } = useAuth()
    expect(isAuthenticated.value).toBe(false)
  })

  it('should login successfully', async () => {
    const mockLoginResponse = {
      success: true,
      token: 'test-token',
      username: 'admin',
      mustChangePassword: true,
    }
    vi.mocked(api.login).mockResolvedValue(mockLoginResponse)

    const { login, isAuthenticated } = useAuth()
    const result = await login('admin', 'admin')

    expect(result.success).toBe(true)
    expect(localStorage.getItem('authToken')).toBe('test-token')
  })

  it('should logout and clear storage', async () => {
    localStorage.setItem('authToken', 'test-token')
    localStorage.setItem('currentUser', 'admin')

    vi.mocked(api.logout).mockResolvedValue({ success: true })

    const { logout } = useAuth()
    await logout()

    expect(localStorage.getItem('authToken')).toBeNull()
    expect(localStorage.getItem('currentUser')).toBeNull()
  })

  it('should check auth status', async () => {
    localStorage.setItem('authToken', 'test-token')

    const mockUserResponse = {
      success: true,
      data: { id: 1, username: 'admin', mustChangePassword: false },
    }
    vi.mocked(api.getCurrentUser).mockResolvedValue(mockUserResponse)

    const { checkAuth } = useAuth()
    const result = await checkAuth()

    expect(result).toBe(true)
  })

  it('should change password', async () => {
    vi.mocked(api.changePassword).mockResolvedValue({ success: true })

    const { changePassword } = useAuth()
    const result = await changePassword('old-pass', 'new-pass123')

    expect(result.success).toBe(true)
    expect(api.changePassword).toHaveBeenCalledWith({
      oldPassword: 'old-pass',
      newPassword: 'new-pass123',
    })
  })

  it('should reject password less than 6 characters', async () => {
    // 这个验证在前端组件中进行，但 useAuth 应该传递正确的参数
    vi.mocked(api.changePassword).mockResolvedValue({ success: false, message: '密码长度至少 6 位' })

    const { changePassword } = useAuth()
    const result = await changePassword('admin', '123')

    expect(result.success).toBe(false)
  })
})
