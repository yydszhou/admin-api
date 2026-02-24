/**
 * HTTP 请求工具封装
 * 基于 axios 封装，包含请求/响应拦截器、Token 管理、错误处理
 */

import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type AxiosError,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/auth'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  // API 基础地址，根据实际项目配置
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // 请求超时时间（毫秒）
  timeout: 10000,
  // 请求头配置
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 获取存储的 Token
 * 优先从 localStorage 获取，支持 remember me 功能
 */
const getToken = (): string | null => {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

/**
 * 清除登录状态
 */
const clearAuth = (): void => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('userInfo')
}

/**
 * 请求拦截器
 * 在请求发送前统一处理，如添加 Token
 */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 获取 Token 并添加到请求头
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    // 请求错误处理
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理响应数据和错误
 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    
    // 根据业务状态码处理
    if (data.code === 200 || data.code === 0) {
      return response
    }
    
    // 业务错误处理
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error: AxiosError<ApiResponse>) => {
    // HTTP 错误处理
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          // 未授权，清除登录状态并跳转到登录页
          ElMessage.error('登录已过期，请重新登录')
          clearAuth()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求发送成功但没有收到响应
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

/**
 * 封装 GET 请求
 * @param url 请求地址
 * @param params 请求参数
 * @param config 额外配置
 */
export const get = <T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.get(url, { params, ...config })
}

/**
 * 封装 POST 请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 额外配置
 */
export const post = <T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.post(url, data, config)
}

/**
 * 封装 PUT 请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 额外配置
 */
export const put = <T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.put(url, data, config)
}

/**
 * 封装 DELETE 请求
 * @param url 请求地址
 * @param config 额外配置
 */
export const del = <T = unknown>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.delete(url, config)
}

export default request
