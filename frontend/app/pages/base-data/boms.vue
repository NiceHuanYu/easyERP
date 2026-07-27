<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="成品物料">
          <el-select
            v-model="searchForm.productMaterialId"
            placeholder="请选择成品物料"
            clearable
            filterable
            remote
            :remote-method="remoteSearchMaterial"
            :loading="materialSearchLoading"
          >
            <el-option
              v-for="m in materialOptions"
              :key="m.id"
              :label="`${m.code} - ${m.name}`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="BOM编号">
          <el-input v-model="searchForm.bomNo" placeholder="请输入BOM编号" clearable />
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
        <el-button type="primary" :icon="Plus" v-permission="'base-data:bom:create'" @click="handleAdd">
          新增BOM
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="bomNo" label="BOM编号" width="130" />
        <el-table-column prop="productMaterialCode" label="成品物料" width="130" />
        <el-table-column prop="productMaterialName" label="成品名称" width="160" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button link type="primary" :icon="Edit" v-permission="'base-data:bom:edit'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'base-data:bom:delete'" @click="handleDelete(row)">
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
      width="800px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成品物料" prop="productMaterialId">
              <el-select
                v-model="form.productMaterialId"
                placeholder="请选择成品物料"
                filterable
                style="width: 100%"
                :disabled="isView"
                @change="onProductMaterialChange"
              >
                <el-option
                  v-for="m in materialOptions"
                  :key="m.id"
                  :label="`${m.code} - ${m.name}`"
                  :value="m.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="请输入版本号" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status" :disabled="isView">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- BOM 明细表 -->
      <div class="bom-details-section">
        <div class="bom-details-header">
          <span class="bom-details-title">BOM 明细</span>
          <el-button
            v-if="!isView"
            type="primary"
            size="small"
            :icon="Plus"
            @click="addDetailRow"
          >
            添加明细
          </el-button>
        </div>
        <el-table :data="form.details" border stripe size="small">
          <el-table-column label="序号" type="index" width="60" />
          <el-table-column label="原材料" width="220">
            <template #default="{ row: detail }">
              <el-select
                v-model="detail.materialId"
                placeholder="选择原材料"
                filterable
                size="small"
                style="width: 100%"
                :disabled="isView"
                @change="(val: number) => onDetailMaterialChange(val, detail)"
              >
                <el-option
                  v-for="m in rawMaterialOptions"
                  :key="m.id"
                  :label="`${m.code} - ${m.name}`"
                  :value="m.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="用量" width="120">
            <template #default="{ row: detail }">
              <el-input-number
                v-model="detail.quantity"
                :min="0"
                :precision="4"
                size="small"
                style="width: 100%"
                :disabled="isView"
              />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80">
            <template #default="{ row: detail }">
              {{ detail.unit }}
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160">
            <template #default="{ row: detail }">
              <el-input
                v-model="detail.remark"
                placeholder="可选备注"
                size="small"
                :disabled="isView"
              />
            </template>
          </el-table-column>
          <el-table-column v-if="!isView" label="操作" width="70" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" :icon="Delete" @click="removeDetailRow($index)" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ middleware: 'auth' })

// --------------- 类型 ---------------
interface MaterialOption {
  id: number
  code: string
  name: string
  unit: string
  category: string
}

interface BomDetail {
  materialId: number | null
  materialCode: string
  materialName: string
  quantity: number
  unit: string
  remark: string
}

interface Bom {
  id: number
  bomNo: string
  productMaterialId: number
  productMaterialCode: string
  productMaterialName: string
  version: string
  status: string
  details: BomDetail[]
}

// --------------- Mock 物料选项 ---------------
const allMaterials: MaterialOption[] = [
  { id: 1, code: 'MAT-0001', name: '钢板A', unit: 'kg', category: 'raw' },
  { id: 2, code: 'MAT-0002', name: '螺丝B', unit: '个', category: 'raw' },
  { id: 3, code: 'MAT-0003', name: '齿轮C', unit: '个', category: 'semi' },
  { id: 4, code: 'MAT-0004', name: '轴承D', unit: '套', category: 'semi' },
  { id: 5, code: 'MAT-0005', name: '成品设备X', unit: '台', category: 'finished' },
  { id: 6, code: 'MAT-0006', name: '成品设备Y', unit: '台', category: 'finished' },
  { id: 7, code: 'MAT-0007', name: '弹簧E', unit: '个', category: 'raw' },
  { id: 8, code: 'MAT-0008', name: '密封圈F', unit: '个', category: 'raw' },
  { id: 9, code: 'MAT-0009', name: '包装箱G', unit: '个', category: 'packaging' },
  { id: 10, code: 'MAT-0010', name: '半成品H', unit: '件', category: 'semi' },
]

