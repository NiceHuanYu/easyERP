<template>
  <div class="stock-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="物料编码">
          <el-input v-model="searchForm.materialId" placeholder="请输入物料编码" clearable style="min-width:140px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="min-width:140px">
            <el-option
              v-for="wh in warehouseOptions"
              :key="wh.value"
              :label="wh.label"
              :value="wh.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="searchForm.onlyWithStock">仅显示有库存</el-checkbox>
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
        <div class="card-header">
          <span>库存列表</span>
          <el-button v-permission="'inventory:stock:export'" type="success" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        highlight-current-row
        @row-click="handleRowClick"
        style="cursor: pointer"
      >
        <el-table-column prop="materialName" label="物料名称" min-width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="quantity" label="库存数量" width="100" sortable />
        <el-table-column prop="availableQty" label="可用数量" width="100" sortable />
        <el-table-column prop="lockedQty" label="锁定数量" width="100" sortable />
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

    <!-- Stock Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="库存明细" size="500px">
      <template v-if="selectedMaterial">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物料名称">{{ selectedMaterial.materialName }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ selectedMaterial.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="库存数量">{{ selectedMaterial.quantity }}</el-descriptions-item>
          <el-descriptions-item label="可用数量">{{ selectedMaterial.availableQty }}</el-descriptions-item>
          <el-descriptions-item label="锁定数量">{{ selectedMaterial.lockedQty }}</el-descriptions-item>
          <el-descriptions-item label="在途数量">{{ selectedMaterial.lockedQty }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">最近流水</el-divider>
        <el-table :data="recentTransactions" stripe border size="small" max-height="300">
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" align="right" />
          <el-table-column prop="currentStock" label="结存" width="80" align="right" />
          <el-table-column prop="sourceNo" label="关联单号" min-width="130" />
        </el-table>
        <el-empty v-if="recentTransactions.length === 0" description="暂无流水记录" />
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../composables/useApi'
import { useAuthStore } from '../../../stores/auth'

const authStore = useAuthStore()

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface StockItem {
  id: number
  materialName: string
  warehouseName: string
  materialId: number
  warehouseId: number
  quantity: number
  availableQty: number
  lockedQty: number
}

interface TransactionLog {
  id: number
  createTime: string
  type: string
  quantity: number
  currentStock: number
  sourceNo: string
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  materialId: '',
  warehouseId: '',
  onlyWithStock: false,
})

const warehouseOptions = ref<{ label: string; value: number }[]>([])

async function loadWarehouseOptions() {
  try {
    const data = await api.page<{ id: number; name: string }>('/base/warehouses', 1, 1000)
    warehouseOptions.value = data.list.map((w) => ({ label: w.name, value: w.id }))
  } catch {
    // options load silently
  }
}

// ── Data ───────────────────────────────────────────
const tableData = ref<StockItem[]>([])
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// ── Fetch ──────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const query: Record<string, string | number | undefined> = {}
    if (searchForm.materialId) query.materialId = searchForm.materialId
    if (searchForm.warehouseId) query.warehouseId = searchForm.warehouseId
    if (searchForm.onlyWithStock) query.minQuantity = 0

    const result = await api.page<StockItem>(
      '/inventory/stock',
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

// ── Drawer / Row Click ─────────────────────────────
const drawerVisible = ref(false)
const selectedMaterial = ref<StockItem | null>(null)
const recentTransactions = ref<TransactionLog[]>([])

async function handleRowClick(row: StockItem) {
  selectedMaterial.value = row

  // Fetch recent transactions for this material+warehouse
  try {
    const txnResult = await api.page<TransactionLog>('/inventory/transactions', 1, 10, {
      materialId: row.materialId,
      warehouseId: row.warehouseId,
    })
    recentTransactions.value = txnResult.list
  } catch {
    recentTransactions.value = []
  }

  drawerVisible.value = true
}

// ── Actions ────────────────────────────────────────
function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.materialId = ''
  searchForm.warehouseId = ''
  searchForm.onlyWithStock = false
  pagination.page = 1
  fetchData()
}

async function handleExport() {
  const query: Record<string, string | number | undefined> = {}
  if (searchForm.materialId) query.materialId = searchForm.materialId
  if (searchForm.warehouseId) query.warehouseId = searchForm.warehouseId
  try {
    const blob = await $fetch<Blob>(`/api/v1/inventory/stock/export`, {
      method: 'GET',
      query,
      headers: { Authorization: `Bearer ${authStore.token}` },
      responseType: 'blob',
    })
    const url = URL.createObjectURL(blob as Blob)
    const a = document.createElement('a')
    a.href = url; a.download = 'inventory_stock.csv'; a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('已导出')
  } catch { ElMessage.error('导出失败') }
}

// ── Init ───────────────────────────────────────────
watch([() => pagination.page, () => pagination.pageSize], () => { fetchData() })
onMounted(() => {
  loadWarehouseOptions()
  fetchData()
})
</script>

<style scoped>
.stock-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.detail-desc {
  margin-bottom: 16px;
}
</style>
