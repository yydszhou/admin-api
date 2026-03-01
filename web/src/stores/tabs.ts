/**
 * Tab 页管理 Store
 * 管理多标签页的状态、切换、关闭等操作
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { RouteLocationNormalized } from 'vue-router'

/**
 * Tab 项接口
 */
export interface TabItem {
  /** 路由路径 */
  path: string
  /** 页面标题 */
  title: string
  /** 是否可关闭 */
  closable: boolean
  /** 菜单图标 */
  icon?: string
}

/**
 * Tab Store
 */
export const useTabsStore = defineStore('tabs', () => {
  // ==================== State ====================

  /**
   * Tab 列表
   */
  const tabs = ref<TabItem[]>([
    { path: '/', title: '仪表盘', closable: false, icon: 'HomeFilled' }
  ])

  /**
   * 当前激活的 Tab 路径
   */
  const activeTab = ref('/')

  // ==================== Getters ====================

  /**
   * Tab 数量
   */
  const tabCount = computed(() => tabs.value.length)

  /**
   * 获取当前激活的 Tab
   */
  const currentTab = computed(() => {
    return tabs.value.find(tab => tab.path === activeTab.value)
  })

  // ==================== Actions ====================

  /**
   * 添加 Tab
   * @param tab Tab 项
   */
  const addTab = (tab: TabItem) => {
    // 如果已存在则不添加
    const exists = tabs.value.some(item => item.path === tab.path)
    if (!exists) {
      tabs.value.push(tab)
    }
    // 激活该 Tab
    activeTab.value = tab.path
  }

  /**
   * 从路由添加 Tab
   * @param route 路由对象
   */
  const addTabFromRoute = (route: RouteLocationNormalized) => {
    const { path, meta } = route
    const title = (meta.title as string) || '未命名'
    const icon = (meta.icon as string) || 'Document'

    // 首页不可关闭
    const closable = path !== '/'

    addTab({
      path,
      title,
      closable,
      icon
    })
  }

  /**
   * 移除 Tab
   * @param path Tab 路径
   * @returns 下一个要激活的路径
   */
  const removeTab = (path: string): string | null => {
    const index = tabs.value.findIndex(tab => tab.path === path)
    if (index === -1) return null

    const tab = tabs.value[index]
    if (!tab) return null

    // 不能关闭最后一个 Tab
    if (tabs.value.length <= 1) return null

    // 不能关闭不可关闭的 Tab
    if (!tab.closable) return null

    tabs.value.splice(index, 1)

    // 如果关闭的是当前激活的 Tab，需要激活其他 Tab
    if (activeTab.value === path) {
      // 优先激活左边的 Tab
      const newIndex = index > 0 ? index - 1 : 0
      const nextTab = tabs.value[newIndex]
      if (nextTab) {
        activeTab.value = nextTab.path
        return activeTab.value
      }
    }

    return null
  }

  /**
   * 切换 Tab
   * @param path Tab 路径
   */
  const switchTab = (path: string) => {
    const exists = tabs.value.some(tab => tab.path === path)
    if (exists) {
      activeTab.value = path
    }
  }

  /**
   * 关闭其他 Tab
   * @param path 保留的 Tab 路径
   */
  const closeOthers = (path: string) => {
    tabs.value = tabs.value.filter(tab => tab.path === path || !tab.closable)
    activeTab.value = path
  }

  /**
   * 关闭所有可关闭的 Tab
   */
  const closeAll = () => {
    tabs.value = tabs.value.filter(tab => !tab.closable)
    activeTab.value = tabs.value[0]?.path || '/'
  }

  /**
   * 刷新 Tab
   * @param path Tab 路径
   */
  const refreshTab = (path: string) => {
    // 触发刷新事件，由页面组件监听处理
    window.dispatchEvent(new CustomEvent('tab-refresh', { detail: { path } }))
  }

  // ==================== Return ====================

  return {
    tabs,
    activeTab,
    tabCount,
    currentTab,
    addTab,
    addTabFromRoute,
    removeTab,
    switchTab,
    closeOthers,
    closeAll,
    refreshTab
  }
})
