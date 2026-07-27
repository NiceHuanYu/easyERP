<template>
  <div class="stock-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="物料编码">
          <el-input v-model="searchForm.materialCode" placeholder="请输入物料编码" clearable />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="请选择仓库" clearable>
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
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="filteredData"
        stripe
        border
        highlight-current-row
        @row-click="handleRowClick"
        style="cursor: pointer"
      >
        <el-table-column prop="materialCode" label="物料编码" width="140" />
        <el-table-column prop="materialName" label="物料名称" min-width="150" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="warehouse" label="仓库" width="120" />
        <el-table-column prop="quantity" label="库存数量" width="100" sortable />
        <el-table-column prop="availableQty" label="可用数量" width="100" sortable />
        <el-table-column prop="lockedQty" label="锁定数量" width="100" sortable />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="filteredData.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
      />
    </el-card>

    <!-- Stock Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="库存明细"
      size="600px"
    >
      <template v-if="selectedMaterial">
        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="物料编码">{{ selectedMaterial.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedMaterial.materialName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ selectedMaterial.spec }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ selectedMaterial.unit }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">各仓库存明细</el-divider>

        <el-table :data="warehouseStockDetails" stripe border>
          <el-table-column prop="warehouse" label="仓库" />
          <el-table-column prop="quantity" label="库存数量" />
          <el-table-column prop="availableQty" label="可用数量" />
          <el-table-column prop="lockedQty" label="锁定数量" />
          <el-table-column prop="lastUpdateTime" label="最后更新" width="170" />
        </el-table>

        <el-divider content-position="left">最近流水</el-divider>

        <el-timeline>
          <el-timeline-item
            v-for="log in recentTransactions"
            :key="log.id"
            :timestamp="log.time"
            placement="top"
          >
            {{ log.description }}
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface StockItem {
  materialCode: string
  materialName: string
  spec: string
  unit: string
  warehouse: string
  quantity: number
  availableQty: number
  lockedQty: number
}

interface WarehouseStockDetail {
  warehouse: string
  quantity: number
  availableQty: number
  lockedQty: number
  lastUpdateTime: string
}

interface TransactionLog {
  id: string
  time: string
  description: string
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  materialCode: '',
  materialName: '',
  warehouse: '',
  onlyWithStock: false,
})

const warehouseOptions = [
  { label: '原料仓', value: '原料仓' },
  { label: '半成品仓', value: '半成品仓' },
  { label: '成品仓', value: '成品仓' },
  { label: '包材仓', value: '包材仓' },
  { label: '临调仓', value: '临调仓' },
]

// ── Mock Data ──────────────────────────────────────
function generateMockStockData(): StockItem[] {
  const materials = [
    { code: 'MAT-0001', name: '冷轧钢板 SPCC', spec: '2.0×1250×2500', unit: '吨' },
    { code: 'MAT-0002', name: '不锈钢管 304', spec: 'φ25×2.5×6000', unit: '根' },
    { code: 'MAT-0003', name: '铜芯电缆 YJV', spec: '3×4mm²', unit: '米' },
    { code: 'MAT-0004', name: 'ABS 塑料粒子', spec: 'PA-757', unit: 'kg' },
    { code: 'MAT-0005', name: '轴承 6205-2RS', spec: '25×52×15', unit: '个' },
    { code: 'MAT-0006', name: '齿轮油 L-CKC220', spec: '200L/桶', unit: '桶' },
    { code: 'MAT-0007', name: '螺栓 M12×50', spec: '8.8级 镀锌', unit: '个' },
    { code: 'MAT-0008', name: '密封圈 NBR', spec: 'φ50×3.5', unit: '个' },
    { code: 'MAT-0009', name: '铝型材 6063-T5', spec: '40×40×2.0', unit: '米' },
    { code: 'MAT-0010', name: '电机 Y2-132S-4', spec: '5.5kW 380V', unit: '台' },
    { code: 'MAT-0011', name: '焊条 J422', spec: 'φ3.2×350', unit: 'kg' },
    { code: 'MAT-0012', name: '油漆 环氧富锌', spec: '灰色 20kg/桶', unit: '桶' },
  ]

  const warehouses = ['原料仓', '半成品仓', '成品仓', '包材仓', '临调仓']

  const data: StockItem[] = []
  for (const mat of materials) {
    // Each material can be in 1-3 warehouses
    const whCount = 1 + Math.floor(Math.random() * 3)
    const shuffled = [...warehouses].sort(() => Math.random() - 0.5)
    for (let i = 0; i < whCount; i++) {
      const qty = Math.floor(Math.random() * 5000) + 50
      const locked = Math.floor(Math.random() * qty * 0.3)
      data.push({
        materialCode: mat.code,
        materialName: mat.name,
        spec: mat.spec,
        unit: mat.unit,
        warehouse: shuffled[i],
        quantity: qty,
        availableQty: qty - locked,
        lockedQty: locked,
      })
    }
  }
  return data
}

