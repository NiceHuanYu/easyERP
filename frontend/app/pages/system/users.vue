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
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
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
              active-value="active"
              inactive-value="inactive"
              @change="(val: string) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'system:user:edit'" @click="handleEdit(row)">
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
        @current-change="fetchData"
        @size-change="fetchData"
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
            active-value="active"
            inactive-value="inactive"
            @change="(val: string) => { form.status = val }"
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
  status: string
  createdAt: string
}

interface EmployeeOption {
  id: number
  code: string
  name: string
}

// --------------- Mock 选项数据 ---------------
const employeeOptions: EmployeeOption[] = [
  { id: 1, code: 'EMP-0001', name: '张三' },
  { id: 2, code: 'EMP-0002', name: '李四' },
  { id: 3, code: 'EMP-0003', name: '王五' },
  { id: 4, code: 'EMP-0004', name: '赵六' },
  { id: 5, code: 'EMP-0005', name: '陈七' },
  { id: 6, code: 'EMP-0006', name: '周八' },
]

const roleOptions = [
  { label: '系统管理员', value: 'admin' },
  { label: '销售经理', value: 'sales_manager' },
  { label: '生产主管', value: 'production_supervisor' },
  { label: '采购员', value: 'purchaser' },
  { label: '财务', value: 'finance' },
  { label: '仓管', value: 'warehouse_keeper' },
  { label: '普通用户', value: 'user' },
]

// --------------- Mock 数据 ---------------
const mockUsers: User[] = Array.from({ length: 35 }, (_, i) => ({
  id: i + 1,
  username: ['admin', 'zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'chenqi', 'zhouba'][i % 7] + (i > 6 ? `_${i + 1}` : ''),
  realName: ['管理员', '张三', '李四', '王五', '赵六', '陈七', '周八'][i % 7],
  email: `user${i + 1}@company.com`,
  employeeId: i < 6 ? i + 1 : null,
  employeeName: i < 6 ? ['张三', '李四', '王五', '赵六', '陈七', '周八'][i % 6] : null,
  roles: [['admin'], ['sales_manager'], ['production_supervisor'], ['purchaser'], ['finance'], ['warehouse_keeper'], ['user']][i % 7],
  status: i % 10 === 0 ? 'inactive' : 'active',
  createdAt: `202${Math.min(4, i % 5)}-${String((i % 12) + 1).padStart(2, '0')}-${String((i % 28) + 1).padStart(2, '0')} ${String(i % 24).padStart(2, '0')}:${String(i % 60).padStart(2, '0')}:00`,
}))

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
  status: 'active',
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
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockUsers]

    if (searchForm.username) {
      list = list.filter((u) => u.username.includes(searchForm.username))
    }
    if (searchForm.status) {
      list = list.filter((u) => u.status === searchForm.status)
    }

    pagination.total = list.length
    const start = (pagination.page - 1) * pagination.pageSize
    tableData.value = list.slice(start, start + pagination.pageSize)
    loading.value = false
  }, 300)
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
  }).then(() => {
    const idx = mockUsers.findIndex((u) => u.id === row.id)
    if (idx > -1) mockUsers.splice(idx, 1)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleStatusChange(row: User, val: string) {
  ElMessageBox.confirm(
    `确定要${val === 'active' ? '禁用' : '启用'}用户「${row.username}」吗？`,
    '状态变更',
    { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
  ).then(() => {
    row.status = val
    ElMessage.success('状态更新成功')
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
  formRef.value?.validate((valid) => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value && editingId.value !== null) {
        const item = mockUsers.find((u) => u.id === editingId.value)
        if (item) {
          const emp = employeeOptions.find((e) => e.id === form.employeeId)
          Object.assign(item, {
            username: form.username,
            realName: form.realName,
            email: form.email,
            employeeId: form.employeeId,
            employeeName: emp?.name ?? null,
            roles: [...form.roles],
            status: form.status,
          })
        }
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockUsers.map((u) => u.id), 0) + 1
        const emp = employeeOptions.find((e) => e.id === form.employeeId)
        mockUsers.push({
          id: newId,
          username: form.username,
          realName: form.realName,
          email: form.email,
          employeeId: form.employeeId,
          employeeName: emp?.name ?? null,
          roles: [...form.roles],
          status: form.status,
          createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
        })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      dialogVisible.value = false
      fetchData()
    }, 300)
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
  form.status = 'active'
}

// --------------- 初始化 ---------------
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
