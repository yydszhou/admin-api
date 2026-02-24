<template>
  <el-container class="layout-container">
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
 */
import { ref } from 'vue'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'

/**
 * 侧边栏折叠状态
 */
const isCollapse = ref(false)
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100vw;
}

.layout-sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.layout-main-container {
  background-color: #f0f2f5;
  flex-direction: column;
}

.layout-header {
  padding: 0;
  height: 60px;
  background-color: #1677ff;
}

.layout-main {
  padding: 20px;
  overflow-y: auto;
}
</style>
