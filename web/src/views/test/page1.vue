<template>
  <div class="test-page" :class="{ 'dark-mode': isDark }">
    <h2>测试页面 1</h2>
    <p>这是第一个测试页面，用于验证面包屑和Tab页功能。</p>
    <p>当前路由路径: {{ route.path }}</p>
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <span>页面内容</span>
        </div>
      </template>
      <p>这是一个简单的测试页面，用于验证面包屑导航和Tab页切换功能是否正常工作。</p>
      <p>您可以点击不同的菜单项来观察面包屑和Tab的变化。</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)
</script>

<style scoped lang="scss">
.test-page {
  padding: 20px;

  h2 {
    color: #303133;
    margin-bottom: 16px;
  }

  p {
    color: #606266;
    margin-bottom: 12px;
    line-height: 1.6;
  }

  .content-card {
    margin-top: 20px;
  }
}

.dark-mode {
  .test-page {
    h2 {
      color: #e0e0e0;
    }

    p {
      color: #a6a6a6;
    }
  }
}
</style>
