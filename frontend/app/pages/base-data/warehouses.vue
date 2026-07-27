<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库编码">
          <el-input v-model="searchForm.code" placeholder="请输入仓库编码" clearable />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input v-model="searchForm.name" placeholder="请输入仓库名称" clearable />
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
        <el-button type="primary" :icon="Plus" v-permission="'base-data:warehouse:create'" @click="handleAdd">
          新增仓库
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="仓库编码" width="120" />
        <el-table-column prop="name" label="名称" width="160" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ typeMap[row.type] ?? row.type }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'base-data:warehouse:edit'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'base-data:warehouse:delete'" @click="handleDelete(row)">
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
      width="560px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入仓库编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="form.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择仓库类型" style="width: 100%">
            <el-option label="原料仓" value="raw" />
            <el-option label="半成品仓" value="semi" />
            <el-option label="成品仓" value="finished" />
            <el-option label="包材仓" value="packaging" />
            <el-option label="不良品仓" value="defective" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
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

definePageMeta({ middleware: 'auth' })

// --------------- 常量 ---------------
const typeMap: Record<string, string> = {
  raw: '原料仓',
  semi: '半成品仓',
  finished: '成品仓',
  packaging: '包材仓',
  defective: '不良品仓',
}

// --------------- 类型 ---------------
interface Warehouse {
  id: number
  code: string
  name: string
  address: string
  manager: string
  type: string
  remark: string
  status: string
}

// --------------- Mock 数据 ---------------
const mockWarehouses: Warehouse[] = [
  { id: 1, code: 'WH-0001', name: '原料仓A', address: '工厂A栋1楼', manager: '张工', type: 'raw', remark: '存放钢板、管材', status: 'active' },
  { id: 2, code: 'WH-0002', name: '原料仓B', address: '工厂A栋2楼', manager: '李工', type: 'raw', remark: '存放电子元器件', status: 'active' },
  { id: 3, code: 'WH-0003', name: '半成品仓', address: '工厂B栋1楼', manager: '王工', type: 'semi', remark: '', status: 'active' },
  { id: 4, code: 'WH-0004', name: '成品仓A', address: '工厂C栋1楼', manager: '赵工', type: 'finished', remark: '常规成品', status: 'active' },
  { id: 5, code: 'WH-0005', name: '成品仓B', address: '工厂C栋2楼', manager: '陈工', type: 'finished', remark: '出口成品', status: 'active' },
  { id: 6, code: 'WH-0006', name: '包材仓', address: '工厂A栋B1', manager: '刘工', type: 'packaging', remark: '', status: 'active' },
  { id: 7, code: 'WH-0007', name: '不良品仓', address: '工厂D栋1楼', manager: '周工', type: 'defective', remark: '待处理不良品', status: 'active' },
  { id: 8, code: 'WH-0008', name: '临时仓', address: '工厂E栋', manager: '吴工', type: 'finished', remark: '临时周转', status: 'inactive' },
]

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
})

const form = reactive({
  code: '',
  name: '',
  address: '',
  manager: '',
  type: 'raw' as string,
  remark: '',
  status: 'active',
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Warehouse[]>([])

const rules: FormRules = {
  code: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => (isEdit.value ? '编辑仓库' : '新增仓库'))

// --------------- 方法 ---------------
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockWarehouses]

    if (searchForm.code) {
      list = list.filter((w) => w.code.includes(searchForm.code))
    }
    if (searchForm.name) {
      list = list.filter((w) => w.name.includes(searchForm.name))
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
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Warehouse) {
  isEdit.value = true
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.address = row.address
  form.manager = row.manager
  form.type = row.type
  form.remark = row.remark
  form.status = row.status
  dialogVisible.value = true
}

function handleDelete(row: Warehouse) {
  ElMessageBox.confirm(`确定要删除仓库「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    const idx = mockWarehouses.findIndex((w) => w.id === row.id)
    if (idx > -1) mockWarehouses.splice(idx, 1)
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
        const item = mockWarehouses.find((w) => w.id === editingId.value)
        if (item) Object.assign(item, { ...form })
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockWarehouses.map((w) => w.id), 0) + 1
        mockWarehouses.push({ id: newId, ...form })
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
  form.address = ''
  form.manager = ''
  form.type = 'raw'
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
