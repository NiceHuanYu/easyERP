<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="供应商编码">
          <el-input v-model="searchForm.code" placeholder="请输入供应商编码" clearable />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
        <el-button type="primary" :icon="Plus" v-permission="'base-data:supplier:create'" @click="handleAdd">
          新增供应商
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="供应商编码" width="130" />
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'base-data:supplier:edit'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'base-data:supplier:delete'" @click="handleDelete(row)">
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
        <el-form-item label="供应商编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入供应商编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
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

// --------------- 类型 ---------------
interface Supplier {
  id: number
  code: string
  name: string
  contact: string
  phone: string
  email: string
  address: string
  remark: string
  status: string
}

// --------------- Mock 数据 ---------------
const mockSuppliers: Supplier[] = Array.from({ length: 25 }, (_, i) => ({
  id: i + 1,
  code: `SUP-${String(i + 1).padStart(4, '0')}`,
  name: `${['鑫达钢铁', '永利五金', '通达轴承', '华美包装', '长城电气'][i % 5]}有限公司${i + 1}`,
  contact: ['刘总', '周经理', '吴厂长', '郑工', '钱女士'][i % 5],
  phone: `1${String(3 + (i % 9)).padStart(2, '0')}${String(Math.random() * 1e8 | 0).padStart(8, '0')}`,
  email: `supplier${i + 1}@example.com`,
  address: `${['东莞市', '佛山市', '苏州市', '无锡市', '宁波市'][i % 5]}${['长安镇', '狮山镇', '昆山', '江阴', '慈溪'][i % 5]}工业区${i + 1}号`,
  remark: `备注信息 ${i + 1}`,
  status: i % 7 === 0 ? 'inactive' : 'active',
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
  status: '',
})

const form = reactive({
  code: '',
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  remark: '',
  status: 'active',
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Supplier[]>([])

const rules: FormRules = {
  code: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => (isEdit.value ? '编辑供应商' : '新增供应商'))

// --------------- 方法 ---------------
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockSuppliers]

    if (searchForm.code) {
      list = list.filter((s) => s.code.includes(searchForm.code))
    }
    if (searchForm.name) {
      list = list.filter((s) => s.name.includes(searchForm.name))
    }
    if (searchForm.status) {
      list = list.filter((s) => s.status === searchForm.status)
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
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Supplier) {
  isEdit.value = true
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.contact = row.contact
  form.phone = row.phone
  form.email = row.email
  form.address = row.address
  form.remark = row.remark
  form.status = row.status
  dialogVisible.value = true
}

function handleDelete(row: Supplier) {
  ElMessageBox.confirm(`确定要删除供应商「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    const idx = mockSuppliers.findIndex((s) => s.id === row.id)
    if (idx > -1) mockSuppliers.splice(idx, 1)
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
        const item = mockSuppliers.find((s) => s.id === editingId.value)
        if (item) Object.assign(item, { ...form })
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockSuppliers.map((s) => s.id), 0) + 1
        mockSuppliers.push({ id: newId, ...form })
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
  form.contact = ''
  form.phone = ''
  form.email = ''
  form.address = ''
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
