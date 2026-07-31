<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.keyword" placeholder="请输入用户名/昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="employeeName" label="关联员工" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="handleToggleStatus(row)">禁用</el-button>
            <el-button v-else link type="success" @click="handleToggleStatus(row)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        class="table-pagination" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联员工">
          <el-select v-model="form.employeeId" placeholder="请选择员工" clearable filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.value" :label="e.label" :value="e.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | string | null>(null)
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const form = reactive({ username: '', password: '', nickname: '', roleIds: [] as string[], employeeId: null as string | null, status: 1 })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<any[]>([])
const roleOptions = ref<{ label: string; value: string }[]>([])

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', validator: (_rule: any, value: string, cb: any) => { if (isEdit.value && !value) cb(); else if (!value) cb(new Error('请输入密码')); else cb(); } }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

async function loadRoles() {
  try {
    const data = await api.get<any[]>('/system/roles')
    roleOptions.value = (data || []).map((r: any) => ({ label: r.name, value: String(r.id) }))
  } catch { /* ignore */ }
}
const employeeOptions = ref<{ label: string; value: string }[]>([])
async function loadEmployees() {
  try {
    const result = await api.page<any>('/base/employees', 1, 1000)
    employeeOptions.value = (result.list || []).map((e: any) => ({ label: e.name + ' (' + e.code + ')', value: String(e.id) }))
  } catch { /* ignore */ }
}


async function loadUserRoles(userId: number | string): Promise<string[]> {
  try {
    const data = await api.get<any[]>('/system/users/' + userId + '/roles')
    return (data || []).map((v: any) => String(v))
  } catch { return [] }
}

async function fetchData() {
  loading.value = true
  try {
    const result = await api.page<any>('/system/users', pagination.page, pagination.pageSize,
      searchForm.keyword ? { keyword: searchForm.keyword } : {})
    tableData.value = result.list
    pagination.total = result.total
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false; editingId.value = null
  form.username = form.password = ''; form.nickname = ''; form.roleIds = []; form.employeeId = null; form.status = 1
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true; editingId.value = row.id
  form.username = row.username; form.password = ''; form.nickname = row.nickname || ''
  form.status = row.status
  form.employeeId = row.employeeId ? String(row.employeeId) : null
  form.roleIds = await loadUserRoles(row.id)
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm('确定删除用户 ' + row.username + '？', '删除', { type: 'warning' }).then(async () => {
    await api.del('/system/users/' + row.id)
    ElMessage.success('已删除')
    fetchData()
  }).catch(() => {})
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await api.put('/system/users/' + row.id + '/status', { status: newStatus })
  row.status = newStatus
  ElMessage.success('状态已更新')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload: any = { username: form.username, nickname: form.nickname, status: form.status }
    if (form.employeeId) payload.employeeId = form.employeeId
    if (form.password) payload.password = form.password
    if (isEdit.value) {
      await api.put('/system/users/' + editingId.value, payload)
    } else {
      const res = await api.post<any>('/system/users', payload)
      editingId.value = res?.id ? res.id : null
    }
    // Save roles
    if (editingId.value) {
      await api.put('/system/users/' + editingId.value + '/roles', form.roleIds)
    }
    ElMessage.success(isEdit.value ? '已更新' : '已创建')
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('保存失败') } finally { submitLoading.value = false }
}

function handleDialogClosed() { formRef.value?.resetFields() }

onMounted(() => { loadRoles(); loadEmployees(); fetchData() })
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-pagination { margin-top: 16px; justify-content: flex-end; }
</style>
