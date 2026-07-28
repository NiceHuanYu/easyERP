<template>
  <div class="order-detail-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/production/orders')">返回</el-button>
      <h2 class="page-title">生产工单详情 - {{ order?.orderNo }}</h2>
      <el-tag v-if="order" :type="statusTagType(order.status)" size="large" style="margin-left: 12px">
        {{ statusLabel(order.status) }}
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div v-if="order" class="action-bar">
      <template v-if="order.status === 'DRAFT'">
        <el-button
          v-permission="'production:order:update'"
          type="primary"
          @click="router.push(`/production/orders/create?id=${order.id}`)"
        >
          编辑
        </el-button>
        <el-popconfirm title="确认删除该工单？" @confirm="handleDelete">
          <template #reference>
            <el-button v-permission="'production:order:delete'" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
        <el-popconfirm title="确认下达该工单？" @confirm="handleRelease">
          <template #reference>
            <el-button v-permission="'production:order:release'" type="warning">下达</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order.status === 'RELEASED'">
        <el-button type="primary" @click="router.push(`/production/orders/create?id=${order.id}`)">
          查看
        </el-button>
        <el-button type="success" @click="handleCreatePicking">创建领料单</el-button>
        <el-popconfirm title="确认完工入库？" @confirm="handleFinish">
          <template #reference>
            <el-button v-permission="'production:order:finish'" type="warning">完工入库</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order.status === 'COMPLETED'">
        <el-button type="primary" @click="router.push(`/production/orders/create?id=${order.id}`)">
          查看
        </el-button>
      </template>
    </div>

    <!-- 标签页 -->
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions v-if="order" :column="3" border>
            <el-descriptions-item label="工单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="销售订单">
              <el-link
                v-if="order.salesOrderId"
                type="primary"
                @click="router.push(`/sales/orders/${order.salesOrderId}`)"
              >
                {{ order.salesOrderNo }}
              </el-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(order.status)" size="small">
                {{ statusLabel(order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="物料">{{ order.materialName }}</el-descriptions-item>
            <el-descriptions-item label="计划数量">{{ order.planQuantity }}</el-descriptions-item>
            <el-descriptions-item label="已完工数量">{{ order.finishedQuantity }}</el-descriptions-item>
            <el-descriptions-item label="计划开始日期">{{ order.planStartDate }}</el-descriptions-item>
            <el-descriptions-item label="计划结束日期">{{ order.planEndDate }}</el-descriptions-item>
            <el-descriptions-item label="生产车间">{{ order.workshopName }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="3">{{ order.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="制单人">{{ order.createdBy }}</el-descriptions-item>
            <el-descriptions-item label="制单时间">{{ order.createdAt }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 物料需求 -->
        <el-tab-pane label="物料需求" name="materials">
          <el-table :data="materialRequirements" border stripe>
            <el-table-column label="序号" width="60" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="materialName" label="物料" min-width="180" />
            <el-table-column prop="unitUsage" label="单位用量" width="120" align="right" />
            <el-table-column prop="requiredQuantity" label="需求数量" width="140" align="right" />
            <el-table-column prop="pickedQuantity" label="已领数量" width="120" align="right">
              <template #default="{ row }">
                <span :class="{ 'fulfilled': row.pickedQuantity >= row.requiredQuantity }">
                  {{ row.pickedQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="unpickedQuantity" label="未领数量" width="120" align="right">
              <template #default="{ row }">
                <span :class="{ 'unfulfilled': row.unpickedQuantity > 0 }">
                  {{ row.unpickedQuantity }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="materialRequirements.length === 0" description="暂无物料需求" />
        </el-tab-pane>

        <!-- 领料记录 -->
        <el-tab-pane label="领料记录" name="pickings">
          <el-table :data="pickings" border stripe>
            <el-table-column prop="pickingNo" label="领料单号" width="160" />
            <el-table-column prop="pickingDate" label="领料日期" width="120" />
            <el-table-column prop="warehouseName" label="领料仓库" width="140" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'DRAFT' ? 'info' : 'success'" size="small">
                  {{ row.status === 'DRAFT' ? '草稿' : '已领料' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="router.push(`/production/pickings/create?id=${row.id}`)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pickings.length === 0" description="暂无领料记录" />
        </el-tab-pane>

        <!-- 入库记录 -->
        <el-tab-pane label="入库记录" name="finishings">
          <el-table :data="finishings" border stripe>
            <el-table-column prop="finishingNo" label="入库单号" width="160" />
            <el-table-column prop="finishingDate" label="入库日期" width="120" />
            <el-table-column prop="warehouseName" label="入库仓库" width="140" />
            <el-table-column prop="quantity" label="入库数量" width="120" align="right" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'DRAFT' ? 'info' : 'success'" size="small">
                  {{ row.status === 'DRAFT' ? '草稿' : '已入库' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="router.push(`/production/finishings/create?id=${row.id}`)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="finishings.length === 0" description="暂无入库记录" />
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
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  DRAFT: '待排产',
  RELEASED: '已下达',
  COMPLETED: '已完成',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | '' {
  const map: Record<string, 'info' | 'warning' | 'success' | ''> = {
    DRAFT: 'info',
    RELEASED: 'warning',
    COMPLETED: 'success',
  }
  return map[s] ?? ''
}

// ==================== 类型 ====================
interface Order {
  id: number
  orderNo: string
  salesOrderId: number | null
  salesOrderNo: string
  materialId: number
  materialName: string
  planQuantity: number
  finishedQuantity: number
  planStartDate: string
  planEndDate: string
  workshopName: string
  status: string
  remark: string
  createdBy: string
  createdAt: string
}

interface MaterialRequirement {
  materialId: number
  materialName: string
  unitUsage: number
  requiredQuantity: number
  pickedQuantity: number
  unpickedQuantity: number
}

interface Picking {
  id: number
  pickingNo: string
  pickingDate: string
  warehouseName: string
  status: 'DRAFT' | 'CONFIRMED'
}

interface FinishingRecord {
  id: number
  finishingNo: string
  finishingDate: string
  warehouseName: string
  quantity: number
  status: 'DRAFT' | 'CONFIRMED'
}

interface History {
  id: number
  action: string
  operator: string
  time: string
}

// ==================== 数据 ====================
const order = ref<Order | null>(null)
const materialRequirements = ref<MaterialRequirement[]>([])
const pickings = ref<Picking[]>([])
const finishings = ref<FinishingRecord[]>([])
const histories = ref<History[]>([])

// ==================== 标签页 ====================
const activeTab = ref('info')

// ==================== 加载 ====================
async function loadOrder(id: string) {
  try {
    const [orderData, materials, pickingList, finishingList, historyList] = await Promise.all([
      api.get<Order>(`/production/orders/${id}`),
      api.get<MaterialRequirement[]>(`/production/orders/material-requirements/${id}`),
      api.get<Picking[]>(`/production/pickings?orderId=${id}`),
      api.get<FinishingRecord[]>(`/production/finishings?orderId=${id}`),
      api.get<History[]>(`/production/orders/${id}/history`),
    ])
    order.value = orderData
    materialRequirements.value = materials
    pickings.value = pickingList
    finishings.value = finishingList
    histories.value = historyList
  } catch {
    ElMessage.error('加载工单详情失败')
  }
}

// ==================== 操作 ====================
async function handleDelete() {
  try {
    await api.del(`/production/orders/${route.params.id}`)
    ElMessage.success('删除成功')
    router.push('/production/orders')
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleRelease() {
  try {
    await api.post(`/production/orders/release/${route.params.id}`)
    ElMessage.success('工单已下达')
    loadOrder(route.params.id as string)
  } catch {
    ElMessage.error('下达失败')
  }
}

async function handleFinish() {
  try {
    await api.post(`/production/orders/${route.params.id}/finish`)
    ElMessage.success('已提交完工入库')
    loadOrder(route.params.id as string)
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleCreatePicking() {
  router.push(`/production/pickings/create?fromOrder=${route.params.id}`)
}

// ==================== 初始化 ====================
onMounted(() => {
  const id = route.params.id as string
  loadOrder(id)
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

.fulfilled {
  color: #67c23a;
}

.unfulfilled {
  color: #e6a23c;
  font-weight: 600;
}

.history-operator {
  color: #909399;
  font-size: 13px;
}
</style>
