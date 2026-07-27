<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="员工编码">
          <el-input v-model="searchForm.code" placeholder="请输入员工编码" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="请选择部门" clearable>
            <el-option label="技术部" value="tech" />
            <el-option label="生产部" value="production" />
            <el-option label="销售部" value="sales" />
            <el-option label="采购部" value="purchase" />
            <el-option label="财务部" value="finance" />
            <el-option label="人事部" value="hr" />
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
        <el-button type="primary" :icon="Plus" v-permission="'base-data:employee:create'" @click="handleAdd">
          新增员工
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="员工编码" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">
            {{ row.gender === 'male' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="100">
          <template #default="{ row }">
            {{ deptMap[row.department] ?? row.department }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'base-data:employee:edit'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'base-data:employee:delete'" @click="handleDelete(row)">
              删除
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="员工编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入员工编码" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="male">男</el-radio>
                <el-radio value="female">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
                <el-option label="技术部" value="tech" />
                <el-option label="生产部" value="production" />
                <el-option label="销售部" value="sales" />
                <el-option label="采购部" value="purchase" />
                <el-option label="财务部" value="finance" />
                <el-option label="人事部" value="hr" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="form.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker
                v-model="form.entryDate"
                type="date"
                placeholder="请选择入职日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联用户">
          <el-select v-model="form.userId" placeholder="请选择关联用户（可选）" filterable clearable style="width: 100%">
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="`${u.username} (${u.realName})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">在职</el-radio>
            <el-radio value="inactive">离职</el-radio>
          </el-radio-group>
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
import type { FormInstance, FormRules } from 'element-plus'

definePageMeta({ middleware: 'auth' })

// --------------- 常量 ---------------
const deptMap: Record<string, string> = {
  tech: '技术部',
  production: '生产部',
  sales: '销售部',
  purchase: '采购部',
  finance: '财务部',
  hr: '人事部',
}

// --------------- 类型 ---------------
interface Employee {
  id: number
  code: string
  name: string
  gender: string
  department: string
  position: string
  phone: string
  email: string
  entryDate: string
  userId: number | null
  remark: string
  status: string
}

interface UserOption {
  id: number
  username: string
  realName: string
}

// --------------- Mock 关联用户选项 ---------------
const userOptions: UserOption[] = [
  { id: 1, username: 'zhangsan', realName: '张三' },
  { id: 2, username: 'lisi', realName: '李四' },
  { id: 3, username: 'wangwu', realName: '王五' },
  { id: 4, username: 'zhaoliu', realName: '赵六' },
  { id: 5, username: 'chenqi', realName: '陈七' },
]

// --------------- Mock 数据 ---------------
const mockEmployees: Employee[] = Array.from({ length: 30 }, (_, i) => ({
  id: i + 1,
  code: `EMP-${String(i + 1).padStart(4, '0')}`,
  name: ['张三', '李四', '王五', '赵六', '陈七', '周八', '吴九', '郑十'][i % 8],
  gender: i % 3 === 0 ? 'female' : 'male',
  department: ['tech', 'production', 'sales', 'purchase', 'finance', 'hr'][i % 6],
  position: ['工程师', '主管', '经理', '专员', '操作工'][i % 5],
  phone: `1${String(3 + (i % 9)).padStart(2, '0')}${String(Math.random() * 1e8 | 0).padStart(8, '0')}`,
  email: `emp${i + 1}@company.com`,
  entryDate: `202${Math.min(4, i % 5)}-${String((i % 12) + 1).padStart(2, '0')}-${String((i % 28) + 1).padStart(2, '0')}`,
  userId: i < 5 ? i + 1 : null,
  remark: i % 4 === 0 ? `备注 ${i + 1}` : '',
  status: i % 8 === 0 ? 'inactive' : 'active',
}))

// --------------- 状态 ---------------
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  code: '',
  name: '',
  department: '',
})

const form = reactive({
  code: '',
  name: '',
  gender: 'male' as string,
  department: 'tech' as string,
  position: '',
  phone: '',
  email: '',
  entryDate: '',
  userId: null as number | null,
  remark: '',
  status: 'active',
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Employee[]>([])

const rules: FormRules = {
  code: [{ required: true, message: '请输入员工编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => (isEdit.value ? '编辑员工' : '新增员工'))

// --------------- 方法 ---------------
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockEmployees]

    if (searchForm.code) {
      list = list.filter((e) => e.code.includes(searchForm.code))
    }
    if (searchForm.name) {
      list = list.filter((e) => e.name.includes(searchForm.name))
    }
    if (searchForm.department) {
      list = list.filter((e) => e.department === searchForm.department)
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
  searchForm.code = ''
  searchForm.name = ''
  searchForm.department = ''
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Employee) {
  isEdit.value = true
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.gender = row.gender
  form.department = row.department
  form.position = row.position
  form.phone = row.phone
  form.email = row.email
  form.entryDate = row.entryDate
  form.userId = row.userId
  form.remark = row.remark
  form.status = row.status
  dialogVisible.value = true
}

function handleDelete(row: Employee) {
  ElMessageBox.confirm(`确定要删除员工「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    const idx = mockEmployees.findIndex((e) => e.id === row.id)
    if (idx > -1) mockEmployees.splice(idx, 1)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value && editingId.value !== null) {
        const item = mockEmployees.find((e) => e.id === editingId.value)
        if (item) Object.assign(item, { ...form })
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockEmployees.map((e) => e.id), 0) + 1
        mockEmployees.push({ id: newId, ...form })
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
  form.code = ''
  form.name = ''
  form.gender = 'male'
  form.department = 'tech'
  form.position = ''
  form.phone = ''
  form.email = ''
  form.entryDate = ''
  form.userId = null
  form.remark = ''
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