// --------------- Mock BOM 数据 ---------------
const mockBoms: Bom[] = [
  {
    id: 1,
    bomNo: 'BOM-2024-0001',
    productMaterialId: 5,
    productMaterialCode: 'MAT-0005',
    productMaterialName: '成品设备X',
    version: 'V1.0',
    status: 'active',
    details: [
      { materialId: 1, materialCode: 'MAT-0001', materialName: '钢板A', quantity: 10, unit: 'kg', remark: '' },
      { materialId: 2, materialCode: 'MAT-0002', materialName: '螺丝B', quantity: 24, unit: '个', remark: '' },
      { materialId: 4, materialCode: 'MAT-0004', materialName: '轴承D', quantity: 4, unit: '套', remark: '' },
    ],
  },
  {
    id: 2,
    bomNo: 'BOM-2024-0002',
    productMaterialId: 6,
    productMaterialCode: 'MAT-0006',
    productMaterialName: '成品设备Y',
    version: 'V2.1',
    status: 'active',
    details: [
      { materialId: 1, materialCode: 'MAT-0001', materialName: '钢板A', quantity: 15, unit: 'kg', remark: '' },
      { materialId: 3, materialCode: 'MAT-0003', materialName: '齿轮C', quantity: 8, unit: '个', remark: '' },
      { materialId: 7, materialCode: 'MAT-0007', materialName: '弹簧E', quantity: 12, unit: '个', remark: '高精度' },
    ],
  },
  {
    id: 3,
    bomNo: 'BOM-2024-0003',
    productMaterialId: 5,
    productMaterialCode: 'MAT-0005',
    productMaterialName: '成品设备X',
    version: 'V1.1',
    status: 'inactive',
    details: [
      { materialId: 1, materialCode: 'MAT-0001', materialName: '钢板A', quantity: 12, unit: 'kg', remark: '' },
      { materialId: 8, materialCode: 'MAT-0008', materialName: '密封圈F', quantity: 6, unit: '个', remark: '' },
    ],
  },
]

// --------------- 状态 ---------------
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const materialSearchLoading = ref(false)

const searchForm = reactive({
  productMaterialId: null as number | null,
  bomNo: '',
})

const form = reactive({
  productMaterialId: null as number | null,
  version: '',
  status: 'active',
  details: [] as BomDetail[],
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Bom[]>([])

// 物料选项（用于搜索和表单的 select）
const materialOptions = ref<MaterialOption[]>(allMaterials)
// 原材料选项（用于 BOM 明细）
const rawMaterialOptions = computed(() =>
  allMaterials.filter((m) => m.category === 'raw' || m.category === 'semi')
)

const rules: FormRules = {
  productMaterialId: [{ required: true, message: '请选择成品物料', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => {
  if (isView.value) return '查看BOM详情'
  return isEdit.value ? '编辑BOM' : '新增BOM'
})

// --------------- 方法 ---------------
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockBoms]

    if (searchForm.productMaterialId) {
      list = list.filter((b) => b.productMaterialId === searchForm.productMaterialId)
    }
    if (searchForm.bomNo) {
      list = list.filter((b) => b.bomNo.includes(searchForm.bomNo))
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
  searchForm.productMaterialId = null
  searchForm.bomNo = ''
  pagination.page = 1
  fetchData()
}

function remoteSearchMaterial(query: string) {
  materialSearchLoading.value = true
  setTimeout(() => {
    if (query) {
      materialOptions.value = allMaterials.filter(
        (m) => m.code.includes(query) || m.name.includes(query)
      )
    } else {
      materialOptions.value = allMaterials
    }
    materialSearchLoading.value = false
  }, 200)
}

function handleAdd() {
  isEdit.value = false
  isView.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleView(row: Bom) {
  isEdit.value = false
  isView.value = true
  editingId.value = row.id
  form.productMaterialId = row.productMaterialId
  form.version = row.version
  form.status = row.status
  form.details = row.details.map((d) => ({ ...d }))
  dialogVisible.value = true
}

function handleEdit(row: Bom) {
  isEdit.value = true
  isView.value = false
  editingId.value = row.id
  form.productMaterialId = row.productMaterialId
  form.version = row.version
  form.status = row.status
  form.details = row.details.map((d) => ({ ...d }))
  dialogVisible.value = true
}

function handleDelete(row: Bom) {
  ElMessageBox.confirm(`确定要删除 BOM「${row.bomNo}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    const idx = mockBoms.findIndex((b) => b.id === row.id)
    if (idx > -1) mockBoms.splice(idx, 1)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function onProductMaterialChange(val: number | null) {
  // 自动填充成品物料信息
}

function onDetailMaterialChange(val: number | null, detail: BomDetail) {
  const mat = allMaterials.find((m) => m.id === val)
  if (mat) {
    detail.materialCode = mat.code
    detail.materialName = mat.name
    detail.unit = mat.unit
  } else {
    detail.materialCode = ''
    detail.materialName = ''
    detail.unit = ''
  }
}

function addDetailRow() {
  form.details.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    quantity: 0,
    unit: '',
    remark: '',
  })
}

function removeDetailRow(index: number) {
  form.details.splice(index, 1)
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    if (form.details.length === 0) {
      ElMessage.warning('请至少添加一条 BOM 明细')
      return
    }
    // 校验明细
    const hasEmptyDetail = form.details.some((d) => !d.materialId || d.quantity <= 0)
    if (hasEmptyDetail) {
      ElMessage.warning('请完善 BOM 明细（原材料和用量必填）')
      return
    }

    submitLoading.value = true
    setTimeout(() => {
      const productMat = allMaterials.find((m) => m.id === form.productMaterialId)
      const payload = {
        productMaterialId: form.productMaterialId!,
        productMaterialCode: productMat?.code ?? '',
        productMaterialName: productMat?.name ?? '',
        version: form.version,
        status: form.status,
        details: form.details.map((d) => ({ ...d })),
      }

      if (isEdit.value && editingId.value !== null) {
        const item = mockBoms.find((b) => b.id === editingId.value)
        if (item) Object.assign(item, payload)
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockBoms.map((b) => b.id), 0) + 1
        const newBomNo = `BOM-${new Date().getFullYear()}-${String(newId).padStart(4, '0')}`
        mockBoms.push({ id: newId, bomNo: newBomNo, ...payload })
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
  form.productMaterialId = null
  form.version = ''
  form.status = 'active'
  form.details = []
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

.bom-details-section {
  margin-top: 16px;
}

.bom-details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.bom-details-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>
