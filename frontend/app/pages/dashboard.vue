<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-card-inner" style="border-left-color: #409eff">
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff">
              <el-icon :size="28"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ stats.pendingOrders }}</div>
              <div class="stat-label">待审核销售订单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-card-inner" style="border-left-color: #e6a23c">
            <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ stats.pendingProductions }}</div>
              <div class="stat-label">待处理生产工单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-card-inner" style="border-left-color: #f56c6c">
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ stats.inventoryAlerts }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-card-inner" style="border-left-color: #67c23a">
            <div class="stat-icon" style="background: #f0f9eb; color: #67c23a">
              <el-icon :size="28"><Coin /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ formatMoney(stats.totalARAP) }}</div>
              <div class="stat-label">应收/应付总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近30天销售额趋势</span>
          </template>
          <ClientChart class="chart" :option="salesTrendOption" autoresize />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">库存分布（按仓库）</span>
          </template>
          <ClientChart class="chart" :option="inventoryDistOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="never" class="quick-actions-card">
      <template #header>
        <span class="chart-title">快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="navigateTo('/sales/orders/create')">
          <el-icon><Plus /></el-icon>
          新建销售订单
        </el-button>
        <el-button type="success" @click="navigateTo('/purchase/requisitions')">
          <el-icon><Plus /></el-icon>
          新建采购申请
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { DocumentChecked, Clock, Warning, Coin, Plus } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
} from 'echarts/components'
import { api } from '../composables/useApi'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
])

definePageMeta({ middleware: 'auth' })

// ---- Stats (fetched from APIs, fallback to 0) ----
const stats = reactive({
  pendingOrders: 0,
  pendingProductions: 0,
  inventoryAlerts: 0,
  totalARAP: 0,
})

async function fetchStats() {
  // 待审核销售订单
  try {
    const orders = await api.page('/sales/orders', 1, 1, { status: 'SUBMITTED' })
    stats.pendingOrders = orders.total
  } catch { /* keep default */ }

  // 待处理生产工单
  try {
    const productions = await api.page('/production/orders', 1, 1, { status: 'DRAFT' })
    stats.pendingProductions = productions.total
  } catch { /* keep default */ }

  // 库存预警（后端独立 endpoint）
  try {
    const count = await api.get<number>('/inventory/stock/warning-count')
    stats.inventoryAlerts = count ?? 0
  } catch { /* keep default */ }

  // 应收+应付总额
  try {
    const [ar, ap] = await Promise.all([
      api.page('/finance/receivables', 1, 1),
      api.page('/finance/payables', 1, 1),
    ])
    stats.totalARAP = (ar.total + ap.total) * 10000
  } catch { /* keep default */ }
}

onMounted(() => {
  fetchStats()
})

function formatMoney(value: number): string {
  return '¥' + value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 近30天销售额趋势 — Mock
const salesTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' as const },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category' as const,
    boundaryGap: false,
    data: Array.from({ length: 30 }, (_, i) => `${i + 1}日`),
  },
  yAxis: {
    type: 'value' as const,
    axisLabel: { formatter: '¥{value}' },
  },
  series: [
    {
      name: '销售额',
      type: 'line' as const,
      smooth: true,
      lineStyle: { color: '#409eff', width: 2 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64,158,255,0.25)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' },
          ],
        },
      },
      data: [
        42000, 38000, 51000, 46000, 53000, 48000, 56000, 49000, 61000, 55000,
        44000, 52000, 58000, 47000, 50000, 54000, 62000, 57000, 45000, 59000,
        63000, 48000, 51000, 55000, 60000, 52000, 49000, 56000, 58000, 53000,
      ],
    },
  ],
}))

// 库存分布 — Mock
const inventoryDistOption = computed(() => ({
  tooltip: { trigger: 'item' as const, formatter: '{b}: {c} 件 ({d}%)' },
  legend: { bottom: 0 },
  series: [
    {
      name: '库存分布',
      type: 'pie' as const,
      radius: ['45%', '75%'],
      center: ['50%', '47%'],
      label: { show: true },
      emphasis: {
        label: { fontSize: 16, fontWeight: 'bold' },
      },
      data: [
        { value: 3240, name: '原材料仓' },
        { value: 1560, name: '半成品仓' },
        { value: 2150, name: '成品仓' },
        { value: 870, name: '包材仓' },
        { value: 420, name: '备品备件仓' },
      ],
    },
  ],
}))
</script>

<style scoped>
.dashboard-page {
  max-width: 1400px;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 6px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 3px solid;
  padding-left: 12px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart {
  height: 340px;
}

.quick-actions-card {
  margin-bottom: 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
}
</style>
