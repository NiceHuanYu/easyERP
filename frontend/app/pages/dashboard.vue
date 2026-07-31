<template>
  <div class="dashboard-page">
    <!-- 4 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card class="stat-card" shadow="never">
          <div class="stat-card-inner" :style="{ borderLeftColor: card.color }">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 3 图表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">近30天销售额趋势</span></template>
          <ClientChart class="chart" :option="salesTrendOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">库存分布（按仓库）</span></template>
          <ClientChart class="chart" :option="stockDistOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">销售订单状态分布</span></template>
          <ClientChart class="chart" :option="orderStatusOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="never">
      <template #header><span class="card-title">快捷操作</span></template>
      <div class="quick-actions">
        <el-button v-permission="'sales:order:create'" type="primary" :icon="Plus" @click="router.push('/sales/orders/create')">新建销售订单</el-button>
        <el-button v-permission="'purchase:order:create'" type="warning" :icon="Plus" @click="router.push('/purchase/requisitions/create')">新建采购申请</el-button>
        <el-button v-permission="'production:order:create'" type="success" :icon="Plus" @click="router.push('/production/orders/create')">新建生产工单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { DocumentChecked, Clock, Warning, Goods, Plus } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { api } from '../composables/useApi'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

definePageMeta({ middleware: 'auth' })
const router = useRouter()

interface StatCard { label: string; value: string; color: string; bg: string; icon: any }
const statCards = ref<StatCard[]>([])

async function fetchStats() {
  const cards: StatCard[] = [
    { label: '待审核销售订单', value: '...', color: '#409eff', bg: '#ecf5ff', icon: DocumentChecked },
    { label: '待审核采购申请', value: '...', color: '#e6a23c', bg: '#fdf6ec', icon: Goods },
    { label: '待处理生产工单', value: '...', color: '#f56c6c', bg: '#fef0f0', icon: Clock },
    { label: '库存预警', value: '...', color: '#e040fb', bg: '#fce4ec', icon: Warning },
  ]
  statCards.value = cards

  const [orders, reqs, prods, warn] = await Promise.allSettled([
    api.page('/sales/orders', 1, 1, { status: 'SUBMITTED' }),
    api.page('/purchase/requisitions', 1, 1, { status: 'SUBMITTED' }),
    api.page('/production/orders', 1, 1, { status: 'RELEASED' }),
    api.get<number>('/inventory/stock/warning-count'),
  ])
  if (orders.status === 'fulfilled') cards[0].value = String(orders.value.total)
  if (reqs.status === 'fulfilled') cards[1].value = String(reqs.value.total)
  if (prods.status === 'fulfilled') cards[2].value = String(prods.value.total)
  if (warn.status === 'fulfilled') cards[3].value = String(warn.value ?? 0)
  statCards.value = cards
}

// 销售额趋势
const salesTrendData = reactive({ dates: [] as string[], amounts: [] as number[] })
async function fetchSalesTrend() {
  try {
    const d = await api.get<{ dates: string[]; amounts: number[] }>('/dashboard/sales-trend')
    salesTrendData.dates = (d.dates || []).map(s => s.slice(5))
    salesTrendData.amounts = d.amounts || []
  } catch { /* */ }
}
const salesTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' as const }, grid: { left: 50, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category' as const, data: salesTrendData.dates },
  yAxis: { type: 'value' as const },
  series: [{ type: 'line', smooth: true, data: salesTrendData.amounts, areaStyle: { color: 'rgba(64,158,255,0.15)' } }],
}))

// 库存分布
const stockDistData = ref<{ name: string; value: number }[]>([])
async function fetchStockDist() {
  try {
    const list = await api.get<{ warehouseId: number; quantity: number }[]>('/dashboard/stock-dist')
    const wh = await api.page<{ id: number; name: string }>('/base/warehouses', 1, 100)
    const nameMap = new Map(wh.list.map(w => [w.id, w.name]))
    stockDistData.value = (list || []).map(s => ({ name: nameMap.get(s.warehouseId) || '仓库' + s.warehouseId, value: s.quantity }))
  } catch { /* */ }
}
const stockDistOption = computed(() => ({
  tooltip: { trigger: 'item' as const }, legend: { bottom: 0 },
  series: [{ type: 'pie', radius: ['45%','75%'], center: ['50%','47%'], data: stockDistData.value, label: { show: true } }],
}))

// 订单状态分布
const orderStatusData = ref<{ name: string; value: number }[]>([])
const statusLabel: Record<string, string> = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVED: '已审核', SHIPPED: '已发货', CLOSED: '已关闭' }
async function fetchOrderStatus() {
  try {
    const list = await api.get<{ status: string; count: number }[]>('/dashboard/order-status-dist')
    orderStatusData.value = (list || []).map(s => ({ name: statusLabel[s.status] || s.status, value: s.count }))
  } catch { /* */ }
}
const orderStatusOption = computed(() => ({
  tooltip: { trigger: 'item' as const }, legend: { bottom: 0 },
  series: [{ type: 'pie', radius: '65%', center: ['50%','47%'], data: orderStatusData.value, label: { show: true, formatter: '{b}: {c}' } }],
}))

onMounted(() => {
  fetchStats(); fetchSalesTrend(); fetchStockDist(); fetchOrderStatus()
})
</script>

<style scoped>
.dashboard-page { max-width: 1400px; }
.stat-row { margin-bottom: 16px; }
.stat-card { border-radius: 6px; }
.stat-card-inner { display: flex; align-items: center; gap: 14px; border-left: 3px solid; padding-left: 12px; }
.stat-icon { width: 50px; height: 50px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.chart-row { margin-bottom: 16px; }
.card-title { font-size: 15px; font-weight: 600; }
.chart { height: 300px; }
.quick-actions { display: flex; gap: 12px; }
</style>
