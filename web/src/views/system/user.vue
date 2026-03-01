<template>
  <div class="rbac-page">
    <el-card shadow="never" class="panel search-panel">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名/邮箱"
          clearable
          style="max-width: 320px"
          @keyup.enter="loadUsers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" plain @click="loadUsers">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <div class="flex-spacer" />
        <el-button type="danger" :disabled="!selectedIds.length" @click="onBatchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel table-panel">
      <template #header>
        <div class="panel-title">👤 用户管理</div>
      </template>

      <el-table
        v-loading="loading"
        :data="rows"
        border
        stripe
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="36">{{ row.username?.slice(0, 1).toUpperCase() }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column label="所属角色" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.roles" :key="item.id" type="primary" effect="light">{{ item.roleName }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="170" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoleDrawer(row)">
              <el-icon><Edit /></el-icon>编辑角色
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadUsers"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新增用户" width="520px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="createForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptionsData" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="roleDrawerVisible" title="编辑角色" size="420px" destroy-on-close>
      <div class="drawer-title">为用户 {{ currentUser?.username }} 分配角色</div>
      <el-checkbox-group v-model="selectedRoleIds" class="role-checks">
        <el-checkbox v-for="item in roleOptionsData" :key="item.id" :label="item.id">{{ item.roleName }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="roleDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitUserRoles">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import {
  batchDeleteUsers,
  createUser,
  getUserRoleIds,
  pageUsers,
  roleOptions,
  updateUserRoles
} from '@/api/rbac'
import type { RoleSimple, UserItem } from '@/types/rbac'

const loading = ref(false)
const submitting = ref(false)
const rows = ref<UserItem[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const roleOptionsData = ref<RoleSimple[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined as number | undefined
})

const createDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  username: '',
  email: '',
  password: '',
  status: '1',
  roleIds: [] as number[]
})
const createRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }]
}

const roleDrawerVisible = ref(false)
const currentUser = ref<UserItem | null>(null)
const selectedRoleIds = ref<number[]>([])

const createTraceId = () => `trace-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const loadRoleOptions = async () => {
  roleOptionsData.value = await roleOptions()
}

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await pageUsers(query)
    rows.value = data.list
    total.value = data.count
  } finally {
    loading.value = false
  }
}

const resetQuery = async () => {
  query.keyword = ''
  query.status = undefined
  query.page = 1
  await loadUsers()
}

const onSelectionChange = (list: UserItem[]) => {
  selectedIds.value = list.map((item) => item.id)
}

const onPageSizeChange = async () => {
  query.page = 1
  await loadUsers()
}

const openCreateDialog = () => {
  createForm.username = ''
  createForm.email = ''
  createForm.password = ''
  createForm.status = '1'
  createForm.roleIds = []
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createUser({ ...createForm, traceId: createTraceId() })
    ElMessage.success('新增用户成功')
    createDialogVisible.value = false
    await loadUsers()
  } finally {
    submitting.value = false
  }
}

const openRoleDrawer = async (row: UserItem) => {
  currentUser.value = row
  selectedRoleIds.value = await getUserRoleIds(row.id)
  roleDrawerVisible.value = true
}

const submitUserRoles = async () => {
  if (!currentUser.value) return
  submitting.value = true
  try {
    await updateUserRoles(currentUser.value.id, {
      roleIds: selectedRoleIds.value,
      traceId: createTraceId()
    })
    ElMessage.success('角色分配成功')
    roleDrawerVisible.value = false
    await loadUsers()
  } finally {
    submitting.value = false
  }
}

const onBatchDelete = async () => {
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个用户吗？`, '二次确认', {
    type: 'warning'
  })
  await batchDeleteUsers({ userIds: selectedIds.value, traceId: createTraceId() })
  ElMessage.success('删除成功')
  selectedIds.value = []
  await loadUsers()
}

onMounted(async () => {
  await Promise.all([loadRoleOptions(), loadUsers()])
})
</script>

<style scoped lang="scss">
.rbac-page {
  min-height: 100%;
  padding: 0;
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

  .flex-spacer {
    flex: 1;
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #24324b;
  }

  .pager-wrap {
    display: flex;
    justify-content: flex-end;
    padding: 14px 0 2px;
  }

  .drawer-title {
    margin-bottom: 14px;
    color: #57617a;
  }

  .role-checks {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .drawer-footer {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
