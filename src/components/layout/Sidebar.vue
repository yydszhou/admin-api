<template>
  <div class="sidebar-container">
    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="#001529"
      text-color="#a6adb4"
      active-text-color="#fff"
      router
      class="sidebar-menu"
    >
      <!-- 系统菜单（可展开） -->
      <el-sub-menu index="/system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统</span>
        </template>
        <el-menu-item index="/system/settings">
          <el-icon><Tools /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
        <el-menu-item index="/system/table">
          <el-icon><Grid /></el-icon>
          <span>数据表格</span>
        </el-menu-item>
        <el-menu-item index="/system/password">
          <el-icon><Lock /></el-icon>
          <span>密码修改</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 数据库菜单（可展开） -->
      <el-sub-menu index="/database">
        <template #title>
          <el-icon><Coin /></el-icon>
          <span>数据库</span>
        </template>
        <el-menu-item index="/database/mysql">
          <el-icon><DataAnalysis /></el-icon>
          <span>MySQL</span>
        </el-menu-item>
        <el-menu-item index="/database/redis">
          <el-icon><DataLine /></el-icon>
          <span>Redis</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 数据菜单 -->
      <el-menu-item index="/data">
        <el-icon><DataBoard /></el-icon>
        <span>数据</span>
      </el-menu-item>

      <!-- 模板菜单 -->
      <el-menu-item index="/template">
        <el-icon><DocumentCopy /></el-icon>
        <span>模板</span>
      </el-menu-item>

      <!-- 其他菜单 -->
      <el-menu-item index="/other">
        <el-icon><MoreFilled /></el-icon>
        <span>其他</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
/**
 * 左侧侧边栏组件
 * 包含系统导航菜单，支持折叠展开
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'

/**
 * Props 定义
 */
interface Props {
  isCollapse: boolean
}

defineProps<Props>()

/**
 * 当前路由
 */
const route = useRoute()

/**
 * 当前激活的菜单项
 */
const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped lang="scss">
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  border-right: none;
  flex: 1;

  // 激活菜单项样式
  :deep(.el-menu-item.is-active) {
    background-color: #1677ff !important;
  }

  // 子菜单标题样式
  :deep(.el-sub-menu__title) {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }
  }

  // 菜单项悬停样式
  :deep(.el-menu-item) {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }
  }

  // 折叠时的样式调整
  :deep(.el-menu--collapse) {
    .el-sub-menu__title {
      padding: 0 16px;
    }
  }
}
</style>
