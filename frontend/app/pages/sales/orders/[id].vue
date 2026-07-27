<template>
  <div class="order-detail-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/sales/orders')">返回</el-button>
      <h2 class="page-title">销售订单详情 - {{ order?.orderNo }}</h2>
      <el-tag v-if="order" :type="statusTagType(order.status)" size="large" style="margin-left: 12px">
        {{ statusLabel(order.status) }}
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <template v-if="order?.status === 'draft'">
        <el-button
          v-permission="'sales:order:update'"
          type="primary"
          @click="router.push(`/sales/orders/create?id=${order?.id}`)"
        >
          编辑
        </el-button>
        <el-popconfirm
          title="确认删除该订单？"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button v-permission="'sales:order:delete'" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
        <el-popconfirm title="确认提交该订单？" @confirm="handleSubmit">
          <template #reference>
            <el-button v-permission="'sales:order:submit'" type="warning">提交</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order?.status === 'submitted'">
        <el-popconfirm title="确认审核通过？" @confirm="handleApprove">
          <template #reference>
            <el-button v-permission="'sales:order:approve'" type="success">审核</el-button>
          </template>
        </el-popconfirm>
        <el-popconfirm title="确认反审核？" @confirm="handleUnapprove">
          <template #reference>
            <el-button v-permission="'sales:order:approve'" type="warning">反审核</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order?.status === 'approved'">
        <el-popconfirm title="确认生成生产工单？" @confirm="handleCreateProductionOrder">
          <template #reference>
            <el-button type="success">生成生产工单</el-button>
          </template>
        </el-popconfirm>
        <el-button type="warning" @click="handleCreateDelivery">生成发货单</el-button>
      </template>
    </div>

    <!-- 订单头信息 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="section-title">订单信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单编号">{{ order?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ order?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="order" :type="statusTagType(order.status)" size="small">
            {{ statusLabel(order.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ order?.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ order?.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="金额总计">
          <span class="amount-text">{{ order ? formatMoney(order.totalAmount) : '' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ order?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="制单人">{{ order?.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="制单时间">{{ order?.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 订单明细 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="section-title">订单明细</span>
      </template>
      <el-table :data="order?.lines" border stripe>
        <el-table-column label="序号" width="60" align="center">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="materialName" label="物料" />
        <el-table-column prop="quantity" label="数量" width="120" align="right" />
        <el-table-column prop="price" label="单价" width="140" align="right">
          <template #default="{ row }">{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 标签页区域 -->
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <!-- 发货记录 -->
        <el-tab-pane label="发货记录" name="deliveries">
          <el-table :data="deliveries" border stripe>
            <el-table-column prop="deliveryNo" label="发货单号" width="160" />
            <el-table-column prop="deliveryDate" label="发货日期" width="120" />
            <el-table-column prop="warehouseName" label="发货仓库" width="140" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
                  {{ row.status === 'draft' ? '草稿' : '已发货' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="router.push(`/sales/deliveries/create?id=${row.id}`)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="deliveries.length === 0" description="暂无发货记录" />
        </el-tab-pane>

        <!-- 操作历史 -->
        <el-tab-pane label="操作历史" name="history">
          <el-timeline v-if="histories.length > 0">
            <el-timeline-item
              v-for="h in histories"
              :key="h.id"
              :timestamp="h.time"
              placement="top"
            >
              {{ h.action }}
              <span v-if="h.operator" class="history-operator">
                — {{ h.operator }}
              </span>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无操作历史" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { formatMoney } from '~/utils'
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  draft: '草稿',
  submitted: '已提交',
  approved: '已审核',
  closed: '已关闭',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
    closed: 'default',
  }
  return map[s] ?? 'default'
}

// ==================== 类型 ====================
interface OrderLine {
  materialId: number
  materialName: string
  quantity: number
  price: number
  amount: number
}

interface SalesOrder {
  id: number
  orderNo: string
  customerId: number
  customerName: string
  orderDate: string
  deliveryDate: string
  totalAmount: number
  status: 'draft' | 'submitted' | 'approved' | 'closed'
  remark: string
  createdBy: string
  createdAt: string
  lines: OrderLine[]
}

interface Delivery {
  id: number
  deliveryNo: string
  deliveryDate: string
  warehouseName: string
  status: 'draft' | 'shipped'
}

interface History {
  id: number
  action: string
  operator: string
  time: string
}

// ==================== 数据 ====================
const order = ref<SalesOrder | null>(null)
const deliveries = ref<Delivery[]>([])
const histories = ref<History[]>([])

// ==================== 标签页 ====================
const activeTab = ref('deliveries')

// ==================== 操作 ====================
async function handleDelete() {
  try {
    await api.del(`/sales/orders/${route.params.id}`)
    ElMessage.success('删除成功')
    router.push('/sales/orders')
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function handleSubmit() {
  try {
    await api.post(`/sales/orders/${route.params.id}/submit`)
    ElMessage.success('提交成功')
    loadOrder()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  }
}

async function handleApprove() {
  try {
    await api.post(`/sales/orders/${route.params.id}/approve`)
    ElMessage.success('审核通过')
    loadOrder()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

async function handleUnapprove() {
  try {
    await api.post(`/sales/orders/${route.params.id}/unapprove`)
    ElMessage.success('已反审核')
    loadOrder()
  } catch (e: any) {
    ElMessage.error(e?.message || '反审核失败')
  }
}

function handleCreateProductionOrder() {
  router.push(`/production/orders/create?fromSalesOrder=${order.value?.id}`)
}

function handleCreateDelivery() {
  router.push(`/sales/deliveries/create?fromOrder=${order.value?.id}`)
}

// ==================== 加载 ====================
async function loadOrder() {
  try {
    const id = route.params.id as string
    order.value = await api.get<SalesOrder>(`/sales/orders/${id}/detail`)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载订单失败')
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.action-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.section-card {
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
}

.amount-text {
  color: #f56c6c;
  font-weight: 700;
}

.history-operator {
  color: #909399;
  font-size: 13px;
}
</style>
