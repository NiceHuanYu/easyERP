<template>
  <div class="transactions-page">
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
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="入库" value="入库" />
            <el-option label="出库" value="出库" />
            <el-option label="调拨" value="调拨" />
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
        :data="paginatedData"
        stripe
        border
      >
        <el-table-column prop="transNo" label="流水号" width="160" />
        <el-table-column prop="date" label="日期" width="110" sortable />
        <el-table-column prop="materialCode" label="物料编码" width="130" />
        <el-table-column prop="materialName" label="物料名称" min-width="150" />
        <el-table-column prop="warehouse" label="仓库" width="110" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag
              :type="typeTagMap[row.type]"
              size="small"
              effect="plain"
            >
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="direction" label="方向" width="70">
          <template #default="{ row }">
            <span :class="row.direction === '+' ? 'direction-in' : 'direction-out'">
              {{ row.direction }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="refNo" label="关联单号" width="150" />
        <el-table-column prop="operator" label="操作人" width="100" />
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
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft } from '@element-plus/icons-vue'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface Transaction {
  transNo: string
  date: string
  materialCode: string
  materialName: string
  warehouse: string
  type: '入库' | '出库' | '调拨'
  quantity: number
  direction: '+' | '-'
  refNo: string
  operator: string
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  materialCode: '',
  materialName: '',
  warehouse: '',
  type: '' as string,
  dateRange: null as [string, string] | null,
})

const warehouseOptions = [
  { label: '原料仓', value: '原料仓' },
  { label: '半成品仓', value: '半成品仓' },
  { label: '成品仓', value: '成品仓' },
  { label: '包材仓', value: '包材仓' },
  { label: '临调仓', value: '临调仓' },
]

const typeTagMap: Record<string, 'success' | 'danger' | 'warning'> = {
  '入库': 'success',
  '出库': 'danger',
  '调拨': 'warning',
}

// ── Mock Data ──────────────────────────────────────
function generateMockTransactions(): Transaction[] {
  const materials = [
    { code: 'MAT-0001', name: '冷轧钢板 SPCC' },
    { code: 'MAT-0002', name: '不锈钢管 304' },
    { code: 'MAT-0003', name: '铜芯电缆 YJV' },
    { code: 'MAT-0004', name: 'ABS 塑料粒子' },
    { code: 'MAT-0005', name: '轴承 6205-2RS' },
    { code: 'MAT-0006', name: '齿轮油 L-CKC220' },
    { code: 'MAT-0007', name: '螺栓 M12×50' },
    { code: 'MAT-0008', name: '密封圈 NBR' },
  ]

  const warehouses = ['原料仓', '半成品仓', '成品仓', '包材仓', '临调仓']
  const types: Array<'入库' | '出库' | '调拨'> = ['入库', '出库', '调拨']
  const operators = ['张伟', '李娜', '王强', '赵敏', '陈刚']
  const refPrefixes: Record<string, string> = {
    '入库': 'PO-RCV-',
    '出库': 'SO-DLV-',
    '调拨': 'TR-',
  }

  const data: Transaction[] = []
  for (let i = 0; i < 60; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const mat = materials[Math.floor(Math.random() * materials.length)]
    const date = new Date()
    date.setDate(date.getDate() - Math.floor(Math.random() * 90))
    date.setHours(8 + Math.floor(Math.random() * 10), Math.floor(Math.random() * 60), Math.floor(Math.random() * 60))

    const direction = type === '出库' ? '-' : '+'
    const qty = Math.floor(Math.random() * 500) + 10

    data.push({
      transNo: `TXN-${String(i + 1).padStart(6, '0')}`,
      date: date.toLocaleDateString('zh-CN'),
      materialCode: mat.code,
      materialName: mat.name,
      warehouse: warehouses[Math.floor(Math.random() * warehouses.length)],
      type,
      quantity: qty,
      direction,
      refNo: `${refPrefixes[type]}${String(Math.floor(Math.random() * 9999) + 1).padStart(4, '0')}`,
      operator: operators[Math.floor(Math.random() * operators.length)],
    })
  }

  // Sort by date descending
  data.sort((a, b) => b.date.localeCompare(a.date) || b.transNo.localeCompare(a.transNo))
  return data
}

const mockData = ref<Transaction[]>(generateMockTransactions())
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 20 })

// ── Filtering ──────────────────────────────────────
const filteredData = computed(() => {
  let list = mockData.value

  if (searchForm.materialCode) {
    list = list.filter((item) =>
      item.materialCode.toLowerCase().includes(searchForm.materialCode.toLowerCase()),
    )
  }
  if (searchForm.materialName) {
    list = list.filter((item) => item.materialName.includes(searchForm.materialName))
  }
  if (searchForm.warehouse) {
    list = list.filter((item) => item.warehouse === searchForm.warehouse)
  }
  if (searchForm.type) {
    list = list.filter((item) => item.type === searchForm.type)
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [start, end] = searchForm.dateRange
    list = list.filter((item) => item.date >= start && item.date <= end)
  }

  return list
})

const paginatedData = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredData.value.slice(start, start + pagination.pageSize)
})

// ── Actions ────────────────────────────────────────
function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  searchForm.materialCode = ''
  searchForm.materialName = ''
  searchForm.warehouse = ''
  searchForm.type = ''
  searchForm.dateRange = null
  pagination.page = 1
}
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
