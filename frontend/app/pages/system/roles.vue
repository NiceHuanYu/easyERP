<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="角色名称" width="140" />
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button link type="warning" @click="handleAssignPermission(row)">分配权限</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        class="table-pagination" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px">
      <el-tree ref="treeRef" :data="permTreeData" :props="{ children: 'children', label: 'label' }"
        node-key="id" show-checkbox default-expand-all :default-checked-keys="checkedPermKeys" />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handlePermSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const permDialogVisible = ref(false)
const permSubmitLoading = ref(false)
const editingRoleForPerm = ref<any>(null)
const checkedPermKeys = ref<string[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()
const permTreeData = ref<any[]>([])

const searchForm = reactive({ name: '' })
const form = reactive({ name: '', code: '', remark: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<any[]>([])

const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

async function loadPermTree() {
  try {
    const data = await api.get<any[]>('/system/roles/permissions')
    permTreeData.value = data || []
  } catch { permTreeData.value = [] }
}

async function loadRolePermissions(roleId: number): Promise<number[]> {
  try {
    const data = await api.get<number[]>('/system/roles/' + roleId + '/permissions')
    return (data || []).map(String)
  } catch { return [] }
}

async function fetchData() {
  loading.value = true
  try {
    const data = await api.get<any[]>('/system/roles')
    let list = data || []
    if (searchForm.name) list = list.filter((r: any) => r.name.includes(searchForm.name))
    pagination.total = list.length
    const start = (pagination.page - 1) * pagination.pageSize
    tableData.value = list.slice(start, start + pagination.pageSize)
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.name = ''; pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false; editingId.value = null
  form.name = ''; form.code = ''; form.remark = ''
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; editingId.value = row.id
  form.name = row.name; form.code = row.code; form.remark = row.remark || ''
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm('确定删除角色 ' + row.name + '？', '删除', { type: 'warning' }).then(async () => {
    await api.del('/system/roles/' + row.id)
    ElMessage.success('已删除')
    fetchData()
  }).catch(() => {})
}

async function handleAssignPermission(row: any) {
  editingRoleForPerm.value = row
  checkedPermKeys.value = []
  permDialogVisible.value = true
  const keys = await loadRolePermissions(row.id)
  await nextTick()
  checkedPermKeys.value = keys
  treeRef.value?.setCheckedKeys(keys, false)
}

async function handlePermSubmit() {
  if (!editingRoleForPerm.value || !treeRef.value) return
  permSubmitLoading.value = true
  try {
    const checked = treeRef.value.getCheckedKeys(false) as string[]
    const half = treeRef.value.getHalfCheckedKeys() as string[]
    const allIds: number[] = [...checked, ...half].map(Number)
    await api.put('/system/roles/' + editingRoleForPerm.value.id + '/permissions', allIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch { ElMessage.error('保存失败') } finally { permSubmitLoading.value = false }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = { name: form.name, code: form.code, remark: form.remark }
    if (isEdit.value) {
      await api.put('/system/roles/' + editingId.value, payload)
    } else {
      await api.post('/system/roles', payload)
    }
    ElMessage.success(isEdit.value ? '已更新' : '已创建')
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('保存失败') } finally { submitLoading.value = false }
}

function handleDialogClosed() { formRef.value?.resetFields() }

onMounted(() => { loadPermTree(); fetchData() })
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-pagination { margin-top: 16px; justify-content: flex-end; }
</style>
