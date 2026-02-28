<template>
  <!-- 密码强度指示器组件 -->
  <div class="password-strength">
    <!-- 强度条 -->
    <div class="strength-bar">
      <div
        v-for="i in 3"
        :key="i"
        class="strength-segment"
        :class="{
          'is-active': i <= activeSegments,
          [`is-${strengthLevel}`]: i <= activeSegments
        }"
      />
    </div>
    <!-- 强度文本 -->
    <span
      class="strength-text"
      :class="`is-${strengthLevel}`"
    >
      {{ strengthText }}
    </span>
  </div>
</template>

<script setup lang="ts">
/**
 * 密码强度指示器组件
 * 根据密码复杂度显示弱/中/强三个等级
 */
import { computed } from 'vue'
import { PasswordStrength, type PasswordStrengthResult } from '@/types/auth'

/**
 * 组件属性定义
 */
interface Props {
  /** 密码值 */
  password: string
}

const props = defineProps<Props>()

/**
 * 计算密码强度
 * @param password 密码字符串
 * @returns 密码强度结果
 */
const calculateStrength = (password: string): PasswordStrengthResult => {
  let score = 0
  
  // 长度评分
  if (password.length >= 6) score += 10
  if (password.length >= 10) score += 10
  if (password.length >= 14) score += 10
  
  // 复杂度评分
  if (/[a-z]/.test(password)) score += 15 // 小写字母
  if (/[A-Z]/.test(password)) score += 15 // 大写字母
  if (/\d/.test(password)) score += 15    // 数字
  if (/[^a-zA-Z0-9]/.test(password)) score += 25 // 特殊字符
  
  // 确定强度等级
  let level: PasswordStrength
  let message: string
  
  if (score < 40) {
    level = PasswordStrength.WEAK
    message = '弱'
  } else if (score < 70) {
    level = PasswordStrength.MEDIUM
    message = '中'
  } else {
    level = PasswordStrength.STRONG
    message = '强'
  }
  
  return { level, score, message }
}

/**
 * 密码强度结果
 */
const strengthResult = computed<PasswordStrengthResult>(() => {
  if (!props.password) {
    return {
      level: PasswordStrength.WEAK,
      score: 0,
      message: '弱'
    }
  }
  return calculateStrength(props.password)
})

/**
 * 强度等级
 */
const strengthLevel = computed(() => strengthResult.value.level)

/**
 * 强度文本
 */
const strengthText = computed(() => {
  if (!props.password) return '请输入密码'
  return `强度：${strengthResult.value.message}`
})

/**
 * 激活的段数（1-3）
 */
const activeSegments = computed(() => {
  switch (strengthResult.value.level) {
    case PasswordStrength.WEAK:
      return 1
    case PasswordStrength.MEDIUM:
      return 2
    case PasswordStrength.STRONG:
      return 3
    default:
      return 0
  }
})
</script>

<style scoped lang="scss">
.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.strength-bar {
  display: flex;
  gap: 4px;
  flex: 1;
}

.strength-segment {
  height: 4px;
  flex: 1;
  border-radius: 2px;
  background-color: var(--el-border-color-lighter);
  transition: background-color 0.3s;
  
  &.is-active {
    &.is-weak {
      background-color: var(--el-color-danger);
    }
    
    &.is-medium {
      background-color: var(--el-color-warning);
    }
    
    &.is-strong {
      background-color: var(--el-color-success);
    }
  }
}

.strength-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 60px;
  text-align: right;
  
  &.is-weak {
    color: var(--el-color-danger);
  }
  
  &.is-medium {
    color: var(--el-color-warning);
  }
  
  &.is-strong {
    color: var(--el-color-success);
  }
}
</style>
