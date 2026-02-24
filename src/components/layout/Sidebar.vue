<template>
  <div class="sidebar-container" :class="{ 'dark-mode': isDark }">
    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      router
      class="sidebar-menu"
    >
      <!-- 递归渲染菜单 -->
      <template v-for="menu in menuList" :key="menu.id">
        <!-- 子菜单 -->
        <el-sub-menu v-if="menu.type === 'submenu' && menu.children" :index="menu.path">
          <template #title>
            <el-icon>
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.id"
            :index="child.path"
          >
            <el-icon>
              <component :is="child.icon" />
            </el-icon>
            <span>{{ child.name }}</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 普通菜单项 -->
        <el-menu-item v-else :index="menu.path">
          <el-icon>
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
/**
 * 左侧侧边栏组件
 * 包含系统导航菜单，支持折叠展开，从远程接口获取菜单数据
 */
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { MenuItem } from '@/types/menu'
import menuData from '@/data/menu.json'
import { useThemeStore } from '@/stores/theme'

/**
 * Props 定义
 */
interface Props {
  isCollapse: boolean
}

defineProps<Props>()

/**
 * 主题 Store
 */
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

/**
 * 当前路由
 */
const route = useRoute()

/**
 * 菜单列表
 */
const menuList = ref<MenuItem[]>([])

/**
 * 当前激活的菜单项
 */
const activeMenu = computed(() => {
  return route.path
})

/**
 * 加载菜单数据
 * 目前从本地 JSON 文件加载，后续可改为从远程 API 获取
 */
const loadMenus = async () => {
  try {
    // 方式1: 从本地 JSON 文件加载（当前使用）
    // 使用类型断言确保 JSON 数据类型正确
    menuList.value = menuData.menus as MenuItem[]

    // 方式2: 从远程 API 加载（后续使用）
    // const response = await fetch('/api/menus')
    // const data = await response.json()
    // menuList.value = data.menus as MenuItem[]
  } catch (error) {
    console.error('加载菜单失败:', error)
    menuList.value = []
  }
}

/**
 * 组件挂载时加载菜单
 */
onMounted(() => {
  loadMenus()
})
</script>

<style scoped lang="scss">
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #001529;
  transition: background-color 0.3s;

  // 亮色模式样式
  .sidebar-menu {
    background-color: #001529;
    border-right: none;
    flex: 1;

    // 菜单文字颜色 - 亮色模式
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #a6adb4;
    }

    // 激活菜单项样式 - 亮色模式
    :deep(.el-menu-item.is-active) {
      background-color: #1677ff !important;
      color: #fff !important;
    }

    // 子菜单标题悬停 - 亮色模式
    :deep(.el-sub-menu__title) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
    }

    // 菜单项悬停 - 亮色模式
    :deep(.el-menu-item) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
        color: #fff !important;
      }
    }

    // 子菜单背景 - 亮色模式
    :deep(.el-sub-menu) {
      .el-menu {
        background-color: #000c17 !important;
      }
    }

    // 折叠时的样式
    :deep(.el-menu--collapse) {
      .el-sub-menu__title {
        padding: 0 16px;
      }
    }
  }

  // 暗黑模式样式
  &.dark-mode {
    background-color: #141414;

    .sidebar-menu {
      background-color: #141414;

      // 菜单文字颜色 - 暗黑模式
      :deep(.el-menu-item),
      :deep(.el-sub-menu__title) {
        color: #a6a6a6;
      }

      // 激活菜单项样式 - 暗黑模式
      :deep(.el-menu-item.is-active) {
        background-color: #1677ff !important;
        color: #fff !important;
      }

      // 子菜单标题悬停 - 暗黑模式
      :deep(.el-sub-menu__title) {
        &:hover {
          background-color: rgba(255, 255, 255, 0.08) !important;
          color: #fff !important;
        }
      }

      // 菜单项悬停 - 暗黑模式
      :deep(.el-menu-item) {
        &:hover {
          background-color: rgba(255, 255, 255, 0.08) !important;
          color: #fff !important;
        }
      }

      // 子菜单背景 - 暗黑模式
      :deep(.el-sub-menu) {
        .el-menu {
          background-color: #0d0d0d !important;
        }
      }
    }
  }
}
</style>
