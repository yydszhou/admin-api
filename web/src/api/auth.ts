/**
 * 认证相关 API 接口
 */

import { get, post } from '@/utils/request'
import type {
  LoginForm,
  RegisterForm,
  ForgotPasswordForm,
  LoginResponse,
  RegisterResponse,
  CaptchaResponse,
  CheckExistResponse,
  User,
  ThirdPartyLoginType
} from '@/types/auth'

/**
 * 用户登录
 * @param data 登录表单数据
 */
export const login = (data: LoginForm): Promise<LoginResponse> => {
  return post<LoginResponse>('/users/login', data as Record<string, unknown>)
}

/**
 * 用户注册
 * @param data 注册表单数据
 */
export const register = (data: RegisterForm): Promise<RegisterResponse> => {
  return post<RegisterResponse>('/users/register', data as Record<string, unknown>)
}

/**
 * 用户登出
 */
export const logout = (): Promise<void> => {
  return post('/auth/logout')
}

/**
 * 获取当前登录用户信息
 */
export const getUserInfo = (): Promise<User> => {
  return get<User>('/auth/user')
}

/**
 * 刷新 Token
 */
export const refreshToken = (): Promise<{ token: string }> => {
  return post<{ token: string }>('/auth/refresh')
}

/**
 * 忘记密码 - 重置密码
 * @param data 忘记密码表单数据
 */
export const resetPassword = (data: ForgotPasswordForm): Promise<void> => {
  return post('/auth/reset-password', data as Record<string, unknown>)
}

/**
 * 发送验证码
 * @param account 邮箱或手机号
 * @param type 验证码类型：'email' | 'sms'
 */
export const sendCode = (
  account: string,
  type: 'email' | 'sms' = 'email'
): Promise<void> => {
  return post('/auth/send-code', { account, type })
}

/**
 * 获取图片验证码
 */
export const getCaptcha = (): Promise<CaptchaResponse> => {
  return get<CaptchaResponse>('/auth/captcha')
}

/**
 * 验证图片验证码
 * @param code 验证码
 * @param captchaKey 验证码标识
 */
export const verifyCaptcha = (
  code: string,
  captchaKey: string
): Promise<void> => {
  return post('/auth/verify-captcha', { code, captchaKey })
}

/**
 * 检查用户名是否已存在
 * @param username 用户名
 */
export const checkUsernameExists = (username: string): Promise<CheckExistResponse> => {
  return get<CheckExistResponse>('/auth/check-username', { username })
}

/**
 * 检查邮箱是否已存在
 * @param email 邮箱
 */
export const checkEmailExists = (email: string): Promise<CheckExistResponse> => {
  return get<CheckExistResponse>('/auth/check-email', { email })
}

/**
 * 第三方登录
 * @param type 第三方登录类型
 * @param code 授权码
 */
export const thirdPartyLogin = (
  type: ThirdPartyLoginType,
  code: string
): Promise<LoginResponse> => {
  return post<LoginResponse>('/auth/third-party', { type, code })
}

/**
 * 获取第三方登录授权 URL
 * @param type 第三方登录类型
 * @param redirectUri 回调地址
 */
export const getThirdPartyAuthUrl = (
  type: ThirdPartyLoginType,
  redirectUri: string
): Promise<{ authUrl: string }> => {
  return get<{ authUrl: string }>('/auth/third-party/url', { type, redirectUri })
}
