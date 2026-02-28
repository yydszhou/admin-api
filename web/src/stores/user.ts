/**
 * 用户状态管理 Store
 * 使用 Pinia 管理用户登录状态、Token、用户信息
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import type {
  User,
  LoginForm,
  RegisterForm,
  LoginResponse
} from '@/types/auth'
import {
  login as loginApi,
  register as registerApi,
  logout as logoutApi,
  getUserInfo as getUserInfoApi
} from '@/api/auth'

/**
 * 计算 SHA256 哈希值
 * @param message 要哈希的字符串
 * @returns SHA256 哈希值（十六进制字符串）
 */
const sha256 = async (message: string): Promise<string> => {
  const encoder = new TextEncoder()
  const data = encoder.encode(message)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 用户 Store
 */
export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  
  /** 当前用户信息 */
  const userInfo = ref<User | null>(null)
  
  /** 访问令牌 */
  const token = ref<string>('')
  
  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  
  // ==================== Getters ====================
  
  /**
   * 获取用户ID
   */
  const userId = computed(() => userInfo.value?.id)
  
  /**
   * 获取用户名
   */
  const username = computed(() => userInfo.value?.username)
  
  /**
   * 获取用户角色
   */
  const userRole = computed(() => userInfo.value?.role)
  
  /**
   * 获取用户头像
   */
  const avatar = computed(() => userInfo.value?.avatar)
  
  // ==================== Actions ====================
  
  /**
   * 设置 Token
   * @param newToken 新的 Token
   * @param remember 是否记住登录（持久化到 localStorage）
   */
  const setToken = (newToken: string, remember: boolean = false): void => {
    token.value = newToken
    if (remember) {
      localStorage.setItem('token', newToken)
    } else {
      sessionStorage.setItem('token', newToken)
    }
  }
  
  /**
   * 设置用户信息
   * @param user 用户信息
   * @param remember 是否记住登录
   */
  const setUserInfo = (user: User, remember: boolean = false): void => {
    userInfo.value = user
    const storage = remember ? localStorage : sessionStorage
    storage.setItem('userInfo', JSON.stringify(user))
  }
  
  /**
   * 从本地存储恢复登录状态
   * 页面刷新后调用，恢复用户的登录状态
   */
  const restoreLoginState = (): boolean => {
    // 优先从 localStorage 获取（remember me）
    let storedToken = localStorage.getItem('token')
    let storedUserInfo = localStorage.getItem('userInfo')
    
    // 如果没有，尝试从 sessionStorage 获取
    if (!storedToken) {
      storedToken = sessionStorage.getItem('token')
      storedUserInfo = sessionStorage.getItem('userInfo')
    }
    
    if (storedToken && storedUserInfo) {
      token.value = storedToken
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        return true
      } catch {
        // 解析失败，清除存储
        clearAuth()
        return false
      }
    }
    
    return false
  }
  
  /**
   * 清除登录状态
   */
  const clearAuth = (): void => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }
  
  /**
   * 用户登录
   * @param loginForm 登录表单数据
   * @returns 登录是否成功
   */
  const login = async (loginForm: LoginForm): Promise<boolean> => {
    try {
      // 对密码进行 SHA256 加密
      const hashedPassword = await sha256(loginForm.password)
      const response: LoginResponse = await loginApi({
        ...loginForm,
        password: hashedPassword
      })
      
      // 保存 Token 和用户信息
      setToken(response.token, loginForm.remember)
      setUserInfo(response.userInfo, loginForm.remember)
      
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      // 错误已在请求拦截器中处理
      return false
    }
  }
  
  /**
   * 用户注册
   * @param registerForm 注册表单数据
   * @returns 注册是否成功
   */
  const register = async (registerForm: RegisterForm): Promise<boolean> => {
    try {
      await registerApi(registerForm)
      ElMessage.success('注册成功，请登录')
      return true
    } catch (error) {
      // 错误已在请求拦截器中处理
      return false
    }
  }
  
  /**
   * 用户登出
   * @param showMessage 是否显示提示消息
   */
  const logout = async (showMessage: boolean = true): Promise<void> => {
    try {
      // 调用登出 API（可选，用于服务端清理会话）
      if (token.value) {
        await logoutApi()
      }
    } catch {
      // 忽略 API 错误
    } finally {
      // 清除本地登录状态
      clearAuth()
      if (showMessage) {
        ElMessage.success('已退出登录')
      }
    }
  }
  
  /**
   * 获取并更新用户信息
   * 用于登录后或页面刷新时获取最新用户信息
   */
  const fetchUserInfo = async (): Promise<boolean> => {
    try {
      const user = await getUserInfoApi()
      // 保持原有的存储方式
      const remember = !!localStorage.getItem('token')
      setUserInfo(user, remember)
      return true
    } catch {
      // 获取失败，可能是 Token 过期
      clearAuth()
      return false
    }
  }
  
  /**
   * 检查并恢复登录状态
   * 应用启动时调用
   */
  const checkLogin = async (): Promise<boolean> => {
    const restored = restoreLoginState()
    if (restored) {
      // 恢复成功，尝试获取最新用户信息
      return await fetchUserInfo()
    }
    return false
  }
  
  // ==================== Return ====================
  
  return {
    // State
    userInfo,
    token,
    isLoggedIn,
    
    // Getters
    userId,
    username,
    userRole,
    avatar,
    
    // Actions
    login,
    register,
    logout,
    setToken,
    setUserInfo,
    clearAuth,
    restoreLoginState,
    fetchUserInfo,
    checkLogin
  }
})
