<template>
  <div class="breadcrumb-container" :class="{ 'dark-mode': isDark }">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbs"
        :key="item.path"
        :to="index < breadcrumbs.length - 1 ? item.path : undefined"
      >
        <el-icon v-if="item.icon" class="breadcrumb-icon">
          <component :is="item.icon" />
        </el-icon>
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
/**
 * 面包屑导航组件
 * 根据当前路由自动生成面包屑
 */
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'
import menuData from '@/data/menu.json'
import type { MenuItem } from '@/types/menu'

const route = useRoute()
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

/**
 * 面包屑项接口
 */
interface BreadcrumbItem {
  path: string
  title: string
  icon?: string
}

/**
 * 查找菜单项
 */
const findMenuItem = (menus: any[], path: string): any | null => {
  for (const menu of menus) {
    if (menu.path === path) {
      return menu
    }
    if (menu.children) {
      const found = findMenuItem(menu.children, path)
      if (found) return found
    }
  }
  return null
}

/**
 * 查找父菜单
 */
const findParentMenu = (menus: any[], childPath: string): any | null => {
  for (const menu of menus) {
    if (menu.children) {
      for (const child of menu.children) {
        if (child.path === childPath) {
          return menu
        }
      }
      const found = findParentMenu(menu.children, childPath)
      if (found) return found
    }
  }
  return null
}

/**
 * 面包屑列表
 */
const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const path = route.path
  const result: BreadcrumbItem[] = []

  // 首页
  if (path === '/') {
    result.push({ path: '/', title: '首页', icon: 'HomeFilled' })
    return result
  }

  // 查找当前菜单
  const currentMenu = findMenuItem(menuData.menus, path)
  if (!currentMenu) {
    // 如果找不到，使用路由元数据
    result.push({ path: '/', title: '首页', icon: 'HomeFilled' })
    if (route.meta.title) {
      result.push({
        path: route.path,
        title: route.meta.title as string,
        icon: (route.meta.icon as string) || 'Document'
      })
    }
    return result
  }

  // 查找父菜单
  const parentMenu = findParentMenu(menuData.menus, path)

  // 构建面包屑
  result.push({ path: '/', title: '首页', icon: 'HomeFilled' })

  if (parentMenu) {
    result.push({
      path: parentMenu.path,
      title: parentMenu.name,
      icon: parentMenu.icon
    })
  }

  result.push({
    path: currentMenu.path,
    title: currentMenu.name,
    icon: currentMenu.icon
  })

  return result
})

/**
 * 监听面包屑变化，更新页面标题
 */
watch(
  breadcrumbs,
  (newBreadcrumbs) => {
    if (newBreadcrumbs.length > 0) {
      const last = newBreadcrumbs[newBreadcrumbs.length - 1]
      if (last) {
        document.title = `${last.title} - Admin UI`
      }
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.breadcrumb-container {
  background-color: #fff;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;

  :deep(.el-breadcrumb) {
    line-height: 1;

    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #606266;
        font-size: 14px;

        &.is-link {
          color: #1677ff;
          cursor: pointer;

          &:hover {
            color: #4096ff;
          }
        }

        .breadcrumb-icon {
          font-size: 14px;
          margin-right: 2px;
        }
      }

      &:last-child {
        .el-breadcrumb__inner {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
}

// 暗黑模式
.dark-mode {
  background-color: #1f1f1f;
  border-bottom-color: #333;

  :deep(.el-breadcrumb) {
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: #a6a6a6;

        &.is-link {
          color: #1677ff;

          &:hover {
            color: #4096ff;
          }
        }
      }

      &:last-child {
        .el-breadcrumb__inner {
          color: #e0e0e0;
        }
      }
    }
  }
}
</style>
