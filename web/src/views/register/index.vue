<template>
  <!-- 注册页面 -->
  <div class="auth-container">
    <div class="auth-card">
      <!-- 头部：Logo 和标题 -->
      <div class="auth-header">
        <div class="auth-logo">
          <el-icon><Monitor /></el-icon>
        </div>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">填写以下信息完成注册</p>
      </div>
      
      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="formRules"
        class="auth-form"
        @keyup.enter="handleRegister"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20 个字符）"
            size="large"
            clearable
            :prefix-icon="User"
            @blur="checkUsernameExists"
          />
        </el-form-item>
        
        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            clearable
            :prefix-icon="Message"
            @blur="checkEmailExists"
          />
        </el-form-item>
        
        <!-- 手机号（可选） -->
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（可选）"
            size="large"
            clearable
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <!-- 密码 -->
        <el-form-item prop="password">
          <PasswordInput
            v-model="registerForm.password"
            placeholder="请输入密码（6-20 个字符）"
            size="large"
          />
        </el-form-item>
        
        <!-- 密码强度提示 -->
        <PasswordStrength :password="registerForm.password" />
        
        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword" style="margin-top: 16px;">
          <PasswordInput
            v-model="registerForm.confirmPassword"
            placeholder="请确认密码"
            size="large"
            @enter="handleRegister"
          />
        </el-form-item>
        
        <!-- 验证码（可选） -->
        <el-form-item prop="code">
          <div class="code-input-group">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入验证码"
              size="large"
              maxlength="6"
            />
            <el-button
              :disabled="isCounting"
              :loading="sending"
              class="code-btn"
              @click="handleSendCode"
            >
              {{ buttonText }}
            </el-button>
          </div>
        </el-form-item>
        
        <!-- 用户协议 -->
        <el-form-item prop="agreement" class="agreement-item">
          <el-checkbox v-model="registerForm.agreement">
            <span class="agreement-text">
              我已阅读并同意
              <a href="javascript:void(0)" @click="showAgreement">《用户协议》</a>
              和
              <a href="javascript:void(0)" @click="showPrivacy">《隐私政策》</a>
            </span>
          </el-checkbox>
        </el-form-item>
        
        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 底部：登录链接 -->
      <div class="auth-footer">
        <span class="footer-text">已有账号？</span>
        <router-link to="/login" class="footer-link">
          立即登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 注册页面
 * 包含用户名、邮箱、手机号、密码、验证码等注册信息填写
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Message, Phone, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PasswordInput from '@/components/PasswordInput.vue'
import PasswordStrength from '@/components/PasswordStrength.vue'
import { useUserStore } from '@/stores/user'
import { checkUsernameExists as checkUsernameApi, checkEmailExists as checkEmailApi } from '@/api/auth'
import { useSendCode } from '@/composables/useAuth'

// ==================== 路由和状态 ====================

const router = useRouter()
const userStore = useUserStore()

// ==================== 表单数据 ====================

/**
 * 表单引用
 */
const formRef = ref<FormInstance>()

/**
 * 注册表单数据
 */
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  code: '',
  agreement: false
})

/**
 * 注册加载状态
 */
const loading = ref(false)

// ==================== 验证码相关 ====================

const { seconds, isCounting, buttonText, sending, sendCode } = useSendCode()

/**
 * 发送验证码
 */
const handleSendCode = (): void => {
  // 优先使用邮箱发送验证码
  const account = registerForm.email || registerForm.phone
  const type = registerForm.email ? 'email' : 'sms'
  sendCode(account, type)
}

// ==================== 表单验证规则 ====================

/**
 * 检查用户名是否已存在
 */
const checkUsernameExists = async (): Promise<void> => {
  if (!registerForm.username || registerForm.username.length < 3) return
  
  try {
    const { exists } = await checkUsernameApi(registerForm.username)
    if (exists) {
      ElMessage.warning('该用户名已被注册')
    }
  } catch {
    // 忽略错误
  }
}

/**
 * 检查邮箱是否已存在
 */
const checkEmailExists = async (): Promise<void> => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!registerForm.email || !emailRegex.test(registerForm.email)) return
  
  try {
    const { exists } = await checkEmailApi(registerForm.email)
    if (exists) {
      ElMessage.warning('该邮箱已被注册')
    }
  } catch {
    // 忽略错误
  }
}

/**
 * 表单验证规则
 */
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: '用户名只能包含字母、数字、下划线和中文',
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// ==================== 方法 ====================

/**
 * 处理注册
 */
const handleRegister = async (): Promise<void> => {
  if (!formRef.value) return
  
  // 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 开始注册
  loading.value = true
  
  try {
    const success = await userStore.register(registerForm)
    
    if (success) {
      // 注册成功，跳转到登录页
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 显示用户协议
 */
const showAgreement = (): void => {
  ElMessage.info('用户协议功能开发中')
}

/**
 * 显示隐私政策
 */
const showPrivacy = (): void => {
  ElMessage.info('隐私政策功能开发中')
}

// ==================== 生命周期 ====================

/**
 * 页面加载时自动聚焦到用户名输入框
 */
onMounted(() => {
  setTimeout(() => {
    const input = document.querySelector('input[placeholder^="请输入用户名"]') as HTMLInputElement
    input?.focus()
  }, 100)
})
</script>

<style scoped lang="scss">
// 导入认证页面通用样式
@use '@/styles/auth.scss' as *;
</style>
