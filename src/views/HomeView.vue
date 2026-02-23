<script setup lang="ts">
/**
 * 首页
 * 登录后的主页面，展示用户信息和登出功能
 */
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/**
 * 处理登出
 */
const handleLogout = async (): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}

/**
 * 页面加载时检查登录状态
 */
onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<template>
  <div class="home-container">
    <header class="home-header">
      <div class="header-left">
        <h1>Admin UI</h1>
      </div>
      <div class="header-right">
        <template v-if="userStore.userInfo">
          <el-avatar :size="32" :src="userStore.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.username }}</span>
        </template>
        <el-button type="danger" size="small" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </header>
    
    <main class="home-main">
      <div class="welcome-card">
        <h2>欢迎使用 Admin UI</h2>
        <p>这是一个基于 Vue 3 + Element Plus 的企业级管理后台系统</p>
        
        <div class="user-info" v-if="userStore.userInfo">
          <h3>当前登录用户信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">
              {{ userStore.userInfo.id }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ userStore.userInfo.username }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userStore.userInfo.email }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag>{{ userStore.userInfo.role }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  h1 {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-color-primary);
    margin: 0;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .username {
    font-size: 14px;
    color: var(--el-text-color-primary);
  }
}

.home-main {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 0 0 32px;
  }
}

.user-info {
  h3 {
    font-size: 16px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin: 0 0 16px;
  }
}
</style>
