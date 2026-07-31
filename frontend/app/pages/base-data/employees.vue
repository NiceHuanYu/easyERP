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
          <el-select v-model="searchForm.dept" placeholder="请选择部门" clearable style="width: 120px">
            <el-option v-for="d in deptOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100px">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
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
              <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="code" label="员工编码" min-width="110" />
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column label="性别" min-width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column label="部门" width="100">
          <template #default="{ row }">
            {{ deptMap[row.dept] ?? row.dept }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
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
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="员工编码" prop="code">
          <el-input v-model="form.code" placeholder="自动生成" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门" prop="dept">
          <el-select v-model="form.dept" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../composables/useApi'
import { generateCode } from '~/utils'

definePageMeta({ middleware: 'auth' })

const API_PATH = '/base/employees'

const deptMap: Record<string, string> = {
  tech: '技术部', production: '生产部', sales: '销售部',
  purchase: '采购部', finance: '财务部', hr: '人事部',
}

const deptOptions = computed(() =>
  Object.entries(deptMap).map(([value, label]) => ({ value, label }))
)

interface Employee {
  id: number; code: string; name: string; gender: number; dept: string; phone: string; status: number
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const searchForm = reactive({ code: '', name: '', dept: '', status: undefined as number | undefined })

const form = reactive({ code: '', name: '', gender: 1 as number, dept: 'tech' as string, phone: '', status: 1 as number })

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Employee[]>([])

const rules: FormRules = {
  code: [{ required: true, message: '请输入员工编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  dept: [{ required: true, message: '请选择部门', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const dialogTitle = computed(() => (isEdit.value ? '编辑员工' : '新增员工'))

async function fetchData() {
  loading.value = true
  try {
    const result = await api.page<Employee>(API_PATH, pagination.page, pagination.pageSize, {
      code: searchForm.code || undefined,
      name: searchForm.name || undefined,
      dept: searchForm.dept || undefined,
      status: searchForm.status,
    })
    tableData.value = result.list
    pagination.total = result.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取数据失败')
  } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.code = ''; searchForm.name = ''; searchForm.dept = ''; searchForm.status = undefined; pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false; editingId.value = null
  form.code = generateCode('employee')
  form.name = ''; form.gender = 1; form.dept = 'tech'; form.phone = ''; form.status = 1
  dialogVisible.value = true
}

function handleEdit(row: Employee) {
  isEdit.value = true; editingId.value = row.id
  form.code = row.code; form.name = row.name; form.gender = row.gender
  form.dept = row.dept || 'tech'; form.phone = row.phone || ''; form.status = row.status
  dialogVisible.value = true
}

async function handleDelete(row: Employee) {
  try {
    await ElMessageBox.confirm(`确定要删除员工「${row.name}」吗？`, '删除确认', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消',
    })
    await api.del(`${API_PATH}/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '删除失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const body = { ...form }
    if (isEdit.value && editingId.value !== null) {
      await api.put(`${API_PATH}/${editingId.value}`, body)
      ElMessage.success('编辑成功')
    } else {
      await api.post(API_PATH, body)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally { submitLoading.value = false }
}

function handleDialogClosed() { formRef.value?.resetFields() }

watch([() => pagination.page, () => pagination.pageSize], () => { fetchData() })
fetchData()
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-pagination { margin-top: 12px; justify-content: flex-end; }
</style>
