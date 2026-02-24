<template>
  <el-container class="layout-container" :class="{ 'dark-mode': isDark }">
    <!-- 左侧侧边栏 -->
    <el-aside
      class="layout-sidebar"
      :width="isCollapse ? '64px' : '200px'"
    >
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container class="layout-main-container">
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <Header v-model:collapse="isCollapse" />
      </el-header>

      <!-- Tab 栏 -->
      <Tabs />

      <!-- 面包屑导航 -->
      <Breadcrumb />

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
/**
 * 后台管理系统主布局组件
 * 采用经典的三栏布局：左侧侧边栏 + 顶部导航栏 + 主内容区
 * 支持暗黑/亮色模式切换，包含 Tab 页管理和面包屑导航
 */
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
import Tabs from './Tabs.vue'
import Breadcrumb from './Breadcrumb.vue'

/**
 * 主题 Store
 */
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

/**
 * 侧边栏折叠状态
 */
const isCollapse = ref(false)

/**
 * 组件挂载时初始化主题
 */
onMounted(() => {
  themeStore.initTheme()
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100vw;

  &.dark-mode {
    .layout-main-container {
      background-color: #141414;
    }

    .layout-main {
      background-color: #141414;
      color: #e0e0e0;
    }
  }
}

.layout-sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.layout-main-container {
  background-color: #f0f2f5;
  flex-direction: column;
  transition: background-color 0.3s;
}

.layout-header {
  padding: 0;
  height: 60px;
  background-color: #1677ff;
}

.layout-main {
  padding: 20px;
  overflow-y: auto;
  transition: background-color 0.3s, color 0.3s;
}
</style>