const mockData = ref<StockItem[]>(generateMockStockData())
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10 })

// ── Filtering ──────────────────────────────────────
const filteredData = computed(() => {
  let list = mockData.value

  if (searchForm.materialCode) {
    list = list.filter((item) =>
      item.materialCode.toLowerCase().includes(searchForm.materialCode.toLowerCase()),
    )
  }
  if (searchForm.materialName) {
    list = list.filter((item) =>
      item.materialName.includes(searchForm.materialName),
    )
  }
  if (searchForm.warehouse) {
    list = list.filter((item) => item.warehouse === searchForm.warehouse)
  }
  if (searchForm.onlyWithStock) {
    list = list.filter((item) => item.quantity > 0)
  }

  return list
})

// ── Drawer / Row Click ─────────────────────────────
const drawerVisible = ref(false)
const selectedMaterial = ref<StockItem | null>(null)
const warehouseStockDetails = ref<WarehouseStockDetail[]>([])
const recentTransactions = ref<TransactionLog[]>([])

function handleRowClick(row: StockItem) {
  selectedMaterial.value = row

  // Build warehouse details for this material across all warehouses
  const allEntries = mockData.value.filter(
    (item) => item.materialCode === row.materialCode,
  )
  warehouseStockDetails.value = allEntries.map((item) => ({
    warehouse: item.warehouse,
    quantity: item.quantity,
    availableQty: item.availableQty,
    lockedQty: item.lockedQty,
    lastUpdateTime: generateRecentDate(),
  }))

  // Mock recent transactions
  recentTransactions.value = [
    { id: '1', time: generateRecentDate(0), description: `出库至生产订单 PO-20241201，数量 ${Math.floor(Math.random() * 50) + 1}` },
    { id: '2', time: generateRecentDate(1), description: `采购入库，来自供应商 深圳钢贸有限公司，数量 ${Math.floor(Math.random() * 200) + 100}` },
    { id: '3', time: generateRecentDate(2), description: `调拨至 ${['原料仓', '半成品仓', '成品仓'][Math.floor(Math.random() * 3)]}，数量 ${Math.floor(Math.random() * 30) + 5}` },
    { id: '4', time: generateRecentDate(4), description: `生产入库，工单 WO-20241120，数量 ${Math.floor(Math.random() * 80) + 20}` },
    { id: '5', time: generateRecentDate(6), description: `盘点调整，差异 ${Math.random() > 0.5 ? '+' : '-'}${Math.floor(Math.random() * 10) + 1}` },
  ]

  drawerVisible.value = true
}

function generateRecentDate(daysAgo: number = 0): string {
  const d = new Date()
  d.setDate(d.getDate() - daysAgo)
  d.setHours(8 + Math.floor(Math.random() * 10), Math.floor(Math.random() * 60), Math.floor(Math.random() * 60))
  return d.toLocaleString('zh-CN', { hour12: false })
}

// ── Actions ────────────────────────────────────────
function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  searchForm.materialCode = ''
  searchForm.materialName = ''
  searchForm.warehouse = ''
  searchForm.onlyWithStock = false
  pagination.page = 1
}

function handleExport() {
  // In production this would call an API endpoint
  ElMessage.success('导出成功，文件下载中...')
}
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
