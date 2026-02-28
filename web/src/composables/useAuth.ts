/**
 * 认证相关组合式函数
 * 封装登录、注册、验证码等通用逻辑
 */

import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormItemRule } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  sendCode as sendCodeApi,
  checkUsernameExists,
  checkEmailExists
} from '@/api/auth'

/**
 * 验证码倒计时组合式函数
 * @param initialSeconds 初始倒计时秒数，默认 60 秒
 */
export function useCountdown(initialSeconds: number = 60) {
  /** 剩余秒数 */
  const seconds = ref(0)
  
  /** 倒计时定时器 */
  let timer: ReturnType<typeof setInterval> | null = null
  
  /**
   * 是否正在倒计时中
   */
  const isCounting = computed(() => seconds.value > 0)
  
  /**
   * 按钮显示文本
   */
  const buttonText = computed(() => {
    return isCounting.value ? `${seconds.value}s 后重试` : '获取验证码'
  })
  
  /**
   * 开始倒计时
   */
  const start = (): void => {
    // 清除之前的定时器
    if (timer) {
      clearInterval(timer)
    }
    
    seconds.value = initialSeconds
    
    timer = setInterval(() => {
      seconds.value--
      if (seconds.value <= 0) {
        stop()
      }
    }, 1000)
  }
  
  /**
   * 停止倒计时
   */
  const stop = (): void => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    seconds.value = 0
  }
  
  return {
    seconds,
    isCounting,
    buttonText,
    start,
    stop
  }
}

/**
 * 登录逻辑组合式函数
 */
export function useLogin() {
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  
  /** 登录加载状态 */
  const loading = ref(false)
  
  /**
   * 执行登录
   * @param formRef 表单引用
   * @param loginForm 登录表单数据
   */
  const doLogin = async (
    formRef: FormInstance | undefined,
    loginForm: { username: string; password: string; remember: boolean }
  ): Promise<void> => {
    if (!formRef) return
    
    // 表单验证
    const valid = await formRef.validate().catch(() => false)
    if (!valid) return
    
    // 开始登录
    loading.value = true
    
    try {
      const success = await userStore.login(loginForm)
      
      if (success) {
        // 登录成功，跳转到原目标页或首页
        const redirect = route.query.redirect as string
        router.push(redirect || '/')
      }
    } finally {
      loading.value = false
    }
  }
  
  return {
    loading,
    doLogin
  }
}

/**
 * 注册逻辑组合式函数
 */
export function useRegister() {
  const router = useRouter()
  
  /** 注册加载状态 */
  const loading = ref(false)
  
  /**
   * 执行注册
   * @param formRef 表单引用
   * @param registerForm 注册表单数据
   */
  const doRegister = async (
    formRef: FormInstance | undefined,
    registerForm: {
      username: string
      email: string
      phone?: string
      password: string
      confirmPassword: string
      code?: string
      agreement: boolean
    }
  ): Promise<void> => {
    if (!formRef) return
    
    // 表单验证
    const valid = await formRef.validate().catch(() => false)
    if (!valid) return
    
    // 开始注册
    loading.value = true
    
    try {
      const success = await useUserStore().register(registerForm)
      
      if (success) {
        // 注册成功，跳转到登录页
        router.push('/login')
      }
    } finally {
      loading.value = false
    }
  }
  
  return {
    loading,
    doRegister
  }
}

/**
 * 发送验证码逻辑组合式函数
 */
export function useSendCode() {
  const { seconds, isCounting, buttonText, start, stop } = useCountdown(60)
  
  /** 发送验证码加载状态 */
  const sending = ref(false)
  
  /**
   * 发送验证码
   * @param account 邮箱或手机号
   * @param type 验证码类型
   */
  const sendCode = async (
    account: string,
    type: 'email' | 'sms' = 'email'
  ): Promise<void> => {
    // 检查账号格式
    if (!account) {
      ElMessage.warning(type === 'email' ? '请先输入邮箱' : '请先输入手机号')
      return
    }
    
    // 邮箱格式验证
    if (type === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(account)) {
        ElMessage.warning('请输入正确的邮箱格式')
        return
      }
    }
    
    // 手机号格式验证
    if (type === 'sms') {
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(account)) {
        ElMessage.warning('请输入正确的手机号格式')
        return
      }
    }
    
    // 检查是否正在倒计时
    if (isCounting.value) return
    
    sending.value = true
    
    try {
      await sendCodeApi(account, type)
      ElMessage.success('验证码已发送，请查收')
      start()
    } catch {
      // 错误已在请求拦截器中处理
    } finally {
      sending.value = false
    }
  }
  
  return {
    seconds,
    isCounting,
    buttonText,
    sending,
    sendCode,
    stop
  }
}

/**
 * 表单验证规则生成函数
 */
export function useFormRules() {
  /**
   * 用户名验证规则
   */
  const usernameRules: FormItemRule[] = [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: '用户名只能包含字母、数字、下划线和中文',
      trigger: 'blur'
    }
  ]
  
  /**
   * 邮箱验证规则
   */
  const emailRules: FormItemRule[] = [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ]
  
  /**
   * 手机号验证规则
   */
  const phoneRules: FormItemRule[] = [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ]
  
  /**
   * 密码验证规则
   */
  const passwordRules: FormItemRule[] = [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
  ]
  
  /**
   * 验证码验证规则
   */
  const codeRules: FormItemRule[] = [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码应为 6 位数字', trigger: 'blur' }
  ]
  
  /**
   * 创建确认密码验证规则
   * @param passwordGetter 获取密码的函数
   */
  const createConfirmPasswordRules = (
    passwordGetter: () => string
  ): FormItemRule[] => [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordGetter()) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
  
  /**
   * 检查用户名是否已存在
   * @param username 用户名
   */
  const checkUsername = async (username: string): Promise<boolean> => {
    try {
      const { exists } = await checkUsernameExists(username)
      return exists
    } catch {
      return false
    }
  }
  
  /**
   * 检查邮箱是否已存在
   * @param email 邮箱
   */
  const checkEmail = async (email: string): Promise<boolean> => {
    try {
      const { exists } = await checkEmailExists(email)
      return exists
    } catch {
      return false
    }
  }
  
  return {
    usernameRules,
    emailRules,
    phoneRules,
    passwordRules,
    codeRules,
    createConfirmPasswordRules,
    checkUsername,
    checkEmail
  }
}
