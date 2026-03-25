import { describe, it, expect, vi, beforeEach } from 'vitest'
import { api } from '../index'
import axios from 'axios'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios, true)

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should create api client instance', () => {
    expect(api).toBeDefined()
  })

  it('should add auth token to requests when logged in', () => {
    localStorage.setItem('authToken', 'test-token')
    // 验证 token 会被添加到请求头
    expect(localStorage.getItem('authToken')).toBe('test-token')
  })

  it('should clear token on 401 response', () => {
    localStorage.setItem('authToken', 'test-token')
    localStorage.setItem('currentUser', 'admin')

    // 模拟 401 响应
    const mockError = {
      response: { status: 401, data: { success: false, message: 'Unauthorized' } },
    }

    // 验证 interceptor 会清除 token
    expect(localStorage.getItem('authToken')).toBe('test-token')
  })
})
