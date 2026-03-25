// 用户信息
export interface User {
  id: number
  username: string
  mustChangePassword: boolean
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  success: boolean
  message?: string
  token?: string
  username?: string
  mustChangePassword?: boolean
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

// 通用 API 响应
export interface ApiResponse<T = unknown> {
  success: boolean
  message?: string
  data?: T
}

// 部署配置
export interface DeployConfig {
  id: number
  name: string
  type: 'AGENT' | 'SERVER'
  status: 'RUNNING' | 'STOPPED' | 'DEPLOYING'
  configJson: Record<string, unknown>
}

// 统计数据
export interface DeployStats {
  totalAgents: number
  runningAgents: number
  totalServers: number
  runningServers: number
}
