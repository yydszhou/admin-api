<template>
  <div class="header-container">
    <!-- 左侧区域：Logo 和系统名称 -->
    <div class="header-left">
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon :size="20" color="#fff">
          <Fold v-if="!collapse" />
          <Expand v-else />
        </el-icon>
      </div>
      <div class="logo">
        <el-icon :size="28" color="#fff"><Monitor /></el-icon>
        <span class="system-name">uimaker 后台管理系统</span>
      </div>
    </div>

    <!-- 中间区域 -->
    <div class="header-center">
      <el-tooltip content="首页" placement="bottom">
        <div class="header-icon-btn" @click="goHome">
          <el-icon :size="18"><HomeFilled /></el-icon>
        </div>
      </el-tooltip>

      <el-dropdown trigger="click">
        <div class="header-icon-btn">
          <el-icon :size="18"><Setting /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>系统设置</el-dropdown-item>
            <el-dropdown-item>个人设置</el-dropdown-item>
            <el-dropdown-item>安全设置</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-tooltip content="消息通知" placement="bottom">
        <div class="header-icon-btn">
          <el-badge :value="3" class="message-badge">
            <el-icon :size="18"><ChatDotRound /></el-icon>
          </el-badge>
        </div>
      </el-tooltip>

      <el-tooltip content="刷新页面" placement="bottom">
        <div class="header-icon-btn" @click="refreshPage">
          <el-icon :size="18"><Refresh /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <!-- 右侧区域 -->
    <div class="header-right">
      <el-tooltip content="全屏" placement="bottom">
        <div class="header-icon-btn" @click="toggleFullscreen">
          <el-icon :size="18"><FullScreen /></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip content="收藏" placement="bottom">
        <div class="header-icon-btn">
          <el-icon :size="18"><Star /></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip content="主题切换" placement="bottom">
        <div class="header-icon-btn" @click="toggleTheme">
          <el-icon :size="18"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip content="通知" placement="bottom">
        <div class="header-icon-btn">
          <el-icon :size="18"><Bell /></el-icon>
        </div>
      </el-tooltip>

      <!-- 用户头像下拉菜单 -->
      <el-dropdown trigger="click" class="user-dropdown">
        <div class="user-info">
          <el-avatar
            :size="32"
            src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
          />
          <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>个人中心</el-dropdown-item>
            <el-dropdown-item>修改密码</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 顶部导航栏组件
 * 包含 Logo、系统名称、快捷操作、用户头像等
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/**
 * Props 定义
 */
interface Props {
  collapse: boolean
}

const props = defineProps<Props>()

/**
 * Emits 定义
 */
const emit = defineEmits<{
  'update:collapse': [value: boolean]
}>()

/**
 * 暗黑模式状态
 */
const isDark = ref(false)

/**
 * 切换侧边栏折叠状态
 */
const toggleCollapse = () => {
  emit('update:collapse', !props.collapse)
}

/**
 * 返回首页
 */
const goHome = () => {
  router.push('/')
}

/**
 * 刷新页面
 */
const refreshPage = () => {
  location.reload()
}

/**
 * 切换全屏
 */
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

/**
 * 切换主题
 */
const toggleTheme = () => {
  isDark.value = !isDark.value
  ElMessage.success(isDark.value ? '已切换到暗黑模式' : '已切换到亮色模式')
}

/**
 * 退出登录
 */
const logout = async () => {
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
</script>

<style scoped lang="scss">
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
  background-color: #1677ff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;

  .system-name {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
}

.header-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #fff;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.message-badge {
  :deep(.el-badge__content) {
    background-color: #ff4d4f;
  }
}

.user-dropdown {
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  .dropdown-arrow {
    color: #fff;
    font-size: 12px;
  }
}
</style>
