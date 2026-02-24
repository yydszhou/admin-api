/**
 * 主题状态管理 Store
 * 管理暗黑/亮色模式切换
 */

import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'

/**
 * 主题类型
 */
export type Theme = 'light' | 'dark'

/**
 * 主题 Store
 */
export const useThemeStore = defineStore('theme', () => {
  // ==================== State ====================

  /**
   * 当前主题
   * 优先从 localStorage 读取，默认亮色模式
   */
  const currentTheme = ref<Theme>(
    (localStorage.getItem('theme') as Theme) || 'light'
  )

  /**
   * 是否暗黑模式
   */
  const isDark = computed(() => currentTheme.value === 'dark')

  // ==================== Actions ====================

  /**
   * 设置主题
   * @param theme 主题类型
   */
  const setTheme = (theme: Theme) => {
    currentTheme.value = theme
    localStorage.setItem('theme', theme)
    applyTheme(theme)
  }

  /**
   * 切换主题
   */
  const toggleTheme = () => {
    const newTheme = currentTheme.value === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
  }

  /**
   * 应用主题到 DOM
   * @param theme 主题类型
   */
  const applyTheme = (theme: Theme) => {
    const html = document.documentElement
    if (theme === 'dark') {
      html.classList.add('dark')
      html.setAttribute('data-theme', 'dark')
    } else {
      html.classList.remove('dark')
      html.setAttribute('data-theme', 'light')
    }
  }

  /**
   * 初始化主题
   */
  const initTheme = () => {
    applyTheme(currentTheme.value)
  }

  // ==================== Watch ====================

  /**
   * 监听主题变化，自动应用
   */
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  }, { immediate: true })

  // ==================== Return ====================

  return {
    currentTheme,
    isDark,
    setTheme,
    toggleTheme,
    initTheme
  }
})
