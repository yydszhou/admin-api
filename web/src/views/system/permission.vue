<template>
  <div class="rbac-page">
    <el-card shadow="never" class="panel">
      <div class="toolbar">
        <div class="panel-title">🔑 权限管理</div>
        <el-input
          v-model="keyword"
          placeholder="按模块/权限名/编码搜索"
          clearable
          style="width: 320px"
          @keyup.enter="loadPermissions"
        />
        <el-button type="primary" plain @click="loadPermissions">搜索</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel">
      <el-tree
        v-loading="loading"
        :data="treeData"
        node-key="id"
        :props="treeProps"
        default-expand-all
      >
        <template #default="{ data }">
          <div v-if="data.children" class="module-node">
            <span>{{ data.module }}</span>
            <span class="module-count">({{ data.children.length }})</span>
          </div>
          <div v-else class="permission-node">
            <div>
              <div class="perm-title">{{ data.permissionName }}</div>
              <div class="perm-sub">{{ data.permissionCode }} · {{ data.description || '无描述' }}</div>
            </div>
            <el-switch
              :model-value="data.enabled"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              @change="(val:boolean)=>onTogglePermission(data.id, val)"
            />
          </div>
        </template>
      </el-tree>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { permissionTree, updatePermissionStatus } from '@/api/rbac'
import type { PermissionTree } from '@/types/rbac'

const loading = ref(false)
const keyword = ref('')
const treeData = ref<PermissionTree[]>([])

const treeProps = {
  label: 'module',
  children: 'children'
}

const createTraceId = () => `trace-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const loadPermissions = async () => {
  loading.value = true
  try {
    treeData.value = await permissionTree(keyword.value || undefined)
  } finally {
    loading.value = false
  }
}

const onTogglePermission = async (permissionId: number, enabled: boolean) => {
  try {
    await ElMessageBox.confirm(
      `${enabled ? '启用' : '停用'}该权限后会影响角色授权，是否继续？`,
      '二次确认',
      { type: 'warning' }
    )
    await updatePermissionStatus(permissionId, { enabled, traceId: createTraceId() })
    ElMessage.success('权限状态已更新')
    await loadPermissions()
  } catch {
    await loadPermissions()
  }
}

onMounted(loadPermissions)
</script>

<style scoped lang="scss">
.rbac-page {
  background: #f5f7fb;

  .panel {
    border: none;
    border-radius: 12px;
    margin-bottom: 14px;
  }

  .toolbar {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #24324b;
    margin-right: 6px;
  }

  .module-node {
    font-weight: 600;
    color: #3f4d6b;
  }

  .module-count {
    margin-left: 6px;
    color: #8c95ad;
  }

  .permission-node {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    padding: 4px 0;
  }

  .perm-title {
    color: #2d3a58;
  }

  .perm-sub {
    color: #8c95ad;
    font-size: 12px;
  }
}
</style>
