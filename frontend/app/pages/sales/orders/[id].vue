<template>
  <div class="order-detail-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/sales/orders')">返回</el-button>
      <h2 class="page-title">销售订单详情 - {{ order.orderNo }}</h2>
      <el-tag :type="statusTagType(order.status)" size="large" style="margin-left: 12px">
        {{ statusLabel(order.status) }}
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <template v-if="order.status === 'draft'">
        <el-button
          v-permission="'sales:order:edit'"
          type="primary"
          @click="router.push(`/sales/orders/create?id=${order.id}`)"
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

      <template v-else-if="order.status === 'submitted'">
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

      <template v-else-if="order.status === 'approved'">
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
        <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ order.customerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(order.status)" size="small">
            {{ statusLabel(order.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ order.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ order.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="金额总计">
          <span class="amount-text">{{ formatMoney(order.totalAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ order.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="制单人">{{ order.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="制单时间">{{ order.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 订单明细 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="section-title">订单明细</span>
      </template>
      <el-table :data="order.lines" border stripe>
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

// ==================== Mock 数据 ====================
const order = ref<SalesOrder>({
  id: 1,
  orderNo: 'SO-00001',
  customerId: 1,
  customerName: '华为技术有限公司',
  orderDate: '2025-06-15',
  deliveryDate: '2025-07-01',
  totalAmount: 11550.0,
  status: 'approved',
  remark: '紧急订单，请优先处理',
  createdBy: '张三',
  createdAt: '2025-06-15 10:30:00',
  lines: [
    { materialId: 1, materialName: 'PCB-001 主板基板', quantity: 100, price: 25.5, amount: 2550.0 },
    { materialId: 2, materialName: 'CPU-002 中央处理器', quantity: 50, price: 180.0, amount: 9000.0 },
  ],
})

const deliveries = ref<Delivery[]>([
  {
    id: 1,
    deliveryNo: 'DLV-00001',
    deliveryDate: '2025-07-02',
    warehouseName: '成品仓A',
    status: 'shipped',
  },
  {
    id: 2,
    deliveryNo: 'DLV-00002',
    deliveryDate: '2025-07-05',
    warehouseName: '成品仓B',
    status: 'draft',
  },
])

const histories = ref<History[]>([
  { id: 1, action: '创建订单', operator: '张三', time: '2025-06-15 10:30:00' },
  { id: 2, action: '提交订单', operator: '张三', time: '2025-06-15 11:00:00' },
  { id: 3, action: '审核通过', operator: '李四', time: '2025-06-15 14:20:00' },
])

// ==================== 标签页 ====================
const activeTab = ref('deliveries')

// ==================== 操作 ====================
function handleDelete() {
  ElMessage.success('删除成功')
  router.push('/sales/orders')
}

function handleSubmit() {
  order.value.status = 'submitted'
  ElMessage.success('提交成功')
}

function handleApprove() {
  order.value.status = 'approved'
  ElMessage.success('审核通过')
}

function handleUnapprove() {
  order.value.status = 'submitted'
  ElMessage.success('已反审核')
}

function handleCreateProductionOrder() {
  router.push(`/production/orders/create?fromSalesOrder=${order.value.id}`)
}

function handleCreateDelivery() {
  router.push(`/sales/deliveries/create?fromOrder=${order.value.id}`)
}

// ==================== 加载 ====================
onMounted(() => {
  // 实际开发中根据 route.params.id 调用 API
  const id = route.params.id
  // 这里使用 mock 数据，可在此根据 id 加载不同数据
  console.log('Loading order:', id)
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
