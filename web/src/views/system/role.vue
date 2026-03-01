<template>
  <div class="rbac-page">
    <el-card shadow="never" class="panel">
      <div class="toolbar">
        <div class="panel-title">🛡️ 角色管理</div>
        <div class="flex-spacer" />
        <el-button type="primary" @click="openCreateRole">新增角色</el-button>
      </div>
    </el-card>

    <el-row :gutter="14">
      <el-col v-for="item in roles" :key="item.id" :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="role-card">
          <template #header>
            <div class="role-header">
              <div>
                <div class="role-name">{{ item.roleName }}</div>
                <div class="role-code">{{ item.roleCode }}</div>
              </div>
              <el-dropdown>
                <el-button link type="primary">操作</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openPermissionDrawer(item)">编辑权限</el-dropdown-item>
                    <el-dropdown-item divided @click="onDeleteRole(item)">删除角色</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>

          <div class="desc">{{ item.description || '暂无描述' }}</div>
          <div class="metrics">
            <el-tag type="info">关联用户 {{ item.userCount }}</el-tag>
            <el-tag type="success">权限 {{ item.permissionCount }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="createDialogVisible" title="新增角色" width="560px" destroy-on-close>
      <el-form ref="createRoleRef" :model="createRoleForm" :rules="createRoleRules" label-width="90px">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="createRoleForm.roleName" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createRoleForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="初始权限" prop="permissionIds">
          <el-tree
            ref="createPermissionTreeRef"
            :data="permissionTreeData"
            show-checkbox
            node-key="id"
            :default-expand-all="true"
            :props="treeProps"
            @check="onCreatePermissionCheck"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitCreateRole">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="permissionDrawerVisible" title="编辑权限" size="760px" destroy-on-close>
      <div class="drawer-grid">
        <el-card shadow="never">
          <template #header>左侧权限树</template>
          <el-tree
            ref="editPermissionTreeRef"
            :data="permissionTreeData"
            show-checkbox
            node-key="id"
            :default-expand-all="true"
            :props="treeProps"
            @check="onEditPermissionCheck"
          />
        </el-card>
        <el-card shadow="never">
          <template #header>右侧已选权限</template>
          <el-scrollbar max-height="460px">
            <el-space wrap>
              <el-tag v-for="item in selectedPermissions" :key="item.id" type="primary" effect="plain">
                {{ item.permissionName }}
              </el-tag>
            </el-space>
          </el-scrollbar>
        </el-card>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="permissionDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="saveRolePermissions">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElTree, FormInstance, FormRules } from 'element-plus'
import {
  createRole,
  deleteRole,
  getRolePermissionIds,
  listRoles,
  permissionTree,
  updateRolePermissions
} from '@/api/rbac'
import type { PermissionTree, RoleItem } from '@/types/rbac'

const loading = ref(false)
const roles = ref<RoleItem[]>([])
const permissionTreeData = ref<PermissionTree[]>([])

const treeProps = {
  label: (node: PermissionTree | { permissionName: string }) => {
    if ('permissionName' in node) return node.permissionName
    return node.module
  },
  children: 'children'
}

const createTraceId = () => `trace-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const loadData = async () => {
  loading.value = true
  try {
    const [roleResp, permissionResp] = await Promise.all([listRoles(), permissionTree()])
    roles.value = roleResp
    permissionTreeData.value = permissionResp
  } finally {
    loading.value = false
  }
}

const createDialogVisible = ref(false)
const createRoleRef = ref<FormInstance>()
const createPermissionTreeRef = ref<InstanceType<typeof ElTree>>()
const createRoleForm = reactive({
  roleName: '',
  description: '',
  permissionIds: [] as number[]
})
const createRoleRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  permissionIds: [{ required: true, message: '请至少选择一个权限', trigger: 'change' }]
}

const openCreateRole = () => {
  createRoleForm.roleName = ''
  createRoleForm.description = ''
  createRoleForm.permissionIds = []
  createDialogVisible.value = true
}

const onCreatePermissionCheck = () => {
  createRoleForm.permissionIds = (createPermissionTreeRef.value?.getCheckedKeys(false) || []) as number[]
}

const submitCreateRole = async () => {
  const valid = await createRoleRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!createRoleForm.permissionIds.length) {
    ElMessage.warning('请至少选择一个权限')
    return
  }
  loading.value = true
  try {
    await createRole({
      roleName: createRoleForm.roleName,
      description: createRoleForm.description,
      permissionIds: createRoleForm.permissionIds,
      traceId: createTraceId()
    })
    ElMessage.success('角色创建成功')
    createDialogVisible.value = false
    await loadData()
  } finally {
    loading.value = false
  }
}

const permissionDrawerVisible = ref(false)
const editingRole = ref<RoleItem | null>(null)
const selectedPermissionIds = ref<number[]>([])
const editPermissionTreeRef = ref<InstanceType<typeof ElTree>>()

const permissionMap = computed(() => {
  const map = new Map<number, { id: number; permissionName: string }>()
  for (const group of permissionTreeData.value) {
    for (const item of group.children) {
      map.set(item.id, { id: item.id, permissionName: item.permissionName })
    }
  }
  return map
})

const selectedPermissions = computed(() => {
  return selectedPermissionIds.value
    .map((id) => permissionMap.value.get(id))
    .filter(Boolean) as Array<{ id: number; permissionName: string }>
})

const openPermissionDrawer = async (role: RoleItem) => {
  editingRole.value = role
  selectedPermissionIds.value = await getRolePermissionIds(role.id)
  permissionDrawerVisible.value = true
  requestAnimationFrame(() => {
    editPermissionTreeRef.value?.setCheckedKeys(selectedPermissionIds.value)
  })
}

const onEditPermissionCheck = () => {
  selectedPermissionIds.value = (editPermissionTreeRef.value?.getCheckedKeys(false) || []) as number[]
}

const saveRolePermissions = async () => {
  if (!editingRole.value) return
  loading.value = true
  try {
    await updateRolePermissions(editingRole.value.id, {
      permissionIds: selectedPermissionIds.value,
      traceId: createTraceId()
    })
    ElMessage.success('权限更新成功')
    permissionDrawerVisible.value = false
    await loadData()
  } finally {
    loading.value = false
  }
}

const onDeleteRole = async (role: RoleItem) => {
  await ElMessageBox.confirm(`确认删除角色「${role.roleName}」吗？`, '二次确认', { type: 'warning' })
  await deleteRole(role.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
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
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #24324b;
  }

  .flex-spacer {
    flex: 1;
  }

  .role-card {
    margin-bottom: 14px;
    border-radius: 12px;
  }

  .role-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .role-name {
    font-weight: 600;
    font-size: 15px;
  }

  .role-code {
    color: #8089a2;
    font-size: 12px;
    margin-top: 4px;
  }

  .desc {
    color: #57617a;
    min-height: 40px;
    margin-bottom: 10px;
  }

  .metrics {
    display: flex;
    gap: 8px;
  }

  .drawer-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .drawer-footer {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
