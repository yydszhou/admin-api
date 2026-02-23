/**
 * 用户认证相关类型定义
 */

/**
 * 用户角色枚举
 */
export enum UserRole {
  ADMIN = 'admin',
  USER = 'user',
  GUEST = 'guest'
}

/**
 * 用户信息接口
 */
export interface User {
  /** 用户ID */
  id: string | number
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 手机号 */
  phone?: string
  /** 头像 */
  avatar?: string
  /** 角色 */
  role: UserRole
  /** 创建时间 */
  createdAt?: string
  /** 更新时间 */
  updatedAt?: string
}

/**
 * 登录表单数据
 */
export interface LoginForm {
  /** 用户名/邮箱 */
  username: string
  /** 密码 */
  password: string
  /** 记住我 */
  remember: boolean
}

/**
 * 注册表单数据
 */
export interface RegisterForm {
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 手机号 */
  phone?: string
  /** 密码 */
  password: string
  /** 确认密码 */
  confirmPassword: string
  /** 验证码 */
  code?: string
  /** 同意协议 */
  agreement: boolean
}

/**
 * 忘记密码表单数据
 */
export interface ForgotPasswordForm {
  /** 邮箱/手机号 */
  account: string
  /** 验证码 */
  code: string
  /** 新密码 */
  newPassword: string
  /** 确认新密码 */
  confirmPassword: string
}

/**
 * API 通用响应结构
 */
export interface ApiResponse<T = unknown> {
  /** 状态码 */
  code: number
  /** 消息 */
  message: string
  /** 数据 */
  data: T
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  /** 访问令牌 */
  token: string
  /** 刷新令牌 */
  refreshToken?: string
  /** 令牌过期时间（秒） */
  expiresIn?: number
  /** 用户信息 */
  userInfo: User
}

/**
 * 注册响应数据
 */
export interface RegisterResponse {
  /** 用户ID */
  userId: string | number
  /** 用户信息 */
  userInfo: User
}

/**
 * 验证码响应数据
 */
export interface CaptchaResponse {
  /** 验证码图片Base64 */
  image: string
  /** 验证码标识 */
  captchaKey: string
}

/**
 * 检查用户名/邮箱是否存在的响应
 */
export interface CheckExistResponse {
  /** 是否存在 */
  exists: boolean
}

/**
 * 第三方登录类型
 */
export enum ThirdPartyLoginType {
  WECHAT = 'wechat',
  GITHUB = 'github'
}

/**
 * 密码强度等级
 */
export enum PasswordStrength {
  WEAK = 'weak',
  MEDIUM = 'medium',
  STRONG = 'strong'
}

/**
 * 密码强度结果
 */
export interface PasswordStrengthResult {
  /** 强度等级 */
  level: PasswordStrength
  /** 强度分数 0-100 */
  score: number
  /** 提示信息 */
  message: string
}
