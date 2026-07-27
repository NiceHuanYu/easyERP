<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" v-permission="'system:user:create'" @click="handleAdd">
          新增用户
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="关联员工" width="120">
          <template #default="{ row }">
            {{ row.employeeName ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role"
              size="small"
              type="info"
              style="margin-right: 4px"
            >
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'system:user:update'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'system:user:delete'" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="table-pagination"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="关联员工" prop="employeeId">
          <el-select v-model="form.employeeId" placeholder="请选择关联员工" clearable filterable style="width: 100%">
            <el-option
              v-for="emp in employeeOptions"
              :key="emp.id"
              :label="`${emp.name} (${emp.code})`"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="form.roles" placeholder="请选择角色" multiple style="width: 100%">
            <el-option
              v-for="r in roleOptions"
              :key="r.value"
              :label="r.label"
              :value="r.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            :model-value="form.status"
            :active-value="1"
            :inactive-value="0"
            @change="(val: number) => { form.status = val }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// --------------- 类型 ---------------
interface User {
  id: number
  username: string
  realName: string
  email: string
  employeeId: number | null
  employeeName: string | null
  roles: string[]
  status: number
  createdAt: string
}

interface EmployeeOption {
  id: number
  code: string
  name: string
}

// --------------- 选项数据 ---------------
const employeeOptions: EmployeeOption[] = []

const roleOptions: { label: string; value: string }[] = []

// --------------- 状态 ---------------
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  username: '',
  status: '',
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  employeeId: null as number | null,
  roles: [] as string[],
  status: 1,
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<User[]>([])

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  roles: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

// --------------- 方法 ---------------
async function fetchData() {
  loading.value = true
  try {
    const result = await api.page<User>('/system/users', pagination.page, pagination.pageSize, {
      username: searchForm.username || undefined,
      status: searchForm.status || undefined,
    })
    tableData.value = result.list
    pagination.total = result.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  form.password = ''
  dialogVisible.value = true
}

function handleEdit(row: User) {
  isEdit.value = true
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.email = row.email
  form.employeeId = row.employeeId
  form.roles = [...row.roles]
  form.status = row.status
  dialogVisible.value = true
}

function handleDelete(row: User) {
  ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      await api.del(`/system/users/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e: any) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

function handleStatusChange(row: User, val: number) {
  ElMessageBox.confirm(
    `确定要${val === 1 ? '禁用' : '启用'}用户「${row.username}」吗？`,
    '状态变更',
    { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
  ).then(async () => {
    try {
      await api.put(`/system/users/${row.id}/status`, { status: val })
      row.status = val
      ElMessage.success('状态更新成功')
    } catch (e: any) {
      ElMessage.error(e?.message || '状态更新失败')
    }
  }).catch(() => {})
}

function handleResetPwd(row: User) {
  ElMessageBox.confirm(
    `确定要重置用户「${row.username}」的密码吗？重置后密码将为默认密码。`,
    '重置密码',
    { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
  ).then(() => {
    ElMessage.success('密码已重置为默认密码')
  }).catch(() => {})
}

function handleSubmit() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload: Record<string, unknown> = {
        username: form.username,
        realName: form.realName,
        email: form.email,
        employeeId: form.employeeId,
        roles: form.roles,
        status: form.status,
      }
      if (!isEdit.value) {
        payload.password = form.password
      }
      if (isEdit.value && editingId.value !== null) {
        await api.put(`/system/users/${editingId.value}`, payload)
        ElMessage.success('编辑成功')
      } else {
        await api.post('/system/users', payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClosed() {
  resetForm()
  formRef.value?.resetFields()
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.employeeId = null
  form.roles = []
  form.status = 1
}

// --------------- 初始化 ---------------
watch([() => pagination.page, () => pagination.pageSize], () => { fetchData() })
fetchData()
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.table-toolbar {
  margin-bottom: 12px;
}

.table-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
