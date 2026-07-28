<template>
  <div class="order-detail-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/sales/orders')">返回</el-button>
      <h2 class="page-title">销售订单详情 - {{ order?.orderNo }}</h2>
      <el-tag v-if="order" :type="statusTagType(order.status)" size="large" style="margin-left:12px">
        {{ statusLabel(order.status) }}
      </el-tag>
    </div>

    <div class="action-bar">
      <template v-if="order?.status === 'DRAFT'">
        <el-button v-permission="'sales:order:update'" type="primary" @click="router.push(`/sales/orders/create?id=${order?.id}`)">编辑</el-button>
        <el-popconfirm title="确认删除？" @confirm="handleDelete"><template #reference><el-button v-permission="'sales:order:delete'" type="danger">删除</el-button></template></el-popconfirm>
        <el-popconfirm title="确认提交？" @confirm="handleSubmit"><template #reference><el-button v-permission="'sales:order:submit'" type="warning">提交</el-button></template></el-popconfirm>
      </template>
      <template v-else-if="order?.status === 'SUBMITTED'">
        <el-popconfirm title="确认审核通过？" @confirm="handleApprove"><template #reference><el-button v-permission="'sales:order:approve'" type="success">审核</el-button></template></el-popconfirm>
      </template>
      <template v-else-if="order?.status === 'APPROVED'">
        <el-popconfirm title="确认反审核？" @confirm="handleUnapprove"><template #reference><el-button v-permission="'sales:order:approve'" type="warning">反审核</el-button></template></el-popconfirm>
        <el-popconfirm title="确认生成生产工单？" @confirm="handleCreateProductionOrder"><template #reference><el-button type="success">生成生产工单</el-button></template></el-popconfirm>
        <el-button type="warning" @click="handleCreateDelivery">生成发货单</el-button>
      </template>
    </div>

    <el-card shadow="never" class="section-card">
      <template #header><span class="section-title">订单信息</span></template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单编号">{{ order?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ getCustomerName(order?.customerId) }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag v-if="order" :type="statusTagType(order.status)" size="small">{{ statusLabel(order.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ order?.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ order?.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="金额总计"><span class="amount-text">{{ order ? formatMoney(order.totalAmount) : '' }}</span></el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ order?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header><span class="section-title">订单明细</span></template>
      <el-table :data="order?.lines" border stripe>
        <el-table-column label="序号" width="60" align="center"><template #default="{ $index }">{{ $index + 1 }}</template></el-table-column>
        <el-table-column prop="materialName" label="物料" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column label="单位" width="80"><template #default="{ row }">{{ row.unit }}</template></el-table-column>
        <el-table-column prop="price" label="单价" width="120" align="right"><template #default="{ row }">{{ formatMoney(row.price) }}</template></el-table-column>
        <el-table-column label="金额" width="120" align="right"><template #default="{ row }">{{ formatMoney(row.amount) }}</template></el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="发货记录" name="deliveries">
          <el-table :data="deliveries" border stripe>
            <el-table-column prop="deliveryNo" label="发货单号" width="160" />
            <el-table-column prop="deliveryDate" label="发货日期" width="120" />
            <el-table-column prop="warehouseName" label="发货仓库" width="140" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'DRAFT' ? 'info' : 'success'" size="small">{{ row.status === 'DRAFT' ? '草稿' : '已发货' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="deliveries.length === 0" description="暂无发货记录" />
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

const statusMap: Record<string, string> = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVED: '已审核', CLOSED: '已关闭' }
function statusLabel(s: string): string { return statusMap[s] ?? s }
function statusTagType(s: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = { DRAFT: 'info', SUBMITTED: 'warning', APPROVED: 'success', CLOSED: 'default' }
  return map[s] ?? 'default'
}

interface OrderLine { materialId: number; materialName: string; quantity: number; unit: string; price: number; amount: number }
interface SalesOrder { id: number; orderNo: string; customerId: number; orderDate: string; deliveryDate: string; totalAmount: number; status: string; remark: string; lines: OrderLine[] }
interface Delivery { id: number; deliveryNo: string; deliveryDate: string; warehouseName: string; status: string }

const order = ref<SalesOrder | null>(null)
const deliveries = ref<Delivery[]>([])
const activeTab = ref('deliveries')
const customerOptions = ref<{ label: string; value: number }[]>([])
const materialOptions = ref<{ id: number; unit: string }[]>([])

function getCustomerName(id?: number): string {
  if (!id) return '-'
  return customerOptions.value.find((c) => c.value === id)?.label ?? String(id)
}

async function loadOrder() {
  try {
    const id = route.params.id as string
    const [header, detail] = await Promise.all([
      api.get<SalesOrder>(`/sales/orders/${id}`),
      api.get<{ lines: OrderLine[] }>(`/sales/orders/${id}/detail`),
    ])
    order.value = { ...header, lines: detail.lines || [] }
    // Resolve unit from material options
    for (const line of order.value.lines) {
      if (!line.unit) {
        const mat = materialOptions.value.find((m) => m.id === line.materialId)
        if (mat) line.unit = mat.unit || ''
      }
    }
  } catch (e: any) { ElMessage.error(e?.message || '加载失败') }
}

async function loadCustomerOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/customers', 1, 1000)
    customerOptions.value = result.list.map((i) => ({ label: i.name, value: i.id }))
  } catch { /* ignore */ }
}

async function loadMaterialOptions() {
  try {
    const result = await api.page<{ id: number; unit: string }>('/base/materials', 1, 1000)
    materialOptions.value = result.list.map((i) => ({ id: i.id, unit: i.unit || '' }))
  } catch { /* ignore */ }
}

async function handleDelete() {
  try { await api.del(`/sales/orders/${route.params.id}`); ElMessage.success('删除成功'); router.push('/sales/orders') }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}
async function handleSubmit() {
  try { await api.post(`/sales/orders/${route.params.id}/submit`); ElMessage.success('提交成功'); loadOrder() }
  catch (e: any) { ElMessage.error(e?.message || '提交失败') }
}
async function handleApprove() {
  try { await api.post(`/sales/orders/${route.params.id}/approve`); ElMessage.success('审核通过'); loadOrder() }
  catch (e: any) { ElMessage.error(e?.message || '审核失败') }
}
async function handleUnapprove() {
  try { await api.post(`/sales/orders/${route.params.id}/unapprove`); ElMessage.success('已反审核'); loadOrder() }
  catch (e: any) { ElMessage.error(e?.message || '反审核失败') }
}
function handleCreateProductionOrder() { router.push(`/production/orders/create?fromSalesOrder=${route.params.id}`) }
function handleCreateDelivery() { router.push(`/sales/deliveries/create?fromOrder=${route.params.id}`) }

onMounted(() => { loadOrder(); loadCustomerOptions(); loadMaterialOptions() })
</script>

<style scoped>
.order-detail-page { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }
.action-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.section-card { margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 600; }
.amount-text { color: #f56c6c; font-weight: 600; font-size: 16px; }
</style>
