<template>
  <div class="order-detail-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/production/orders')">返回</el-button>
      <h2 class="page-title">生产工单详情 - {{ order.orderNo }}</h2>
      <el-tag :type="statusTagType(order.status)" size="large" style="margin-left: 12px">
        {{ statusLabel(order.status) }}
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <template v-if="order.status === 'pending'">
        <el-button
          v-permission="'production:order:edit'"
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

      <template v-else-if="order.status === 'released'">
        <el-button type="success" @click="handleCreatePicking">创建领料单</el-button>
        <el-popconfirm title="确认开始执行？" @confirm="handleStart">
          <template #reference>
            <el-button type="primary">开始执行</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order.status === 'running'">
        <el-button type="success" @click="handleCreatePicking">创建领料单</el-button>
        <el-popconfirm title="确认完工入库？" @confirm="handleFinish">
          <template #reference>
            <el-button v-permission="'production:order:finish'" type="warning">完工入库</el-button>
          </template>
        </el-popconfirm>
      </template>

      <template v-else-if="order.status === 'finishing'">
        <el-button type="primary" @click="handleCreateFinishing">创建入库单</el-button>
      </template>
    </div>

    <!-- 标签页 -->
    <el-card shadow="never" class="section-card">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="3" border>
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
                <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
                  {{ row.status === 'draft' ? '草稿' : '已领料' }}
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
                <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
                  {{ row.status === 'draft' ? '草稿' : '已入库' }}
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

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  pending: '待排产',
  released: '已下达',
  running: '执行中',
  finishing: '完工待入库',
  completed: '已完成',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | '' {
  const map: Record<string, 'info' | 'warning' | 'success' | ''> = {
    pending: 'info',
    released: 'warning',
    running: '',
    finishing: 'warning',
    completed: 'success',
  }
  return map[s] ?? ''
}

// ==================== 类型 ====================
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
  status: 'draft' | 'picked'
}

interface Finishing {
  id: number
  finishingNo: string
  finishingDate: string
  warehouseName: string
  quantity: number
  status: 'draft' | 'finished'
}

interface History {
  id: number
  action: string
  operator: string
  time: string
}

// ==================== Mock 数据 ====================
const order = ref({
  id: 1,
  orderNo: 'MO-00001',
  salesOrderId: 1,
  salesOrderNo: 'SO-00001',
  materialId: 1,
  materialName: 'PCB-001 主板基板',
  planQuantity: 100,
  finishedQuantity: 60,
  planStartDate: '2025-06-15',
  planEndDate: '2025-07-01',
  workshopName: 'SMT 贴片车间',
  status: 'running' as const,
  remark: '紧急工单，请优先安排',
  createdBy: '张三',
  createdAt: '2025-06-14 09:30:00',
})

const materialRequirements = ref<MaterialRequirement[]>([
  { materialId: 11, materialName: '电阻 10KΩ', unitUsage: 5, requiredQuantity: 500, pickedQuantity: 500, unpickedQuantity: 0 },
  { materialId: 12, materialName: '电容 100μF', unitUsage: 3, requiredQuantity: 300, pickedQuantity: 200, unpickedQuantity: 100 },
  { materialId: 13, materialName: 'PCB 裸板', unitUsage: 1, requiredQuantity: 100, pickedQuantity: 60, unpickedQuantity: 40 },
  { materialId: 14, materialName: '锡膏', unitUsage: 0.02, requiredQuantity: 2, pickedQuantity: 1.5, unpickedQuantity: 0.5 },
])

const pickings = ref<Picking[]>([
  { id: 1, pickingNo: 'PK-00001', pickingDate: '2025-06-16', warehouseName: '电子料仓', status: 'picked' },
  { id: 2, pickingNo: 'PK-00002', pickingDate: '2025-06-18', warehouseName: '电子料仓', status: 'draft' },
  { id: 3, pickingNo: 'PK-00003', pickingDate: '2025-06-18', warehouseName: '板材仓', status: 'picked' },
])

const finishings = ref<Finishing[]>([
  { id: 1, finishingNo: 'FN-00001', finishingDate: '2025-06-20', warehouseName: '成品仓A', quantity: 30, status: 'finished' },
  { id: 2, finishingNo: 'FN-00002', finishingDate: '2025-06-22', warehouseName: '成品仓A', quantity: 30, status: 'finished' },
])

const histories = ref<History[]>([
  { id: 1, action: '创建工单', operator: '张三', time: '2025-06-14 09:30:00' },
  { id: 2, action: '下达工单', operator: '李四', time: '2025-06-15 08:00:00' },
  { id: 3, action: '开始执行', operator: '王五', time: '2025-06-15 08:30:00' },
  { id: 4, action: '领料 (PK-00001)', operator: '王五', time: '2025-06-16 10:00:00' },
  { id: 5, action: '完工入库 30 件 (FN-00001)', operator: '王五', time: '2025-06-20 16:00:00' },
  { id: 6, action: '完工入库 30 件 (FN-00002)', operator: '赵六', time: '2025-06-22 14:00:00' },
])

// ==================== 标签页 ====================
const activeTab = ref('info')

// ==================== 操作 ====================
function handleDelete() {
  ElMessage.success('删除成功')
  router.push('/production/orders')
}

function handleRelease() {
  order.value.status = 'released'
  ElMessage.success('工单已下达')
}

function handleStart() {
  order.value.status = 'running'
  ElMessage.success('工单已开始执行')
}

function handleFinish() {
  order.value.status = 'finishing'
  ElMessage.success('已提交完工入库')
}

function handleCreatePicking() {
  router.push(`/production/pickings/create?fromOrder=${order.value.id}`)
}

function handleCreateFinishing() {
  router.push(`/production/finishings/create?fromOrder=${order.value.id}`)
}

// ==================== 加载 ====================
onMounted(() => {
  const id = route.params.id as string
  // 实际开发中根据 id 调用 API
  console.log('Loading production order:', id)
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
