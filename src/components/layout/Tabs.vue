<template>
  <div class="tabs-container" :class="{ 'dark-mode': isDark }">
    <div class="tabs-wrapper">
      <el-tabs
        v-model="activeTab"
        type="card"
        @tab-click="handleTabClick"
        @tab-remove="handleTabRemove"
      >
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.path"
          :label="tab.title"
          :name="tab.path"
          :closable="tab.closable"
        >
          <template #label>
            <span class="tab-label">
              <el-icon v-if="tab.icon" class="tab-icon">
                <component :is="tab.icon" />
              </el-icon>
              {{ tab.title }}
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="tabs-actions">
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button type="primary" link class="tabs-more-btn">
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="refresh">
              <el-icon><Refresh /></el-icon>刷新当前
            </el-dropdown-item>
            <el-dropdown-item command="closeOthers">
              <el-icon><CircleClose /></el-icon>关闭其他
            </el-dropdown-item>
            <el-dropdown-item command="closeAll">
              <el-icon><FolderDelete /></el-icon>关闭所有
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Tab 栏组件
 * 管理多标签页的展示和切换
 */
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useTabsStore } from '@/stores/tabs'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const themeStore = useThemeStore()

const { tabs, activeTab: storeActiveTab } = storeToRefs(tabsStore)
const { isDark } = storeToRefs(themeStore)

/**
 * 当前激活的 Tab
 */
const activeTab = computed({
  get: () => storeActiveTab.value,
  set: (val) => tabsStore.switchTab(val)
})

/**
 * 监听路由变化，自动添加 Tab
 */
watch(
  () => route.path,
  () => {
    tabsStore.addTabFromRoute(route)
  },
  { immediate: true }
)

/**
 * Tab 点击
 */
const handleTabClick = (tab: any) => {
  const path = tab.props.name
  if (path && path !== route.path) {
    router.push(path)
  }
}

/**
 * Tab 关闭
 */
const handleTabRemove = (path: string) => {
  const nextPath = tabsStore.removeTab(path)
  if (nextPath) {
    router.push(nextPath)
  }
}

/**
 * 下拉菜单命令
 */
const handleCommand = (command: string) => {
  switch (command) {
    case 'refresh':
      tabsStore.refreshTab(activeTab.value)
      break
    case 'closeOthers':
      tabsStore.closeOthers(activeTab.value)
      break
    case 'closeAll':
      tabsStore.closeAll()
      if (tabsStore.activeTab !== route.path) {
        router.push(tabsStore.activeTab)
      }
      break
  }
}
</script>

<style scoped lang="scss">
.tabs-container {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 10px;
  height: 40px;

  .tabs-wrapper {
    flex: 1;
    overflow: hidden;

    :deep(.el-tabs) {
      .el-tabs__header {
        margin: 0;
        border-bottom: none;
      }

      .el-tabs__nav {
        border: none;
      }

      .el-tabs__item {
        height: 40px;
        line-height: 40px;
        border: none;
        border-right: 1px solid #e4e7ed;
        padding: 0 16px;
        transition: all 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        &.is-active {
          background-color: #1677ff;
          color: #fff;
          border-right-color: #1677ff;
        }

        .tab-label {
          display: flex;
          align-items: center;
          gap: 6px;

          .tab-icon {
            font-size: 14px;
          }
        }

        .el-icon-close {
          margin-left: 8px;
          font-size: 12px;

          &:hover {
            background-color: rgba(255, 255, 255, 0.2);
          }
        }
      }
    }
  }

  .tabs-actions {
    padding-left: 10px;
    border-left: 1px solid #e4e7ed;

    .tabs-more-btn {
      font-size: 16px;
    }
  }
}

// 暗黑模式
.dark-mode {
  background-color: #1f1f1f;
  border-bottom-color: #333;

  .tabs-wrapper {
    :deep(.el-tabs) {
      .el-tabs__item {
        color: #a6a6a6;
        border-right-color: #333;

        &:hover {
          background-color: #2a2a2a;
          color: #e0e0e0;
        }

        &.is-active {
          background-color: #1677ff;
          color: #fff;
          border-right-color: #1677ff;
        }
      }
    }
  }

  .tabs-actions {
    border-left-color: #333;
  }
}
</style>
