import { get, post } from '@/utils/request'
import {
  UserRole,
  type LoginForm,
  type RegisterForm,
  type ForgotPasswordForm,
  type LoginResponse,
  type RegisterResponse,
  type CaptchaResponse,
  type CheckExistResponse,
  type User,
  type ThirdPartyLoginType
} from '@/types/auth'

interface BackendLoginResp {
  userId: number
  username: string
  email: string
  token: string
  roles: string[]
}

interface BackendRegisterResp {
  id: number
  username: string
  email: string
}

interface BackendUserInfoResp {
  userId: number
  username: string
  email: string
  roles: string[]
}

const mapRole = (roles?: string[]): UserRole => {
  const role = roles?.[0] || ''
  if (role === 'ADMIN') return UserRole.ADMIN
  if (role === 'OPERATOR') return UserRole.USER
  return UserRole.GUEST
}

const mapUser = (data: { userId: number; username: string; email: string; roles?: string[] }): User => ({
  id: data.userId,
  username: data.username,
  email: data.email,
  role: mapRole(data.roles)
})

export const login = (data: LoginForm): Promise<LoginResponse> => {
  return post<BackendLoginResp>('/users/login', data as Record<string, unknown>).then((resp) => ({
    token: resp.token,
    userInfo: mapUser(resp)
  }))
}

export const register = (data: RegisterForm): Promise<RegisterResponse> => {
  return post<BackendRegisterResp>('/users/register', data as Record<string, unknown>).then((resp) => ({
    userId: resp.id,
    userInfo: {
      id: resp.id,
      username: resp.username,
      email: resp.email,
      role: UserRole.GUEST
    }
  }))
}

export const logout = (): Promise<void> => post('/auth/logout')

export const getUserInfo = (): Promise<User> => {
  return get<BackendUserInfoResp>('/me').then((resp) => mapUser(resp))
}

export const refreshToken = (): Promise<{ token: string }> => post<{ token: string }>('/auth/refresh')

export const resetPassword = (data: ForgotPasswordForm): Promise<void> => {
  return post('/auth/reset-password', data as Record<string, unknown>)
}

export const sendCode = (
  account: string,
  type: 'email' | 'sms' = 'email'
): Promise<void> => {
  return post('/auth/send-code', { account, type })
}

export const getCaptcha = (): Promise<CaptchaResponse> => {
  return get<CaptchaResponse>('/auth/captcha')
}

export const verifyCaptcha = (
  code: string,
  captchaKey: string
): Promise<void> => {
  return post('/auth/verify-captcha', { code, captchaKey })
}

export const checkUsernameExists = (username: string): Promise<CheckExistResponse> => {
  return get<CheckExistResponse>('/auth/check-username', { username })
}

export const checkEmailExists = (email: string): Promise<CheckExistResponse> => {
  return get<CheckExistResponse>('/auth/check-email', { email })
}

export const thirdPartyLogin = (
  type: ThirdPartyLoginType,
  code: string
): Promise<LoginResponse> => {
  return post<LoginResponse>('/auth/third-party', { type, code })
}

export const getThirdPartyAuthUrl = (
  type: ThirdPartyLoginType,
  redirectUri: string
): Promise<{ authUrl: string }> => {
  return get<{ authUrl: string }>('/auth/third-party/url', { type, redirectUri })
}
