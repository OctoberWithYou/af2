import axios, { type AxiosInstance, type AxiosError } from 'axios'
import type {
  LoginRequest,
  LoginResponse,
  ChangePasswordRequest,
  ApiResponse,
  DeployConfig,
  DeployStats,
  User,
} from '@/types'

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: '/api',
      timeout: 30000,
    })

    // 请求拦截器 - 添加 token
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })

    // 响应拦截器 - 处理错误
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ApiResponse>) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('authToken')
          localStorage.removeItem('currentUser')
          window.location.reload()
        }
        return Promise.reject(error)
      }
    )
  }

  // 认证相关 API
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const { data } = await this.client.post<LoginResponse>('/auth/login', credentials)
    return data
  }

  async logout(): Promise<ApiResponse> {
    const { data } = await this.client.post<ApiResponse>('/auth/logout')
    return data
  }

  async changePassword(passwords: ChangePasswordRequest): Promise<ApiResponse> {
    const { data } = await this.client.post<ApiResponse>('/auth/change-password', passwords)
    return data
  }

  async getCurrentUser(): Promise<ApiResponse<User>> {
    const { data } = await this.client.get<ApiResponse<User>>('/auth/me')
    return data
  }

  // 部署配置相关 API
  async getConfigs(): Promise<ApiResponse<DeployConfig[]>> {
    const { data } = await this.client.get<ApiResponse<DeployConfig[]>>('/deploy/configs')
    return data
  }

  async getConfig(id: number): Promise<ApiResponse<DeployConfig>> {
    const { data } = await this.client.get<ApiResponse<DeployConfig>>(`/deploy/configs/${id}`)
    return data
  }

  async createConfig(config: Partial<DeployConfig>): Promise<ApiResponse> {
    const { data } = await this.client.post<ApiResponse>('/deploy/configs', config)
    return data
  }

  async updateConfig(id: number, config: Partial<DeployConfig>): Promise<ApiResponse> {
    const { data } = await this.client.put<ApiResponse>(`/deploy/configs/${id}`, config)
    return data
  }

  async deleteConfig(id: number): Promise<ApiResponse> {
    const { data } = await this.client.delete<ApiResponse>(`/deploy/configs/${id}`)
    return data
  }

  async deploy(id: number): Promise<ApiResponse> {
    const { data } = await this.client.post<ApiResponse>(`/deploy/configs/${id}/deploy`)
    return data
  }

  async stop(id: number): Promise<ApiResponse> {
    const { data } = await this.client.post<ApiResponse>(`/deploy/configs/${id}/stop`)
    return data
  }

  async getStats(): Promise<ApiResponse<DeployStats>> {
    const { data } = await this.client.get<ApiResponse<DeployStats>>('/deploy/stats')
    return data
  }
}

export const api = new ApiClient()
