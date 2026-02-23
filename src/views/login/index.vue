<template>
  <!-- 登录页面 -->
  <div class="auth-container">
    <div class="auth-card">
      <!-- 头部：Logo 和标题 -->
      <div class="auth-header">
        <div class="auth-logo">
          <el-icon><Monitor /></el-icon>
        </div>
        <h1 class="auth-title">Admin UI</h1>
        <p class="auth-subtitle">企业级管理后台系统</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="formRules"
        class="auth-form"
      >
        <!-- 用户名/邮箱 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        
        <!-- 密码 -->
        <el-form-item prop="password">
          <PasswordInput
            v-model="loginForm.password"
            placeholder="请输入密码"
            size="large"
            @enter="handleLogin"
          />
        </el-form-item>
        
        <!-- 记住我 & 忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="loginForm.remember">
            记住我
          </el-checkbox>
          <router-link to="/forgot-password" class="forgot-link">
            忘记密码？
          </router-link>
        </div>
        
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 第三方登录 -->
      <el-divider class="auth-divider">
        <span>其他登录方式</span>
      </el-divider>
      
      <div class="social-login">
        <div class="social-btn" title="微信登录" @click="handleWechatLogin">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="social-btn" title="GitHub 登录" @click="handleGithubLogin">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path
              fill="currentColor"
              d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"
            />
          </svg>
        </div>
      </div>
      
      <!-- 底部：注册链接 -->
      <div class="auth-footer">
        <span class="footer-text">还没有账号？</span>
        <router-link to="/register" class="footer-link">
          立即注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 登录页面
 * 包含用户名/邮箱登录、记住我、忘记密码、第三方登录等功能
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Monitor, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PasswordInput from '@/components/PasswordInput.vue'
import { useUserStore } from '@/stores/user'
import { ThirdPartyLoginType } from '@/types/auth'
import { getThirdPartyAuthUrl } from '@/api/auth'

// ==================== 路由和状态 ====================

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ==================== 表单数据 ====================

/**
 * 表单引用
 */
const formRef = ref<FormInstance>()

/**
 * 登录表单数据
 */
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

/**
 * 登录加载状态
 */
const loading = ref(false)

// ==================== 表单验证规则 ====================

/**
 * 表单验证规则
 */
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

// ==================== 方法 ====================

/**
 * 处理登录
 */
const handleLogin = async (): Promise<void> => {
  if (!formRef.value) return
  
  // 表单验证
  const valid = await formRef.value.validate().catch(() => false)
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

/**
 * 处理微信登录
 */
const handleWechatLogin = async (): Promise<void> => {
  try {
    const { authUrl } = await getThirdPartyAuthUrl(
      ThirdPartyLoginType.WECHAT,
      window.location.origin + '/auth/callback'
    )
    // 跳转到微信授权页
    window.location.href = authUrl
  } catch {
    ElMessage.warning('微信登录暂时不可用')
  }
}

/**
 * 处理 GitHub 登录
 */
const handleGithubLogin = async (): Promise<void> => {
  try {
    const { authUrl } = await getThirdPartyAuthUrl(
      ThirdPartyLoginType.GITHUB,
      window.location.origin + '/auth/callback'
    )
    // 跳转到 GitHub 授权页
    window.location.href = authUrl
  } catch {
    ElMessage.warning('GitHub 登录暂时不可用')
  }
}

// ==================== 生命周期 ====================

/**
 * 页面加载时自动聚焦到用户名输入框
 */
onMounted(() => {
  // 延迟执行，确保 DOM 已渲染
  setTimeout(() => {
    const input = document.querySelector('input[placeholder="请输入用户名或邮箱"]') as HTMLInputElement
    input?.focus()
  }, 100)
})
</script>

<style scoped lang="scss">
// 导入认证页面通用样式
@use '@/styles/auth.scss' as *;
</style>
