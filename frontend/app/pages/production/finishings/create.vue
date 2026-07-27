<template>
  <div class="finishing-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '查看入库单' : '新增入库单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="110px"
      class="finishing-form"
    >
      <!-- 入库头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">入库信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="生产工单" prop="orderId">
              <el-select
                v-model="form.orderId"
                placeholder="请选择生产工单"
                filterable
                style="width: 100%"
                :disabled="isEdit"
                @change="onOrderChange"
              >
                <el-option
                  v-for="o in orderOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
              >
                <el-option
                  v-for="w in warehouseOptions"
                  :key="w.value"
                  :label="w.label"
                  :value="w.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库日期" prop="finishingDate">
              <el-date-picker
                v-model="form.finishingDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 入库明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">入库明细</span>
        </template>
        <el-table :data="form.lines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialName" label="物料" min-width="180" />
          <el-table-column prop="orderQuantity" label="工单数量" width="120" align="right" />
          <el-table-column prop="finishedQuantity" label="已入库数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="{ 'fulfilled': row.finishedQuantity >= row.orderQuantity }">
                {{ row.finishedQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="本次入库数量" width="160" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.finishingQuantity"
                :min="0"
                :max="row.orderQuantity - row.finishedQuantity"
                :precision="0"
                style="width: 140px"
                :disabled="isEdit"
              />
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
        </el-table>
        <el-empty
          v-if="form.lines.length === 0"
          description="请先选择生产工单以加载产品信息"
        />
      </el-card>

      <!-- 操作按钮 -->
      <div v-if="!isEdit" class="form-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="handleConfirm">确认入库</el-button>
      </div>
      <div v-else class="form-actions">
        <el-button @click="router.back()">返回</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { formatDate } from '~/utils'
import { useAuthStore } from '../../../stores/auth'
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ==================== 编辑/新增模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据 ====================
const orderOptions = ref<{ label: string; value: number }[]>([])
const warehouseOptions = ref<{ label: string; value: number }[]>([])

async function loadOptions() {
  try {
    const [orders, warehouses] = await Promise.all([
      api.get<{ id: number; orderNo: string; productName: string; status: string }[]>('/production/orders?status=running&status=finishing'),
      api.page<{ id: number; name: string }>('/base/warehouses', 1, 1000),
    ])
    orderOptions.value = orders.map((o) => {
      const statusLabel = o.status === 'running' ? '执行中' : '完工待入库'
      return { label: `${o.orderNo} / ${o.productName}（${statusLabel}）`, value: o.id }
    })
    warehouseOptions.value = warehouses.list.map((w) => ({ label: w.name, value: w.id }))
  } catch {
    // options load silently
  }
}

// ==================== 类型 ====================
interface FinishingLine {
  materialId: number
  materialName: string
  orderQuantity: number
  finishedQuantity: number
  finishingQuantity: number
  unit: string
}

interface FinishingForm {
  orderId: number | null
  warehouseId: number | null
  finishingDate: string
  lines: FinishingLine[]
}

// ==================== 表单 ====================
const form = reactive<FinishingForm>({
  orderId: null,
  warehouseId: null,
  finishingDate: formatDate(new Date(), 'YYYY-MM-DD'),
  lines: [],
})

const formRef = ref()
const rules = {
  orderId: [{ required: true, message: '请选择生产工单', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择入库仓库', trigger: 'change' }],
  finishingDate: [{ required: true, message: '请选择入库日期', trigger: 'change' }],
}

// ==================== 事件处理 ====================
async function onOrderChange(val: number | null) {
  if (!val) {
    form.lines = []
    return
  }
  try {
    const orderData = await api.get<{ materialId: number; materialName: string; planQuantity: number; finishedQuantity: number; unit: string }>(`/production/orders/${val}`)
    form.lines = [{
      materialId: orderData.materialId,
      materialName: orderData.materialName,
      orderQuantity: orderData.planQuantity,
      finishedQuantity: orderData.finishedQuantity,
      finishingQuantity: Math.max(0, orderData.planQuantity - orderData.finishedQuantity),
      unit: orderData.unit || '个',
    }]
  } catch {
    form.lines = []
    ElMessage.warning('加载产品信息失败')
  }
}

// ==================== 校验 ====================
function validateLines(): boolean {
  if (form.lines.length === 0) {
    ElMessage.warning('请先选择生产工单，加载产品信息')
    return false
  }
  const hasAny = form.lines.some((l) => l.finishingQuantity > 0)
  if (!hasAny) {
    ElMessage.warning('请填写本次入库数量')
    return false
  }
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (line.finishingQuantity > line.orderQuantity - line.finishedQuantity) {
      ElMessage.warning(`第 ${i + 1} 行：本次入库数量不能超过未入库数量`)
      return false
    }
  }
  return true
}

// ==================== 提交 ====================
async function doSubmit(status: 'draft' | 'finished') {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  const payload = {
    orderId: form.orderId,
    warehouseId: form.warehouseId,
    finishingDate: form.finishingDate,
    lines: form.lines.filter((l) => l.finishingQuantity > 0).map((l) => ({
      materialId: l.materialId,
      quantity: l.finishingQuantity,
    })),
  }

  try {
    if (isEdit.value) {
      await api.put(`/production/finishings/${route.query.id}`, payload)
    } else {
      const result = await api.post<{ id: number }>('/production/finishings', payload)
      if (status === 'finished') {
        await api.post(`/production/finishings/confirm/${result.id}`)
      }
    }
    const actionLabel = status === 'draft' ? '保存草稿' : '确认入库'
    ElMessage.success(`${actionLabel}成功`)
    router.push('/production/finishings')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleSaveDraft() {
  await doSubmit('draft')
}

async function handleConfirm() {
  await doSubmit('finished')
}

// ==================== 预填逻辑 ====================
function prefillFromOrder(orderId: string) {
  form.orderId = Number(orderId)
  onOrderChange(form.orderId)
}

// ==================== 初始化 ====================
onMounted(() => {
  loadOptions()
  if (route.query.fromOrder) {
    prefillFromOrder(route.query.fromOrder as string)
  }
  if (route.query.id) {
    loadFinishing(route.query.id as string)
  }
})

async function loadFinishing(id: string) {
  try {
    const data = await api.get<{ orderId: number; warehouseId: number; finishingDate: string; lines: FinishingLine[] }>(`/production/finishings/${id}`)
    form.orderId = data.orderId
    form.warehouseId = data.warehouseId
    form.finishingDate = data.finishingDate
    form.lines = data.lines.map((l) => ({ ...l }))
  } catch {
    ElMessage.error('加载入库单数据失败')
  }
}
</script>

<style scoped>
.finishing-create-page {
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

.section-card {
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
}

.fulfilled {
  color: #67c23a;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 20px 0;
}
</style>
