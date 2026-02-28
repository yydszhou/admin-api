<template>
  <!-- 密码输入框组件 -->
  <el-input
    v-model="inputValue"
    :type="showPassword ? 'text' : 'password'"
    :placeholder="placeholder"
    :disabled="disabled"
    :size="size"
    :clearable="clearable"
    @blur="handleBlur"
    @focus="handleFocus"
    @keyup.enter="handleEnter"
  >
    <!-- 密码显示/隐藏切换按钮 -->
    <template #suffix>
      <el-icon
        class="password-toggle-icon"
        @click="togglePasswordVisibility"
      >
        <View v-if="showPassword" />
        <Hide v-else />
      </el-icon>
    </template>
  </el-input>
</template>

<script setup lang="ts">
/**
 * 密码输入框组件
 * 支持显示/隐藏切换、自定义占位符、禁用状态等
 */
import { ref, computed } from 'vue'
import { View, Hide } from '@element-plus/icons-vue'

/**
 * 组件属性定义
 */
interface Props {
  /** 绑定值 */
  modelValue: string
  /** 占位符文本 */
  placeholder?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 输入框尺寸 */
  size?: 'large' | 'default' | 'small'
  /** 是否可清空 */
  clearable?: boolean
}

/**
 * 组件事件定义
 */
interface Emits {
  /** 更新绑定值 */
  (e: 'update:modelValue', value: string): void
  /** 失去焦点事件 */
  (e: 'blur'): void
  /** 获得焦点事件 */
  (e: 'focus'): void
  /** 回车键事件 */
  (e: 'enter'): void
}

// 定义属性和事件
const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入密码',
  disabled: false,
  size: 'default',
  clearable: true
})

const emit = defineEmits<Emits>()

/**
 * 是否显示密码明文
 */
const showPassword = ref(false)

/**
 * 输入框值（双向绑定）
 */
const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

/**
 * 切换密码显示/隐藏
 */
const togglePasswordVisibility = (): void => {
  showPassword.value = !showPassword.value
}

/**
 * 处理失去焦点事件
 */
const handleBlur = (): void => {
  emit('blur')
}

/**
 * 处理获得焦点事件
 */
const handleFocus = (): void => {
  emit('focus')
}

/**
 * 处理回车键事件
 */
const handleEnter = (): void => {
  emit('enter')
}
</script>

<style scoped lang="scss">
.password-toggle-icon {
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: color 0.2s;
  
  &:hover {
    color: var(--el-text-color-primary);
  }
}
</style>
