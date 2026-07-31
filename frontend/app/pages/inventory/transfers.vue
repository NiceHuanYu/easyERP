<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="min-width:120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已确认" value="CONFIRMED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增调拨</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width:100%">
        <el-table-column prop="transferNo" label="调拨单号" min-width="150" />
        <el-table-column prop="fromWarehouseName" label="调出仓库" min-width="120" />
        <el-table-column prop="toWarehouseName" label="调入仓库" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status==='DRAFT'?'info':'success'" size="small">{{ row.status==='DRAFT'?'草稿':'已确认' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status==='DRAFT'" link type="success" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status==='DRAFT'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="pagination.total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        background style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="isView">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="调出仓库" prop="fromWarehouseId">
              <el-select v-model="form.fromWarehouseId" placeholder="请选择" style="width:100%">
                <el-option v-for="w in warehouseOptions" :key="w.value" :label="w.label" :value="w.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入仓库" prop="toWarehouseId">
              <el-select v-model="form.toWarehouseId" placeholder="请选择" style="width:100%">
                <el-option v-for="w in warehouseOptions" :key="w.value" :label="w.label" :value="w.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>

        <el-divider content-position="left">调拨明细</el-divider>
        <div style="margin-bottom:8px" v-if="!isView">
          <el-button size="small" @click="addLine">添加行</el-button>
        </div>
        <el-table :data="form.lines" border stripe>
          <el-table-column label="物料" min-width="200">
            <template #default="{ row, $index }">
              <el-select v-model="row.materialId" placeholder="请选择物料" filterable style="width:100%" :disabled="isView"
                @change="(v:any) => onMaterialChange($index, v)">
                <el-option v-for="m in materialOptions" :key="m.value" :label="m.label" :value="m.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0" :precision="2" style="width:100%" :disabled="isView" />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" prop="unit" />
          <el-table-column label="操作" width="70" v-if="!isView">
            <template #default="{ $index }">
              <el-button :disabled="form.lines.length<=1" type="danger" size="small" @click="removeLine($index)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer v-if="!isView">
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const loading = ref(false)
const dialogVisible = ref(false)
const isView = ref(false)
const formRef = ref<FormInstance>()
const tableData = ref<any[]>([])
const searchForm = reactive({ status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive({
  fromWarehouseId: null as string | null, toWarehouseId: null as string | null,
  remark: '', lines: [] as any[],
})
const rules: FormRules = {
  fromWarehouseId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toWarehouseId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
}

const dialogTitle = computed(() => isView.value ? '查看调拨单' : '新增调拨')
const warehouseOptions = ref<{ label: string; value: string }[]>([])
const materialOptions = ref<{ label: string; value: string; unit: string }[]>([])

async function loadOptions() {
  const [wh, mat] = await Promise.all([
    api.page<any>('/base/warehouses', 1, 1000),
    api.page<any>('/base/materials', 1, 1000),
  ])
  warehouseOptions.value = wh.list.map((w: any) => ({ label: w.name, value: String(w.id) }))
  materialOptions.value = mat.list.map((m: any) => ({ label: m.name, value: String(m.id), unit: m.unit || '' }))
}

function onMaterialChange(index: number, val: string) {
  const m = materialOptions.value.find(o => o.value === val)
  if (m) { form.lines[index].unit = m.unit }
}

function addLine() { form.lines.push({ materialId: null, quantity: 0, unit: '' }) }
function removeLine(i: number) { if (form.lines.length > 1) form.lines.splice(i, 1) }
function resetForm() { Object.assign(form, { fromWarehouseId: null, toWarehouseId: null, remark: '', lines: [] }) }

async function fetchData() {
  loading.value = true
  try {
    const r = await api.page<any>('/inventory/transfers', pagination.page, pagination.pageSize,
      searchForm.status ? { status: searchForm.status } : undefined)
    tableData.value = r.list; pagination.total = r.total
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}
function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.status = ''; handleSearch() }
watch([() => pagination.page, () => pagination.pageSize], () => fetchData())

function handleAdd() { isView.value = false; resetForm(); addLine(); dialogVisible.value = true }

async function handleView(row: any) {
  isView.value = true
  try {
    const d = await api.get<any>('/inventory/transfers/' + row.id)
    const t = d.transfer
    form.fromWarehouseId = String(t.fromWarehouseId)
    form.toWarehouseId = String(t.toWarehouseId)
    form.remark = t.remark || ''
    form.lines = d.items.map((i: any) => ({ materialId: String(i.materialId), quantity: i.quantity, unit: i.unit || '', materialName: i.materialName }))
    dialogVisible.value = true
  } catch { ElMessage.error('加载失败') }
}

async function handleSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok || form.lines.length === 0) { ElMessage.warning('请添加调拨明细'); return }
  try {
    await api.post('/inventory/transfers', form)
    ElMessage.success('已保存'); dialogVisible.value = false; fetchData()
  } catch { ElMessage.error('保存失败') }
}

async function handleConfirm(row: any) {
  try { await api.post('/inventory/transfers/' + row.id + '/confirm'); ElMessage.success('已确认'); fetchData() }
  catch { ElMessage.error('确认失败') }
}
async function handleDelete(row: any) {
  try { await api.del('/inventory/transfers/' + row.id); ElMessage.success('已删除'); fetchData() }
  catch { ElMessage.error('删除失败') }
}

onMounted(() => { loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap:12px; }
.search-card :deep(.el-card__body) { padding-bottom:0; }
.table-toolbar { margin-bottom:12px; }
</style>
