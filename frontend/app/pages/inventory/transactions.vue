<template>
  <div class="transactions-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="物料编码">
          <el-input v-model="searchForm.materialId" placeholder="请输入物料编码" clearable />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable>
            <el-option
              v-for="wh in warehouseOptions"
              :key="wh.value"
              :label="wh.label"
              :value="wh.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option
              v-for="t in typeOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <span>库存流水</span>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column prop="createTime" label="日期" width="170" sortable />
        <el-table-column prop="materialName" label="物料名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type] || 'info'" size="small">
              {{ dictStore.getDictLabel('inv_transaction_type', row.type) || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="currentStock" label="结存" width="90" />
        <el-table-column prop="sourceNo" label="关联单号" width="150" />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../composables/useApi'
import { useDictStore } from '../../../stores/dict'

definePageMeta({ middleware: 'auth' })

const dictStore = useDictStore()

// ── Types ──────────────────────────────────────────
interface Transaction {
  transNo: string
  date: string
  materialCode: string
  materialName: string
  warehouse: string
  type: string
  quantity: number
  direction: '+' | '-'
  refNo: string
  operator: string
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  materialId: '',
  warehouseId: '',
  type: '' as string,
  dateRange: null as [string, string] | null,
})

const warehouseOptions = ref<{ label: string; value: number }[]>([])
const typeOptions = ref<{ label: string; value: string }[]>([])

async function loadOptions() {
  // 仓库
  try {
    const data = await api.page<{ id: number; name: string }>('/base/warehouses', 1, 1000)
    warehouseOptions.value = data.list.map((w) => ({ label: w.name, value: w.id }))
  } catch { /* ignore */ }
  // 库存变动类型（从字典加载）
  typeOptions.value = dictStore.getDictItems('inv_transaction_type')
    .map(d => ({ label: d.label, value: d.value }))
}

const typeTagMap: Record<string, 'success' | 'danger' | 'warning'> = {
  'PURCHASE_IN': 'success',
  'FINISH_IN': 'success',
  'PICKING_OUT': 'danger',
  'SALES_OUT': 'danger',
  'PICKING_RETURN': 'success',
  'TRANSFER': 'warning',
  'ADJUST': 'warning',
  'SCRAP': 'danger',
}

// ── Data ───────────────────────────────────────────
const tableData = ref<Transaction[]>([])
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

// ── Fetch ──────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const query: Record<string, string | number | undefined> = {}
    if (searchForm.materialId) query.materialId = searchForm.materialId
    if (searchForm.warehouseId) query.warehouseId = searchForm.warehouseId
    if (searchForm.type) query.type = searchForm.type
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      query.startTime = searchForm.dateRange[0]
      query.endTime = searchForm.dateRange[1]
    }

    const result = await api.page<Transaction>(
      '/inventory/transactions',
      pagination.page,
      pagination.pageSize,
      query,
    )
    tableData.value = result.list
    pagination.total = result.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// ── Actions ────────────────────────────────────────
function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.materialId = ''
  searchForm.warehouseId = ''
  searchForm.type = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchData()
}

// ── Init ───────────────────────────────────────────
watch([() => pagination.page, () => pagination.pageSize], () => { fetchData() })
onMounted(async () => {
  if (!dictStore.loaded) await dictStore.fetchAllDicts()
  loadOptions()
  fetchData()
})
</script>

<style scoped>
.transactions-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.direction-in {
  color: #67c23a;
  font-weight: 600;
}

.direction-out {
  color: #f56c6c;
  font-weight: 600;
}
</style>
