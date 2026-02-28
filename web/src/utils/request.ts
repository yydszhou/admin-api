import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type AxiosError,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp?: string
  traceId?: string
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const getToken = (): string | null => {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

const clearAuth = (): void => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('userInfo')
}

const createTraceId = (): string => {
  return `trace-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (!config.headers['X-Trace-Id']) {
      config.headers['X-Trace-Id'] = createTraceId()
    }
    return config
  },
  (error: AxiosError) => {
    throw error
  }
)

request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const body = response.data
    if (body.code === 200 || body.code === 0) {
      return body.data
    }
    ElMessage.error(body.message || '请求失败')
    throw new Error(body.message || '请求失败')
  },
  (error: AxiosError<ApiResponse>) => {
    const { response } = error
    if (response) {
      const { status, data } = response
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          clearAuth()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error(data?.message || '没有权限执行此操作')
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
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    throw error
  }
)

export const get = <T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.get(url, { params, ...config }) as unknown as Promise<T>
}

export const post = <T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.post(url, data, config) as unknown as Promise<T>
}

export const put = <T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.put(url, data, config) as unknown as Promise<T>
}

export const del = <T = unknown>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> => {
  return request.delete(url, config) as unknown as Promise<T>
}

export default request
