<template>
  <div class="user-management" :class="{ 'dark-mode': isDark }">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form
        ref="searchFormRef"
        :model="searchForm"
        inline
        class="search-form"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">用户列表</div>
          <div class="table-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增用户
            </el-button>
            <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        @selection-change="handleSelectionChange"
        class="user-table"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="管理员" value="管理员" />
                <el-option label="普通用户" value="普通用户" />
                <el-option label="访客" value="访客" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="enabled">启用</el-radio>
                <el-radio label="disabled">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 用户管理页面
 * 包含用户查询、表格展示、分页、增删改查功能
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'

// ==================== 主题 ====================

const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

// ==================== 搜索表单 ====================

const searchFormRef = ref<FormInstance>()

const searchForm = reactive({
  username: '',
  phone: '',
  status: '',
  dateRange: [] as Date[]
})

/**
 * 查询
 */
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.page = 1
  loadUserList()
}

// ==================== 表格数据 ====================

const loading = ref(false)
const userList = ref<any[]>([])
const selectedRows = ref<any[]>([])

/**
 * 模拟用户数据
 */
const mockUserList = [
  { id: 1, username: 'admin', nickname: '管理员', phone: '13800138000', email: 'admin@example.com', role: '管理员', status: 'enabled', createTime: '2024-01-15 10:30:00' },
  { id: 2, username: 'zhangsan', nickname: '张三', phone: '13800138001', email: 'zhangsan@example.com', role: '普通用户', status: 'enabled', createTime: '2024-01-16 14:20:00' },
  { id: 3, username: 'lisi', nickname: '李四', phone: '13800138002', email: 'lisi@example.com', role: '普通用户', status: 'disabled', createTime: '2024-01-17 09:15:00' },
  { id: 4, username: 'wangwu', nickname: '王五', phone: '13800138003', email: 'wangwu@example.com', role: '访客', status: 'enabled', createTime: '2024-01-18 16:45:00' },
  { id: 5, username: 'zhaoliu', nickname: '赵六', phone: '13800138004', email: 'zhaoliu@example.com', role: '普通用户', status: 'enabled', createTime: '2024-01-19 11:30:00' },
  { id: 6, username: 'qianqi', nickname: '钱七', phone: '13800138005', email: 'qianqi@example.com', role: '访客', status: 'disabled', createTime: '2024-01-20 13:20:00' },
  { id: 7, username: 'sunba', nickname: '孙八', phone: '13800138006', email: 'sunba@example.com', role: '普通用户', status: 'enabled', createTime: '2024-01-21 15:10:00' },
  { id: 8, username: 'zhoujiu', nickname: '周九', phone: '13800138007', email: 'zhoujiu@example.com', role: '管理员', status: 'enabled', createTime: '2024-01-22 08:45:00' },
  { id: 9, username: 'wushi', nickname: '吴十', phone: '13800138008', email: 'wushi@example.com', role: '普通用户', status: 'enabled', createTime: '2024-01-23 17:30:00' },
  { id: 10, username: 'zheng11', nickname: '郑十一', phone: '13800138009', email: 'zheng11@example.com', role: '访客', status: 'disabled', createTime: '2024-01-24 12:00:00' }
]

/**
 * 加载用户列表
 */
const loadUserList = () => {
  loading.value = true
  // 模拟 API 请求
  setTimeout(() => {
    userList.value = mockUserList.slice(
      (pagination.page - 1) * pagination.pageSize,
      pagination.page * pagination.pageSize
    )
    pagination.total = mockUserList.length
    loading.value = false
  }, 300)
}

/**
 * 获取角色标签类型
 */
const getRoleType = (role: string) => {
  const map: Record<string, string> = {
    '管理员': 'danger',
    '普通用户': 'primary',
    '访客': 'info'
  }
  return map[role] || 'info'
}

/**
 * 选择行变化
 */
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// ==================== 分页 ====================

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadUserList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadUserList()
}

// ==================== 操作按钮 ====================

/**
 * 新增用户
 */
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  formData.id = undefined
  formData.username = ''
  formData.nickname = ''
  formData.phone = ''
  formData.email = ''
  formData.role = '普通用户'
  formData.status = 'enabled'
  dialogVisible.value = true
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`,
      '提示',
      { type: 'warning' }
    )
    ElMessage.success('批量删除成功')
    loadUserList()
  } catch {
    // 取消
  }
}

/**
 * 导出
 */
const handleExport = () => {
  ElMessage.success('导出成功')
}

/**
 * 查看用户
 */
const handleView = (row: any) => {
  ElMessage.info(`查看用户: ${row.username}`)
}

/**
 * 编辑用户
 */
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, row)
  dialogVisible.value = true
}

/**
 * 删除用户
 */
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadUserList()
  } catch {
    // 取消
  }
}

/**
 * 状态变更
 */
const handleStatusChange = (row: any) => {
  const statusText = row.status === 'enabled' ? '启用' : '禁用'
  ElMessage.success(`用户 "${row.username}" 已${statusText}`)
}

// ==================== 表单对话框 ====================

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref<FormInstance>()

const formData = reactive({
  id: undefined as number | undefined,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  role: '普通用户',
  status: 'enabled'
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  ElMessage.success(dialogTitle.value === '新增用户' ? '新增成功' : '编辑成功')
  dialogVisible.value = false
  loadUserList()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-management {
  padding-bottom: 20px;

  .search-card {
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .search-form {
    .el-form-item {
      margin-bottom: 0;
      margin-right: 20px;
    }
  }

  .table-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
    }

    :deep(.el-card__body) {
      padding: 0;
    }
  }

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .table-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .table-actions {
      display: flex;
      gap: 10px;
    }
  }

  .user-table {
    :deep(.el-table__header) {
      th {
        background-color: #f5f7fa;
        color: #606266;
        font-weight: 600;
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    padding: 15px 20px;
    border-top: 1px solid #ebeef5;
  }
}

// 暗黑模式样式
.dark-mode {
  .search-card,
  .table-card {
    background-color: #1f1f1f;
    border-color: #333;

    :deep(.el-card__header) {
      border-bottom-color: #333;
    }
  }

  .table-header {
    .table-title {
      color: #e0e0e0;
    }
  }

  .user-table {
    :deep(.el-table__header) {
      th {
        background-color: #2a2a2a;
        color: #e0e0e0;
      }
    }

    :deep(.el-table__body) {
      tr {
        background-color: transparent;
      }

      td {
        background-color: transparent;
        color: #e0e0e0;
        border-bottom-color: #333;
      }

      tr:hover > td {
        background-color: #2a2a2a !important;
      }
    }
  }

  .pagination-container {
    border-top-color: #333;
  }
}
</style>
