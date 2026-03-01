<template>
  <!-- 忘记密码页面 -->
  <div class="auth-container">
    <div class="auth-card">
      <!-- 头部：Logo 和标题 -->
      <div class="auth-header">
        <div class="auth-logo">
          <el-icon><Monitor /></el-icon>
        </div>
        <h1 class="auth-title">重置密码</h1>
        <p class="auth-subtitle">验证身份后设置新密码</p>
      </div>
      
      <!-- 忘记密码表单 -->
      <el-form
        ref="formRef"
        :model="forgotForm"
        :rules="formRules"
        class="auth-form"
        @keyup.enter="handleReset"
      >
        <!-- 邮箱/手机号 -->
        <el-form-item prop="account">
          <el-input
            v-model="forgotForm.account"
            placeholder="请输入邮箱或手机号"
            size="large"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        
        <!-- 验证码 -->
        <el-form-item prop="code">
          <div class="code-input-group">
            <el-input
              v-model="forgotForm.code"
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
        
        <!-- 新密码 -->
        <el-form-item prop="newPassword">
          <PasswordInput
            v-model="forgotForm.newPassword"
            placeholder="请输入新密码（6-20 个字符）"
            size="large"
          />
        </el-form-item>
        
        <!-- 确认新密码 -->
        <el-form-item prop="confirmPassword">
          <PasswordInput
            v-model="forgotForm.confirmPassword"
            placeholder="请确认新密码"
            size="large"
            @enter="handleReset"
          />
        </el-form-item>
        
        <!-- 重置密码按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleReset"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 底部：返回登录 -->
      <div class="auth-footer">
        <span class="footer-text">想起密码了？</span>
        <router-link to="/login" class="footer-link">
          返回登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 忘记密码页面
 * 包含身份验证（邮箱/手机 + 验证码）和密码重置功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PasswordInput from '@/components/PasswordInput.vue'
import { resetPassword as resetPasswordApi } from '@/api/auth'
import { useSendCode } from '@/composables/useAuth'

// ==================== 路由 ====================

const router = useRouter()

// ==================== 表单数据 ====================

/**
 * 表单引用
 */
const formRef = ref<FormInstance>()

/**
 * 忘记密码表单数据
 */
const forgotForm = reactive({
  account: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

/**
 * 重置密码加载状态
 */
const loading = ref(false)

// ==================== 验证码相关 ====================

const { seconds, isCounting, buttonText, sending, sendCode } = useSendCode()

/**
 * 判断账号类型（邮箱或手机号）
 */
const accountType = computed((): 'email' | 'sms' | null => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const phoneRegex = /^1[3-9]\d{9}$/
  
  if (emailRegex.test(forgotForm.account)) {
    return 'email'
  } else if (phoneRegex.test(forgotForm.account)) {
    return 'sms'
  }
  return null
})

/**
 * 发送验证码
 */
const handleSendCode = (): void => {
  if (!accountType.value) {
    ElMessage.warning('请输入正确的邮箱或手机号')
    return
  }
  
  sendCode(forgotForm.account, accountType.value)
}

// ==================== 表单验证规则 ====================

/**
 * 表单验证规则
 */
const formRules: FormRules = {
  account: [
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        const phoneRegex = /^1[3-9]\d{9}$/
        
        if (!emailRegex.test(value) && !phoneRegex.test(value)) {
          callback(new Error('请输入正确的邮箱或手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码应为 6 位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// ==================== 方法 ====================

/**
 * 处理重置密码
 */
const handleReset = async (): Promise<void> => {
  if (!formRef.value) return
  
  // 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 开始重置密码
  loading.value = true
  
  try {
    await resetPasswordApi({
      account: forgotForm.account,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword,
      confirmPassword: forgotForm.confirmPassword
    })
    
    ElMessage.success('密码重置成功，请使用新密码登录')
    // 跳转到登录页
    router.push('/login')
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

/**
 * 页面加载时自动聚焦到账号输入框
 */
onMounted(() => {
  setTimeout(() => {
    const input = document.querySelector('input[placeholder="请输入邮箱或手机号"]') as HTMLInputElement
    input?.focus()
  }, 100)
})
</script>

<style scoped lang="scss">
// 导入认证页面通用样式
@use '@/styles/auth.scss' as *;
</style>
