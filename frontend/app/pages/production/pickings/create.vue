<template>
  <div class="picking-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '查看领料单' : '新增领料单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="110px"
      class="picking-form"
    >
      <!-- 领料头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">领料信息</span>
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
            <el-form-item label="仓库" prop="warehouseId">
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
            <el-form-item label="领料日期" prop="pickingDate">
              <el-date-picker
                v-model="form.pickingDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 领料明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">领料明细</span>
        </template>
        <el-table :data="form.lines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialName" label="物料" min-width="180" />
          <el-table-column prop="requiredQuantity" label="需求数量" width="120" align="right" />
          <el-table-column prop="pickedQuantity" label="已领数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="{ 'fulfilled': row.pickedQuantity >= row.requiredQuantity }">
                {{ row.pickedQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="本次领料数量" width="160" align="center">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.pickingQuantity"
                :min="0"
                :max="row.requiredQuantity - row.pickedQuantity"
                :precision="row.unit === 'kg' ? 4 : 0"
                style="width: 140px"
                :disabled="isEdit"
              />
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
        </el-table>
        <el-empty
          v-if="form.lines.length === 0"
          description="请先选择生产工单以加载物料需求"
        />
      </el-card>

      <!-- 操作按钮 -->
      <div v-if="!isEdit" class="form-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="handleConfirm">确认领料</el-button>
      </div>
      <div v-else class="form-actions">
        <el-button @click="router.back()">返回</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { formatDate, generateCode } from '~/utils'
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
      api.get<{ id: number; orderNo: string; productName: string; status: string }[]>('/production/orders?status=RELEASED'),
      api.page<{ id: number; name: string }>('/base/warehouses', 1, 1000),
    ])
    orderOptions.value = orders.map((o) => ({
      label: `${o.orderNo} / ${o.productName}（${o.status === 'RELEASED' ? '已下达' : o.status}）`,
      value: o.id,
    }))
    warehouseOptions.value = warehouses.list.map((w) => ({ label: w.name, value: w.id }))
  } catch {
    // options load silently
  }
}

// ==================== 类型 ====================
interface PickingLine {
  materialId: number
  materialName: string
  requiredQuantity: number
  pickedQuantity: number
  pickingQuantity: number
  unit: string
}

interface PickingForm {
  pickingNo: string
  orderId: number | null
  warehouseId: number | null
  pickingDate: string
  lines: PickingLine[]
}

// ==================== 表单 ====================
const form = reactive<PickingForm>({
  pickingNo: '',
  orderId: null,
  warehouseId: null,
  pickingDate: formatDate(new Date(), 'YYYY-MM-DD'),
  lines: [],
})

const formRef = ref()
const rules = {
  orderId: [{ required: true, message: '请选择生产工单', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  pickingDate: [{ required: true, message: '请选择领料日期', trigger: 'change' }],
}

// ==================== 事件处理 ====================
async function onOrderChange(val: number | null) {
  if (!val) {
    form.lines = []
    return
  }
  try {
    const lines = await api.get<PickingLine[]>(`/production/orders/material-requirements/${val}`)
    form.lines = lines.map((l) => ({ ...l, pickingQuantity: Math.max(0, l.requiredQuantity - l.pickedQuantity) }))
  } catch {
    form.lines = []
    ElMessage.warning('加载物料需求失败')
  }
}

// ==================== 校验 ====================
function validateLines(): boolean {
  if (form.lines.length === 0) {
    ElMessage.warning('请先选择生产工单，加载物料需求')
    return false
  }
  const hasAny = form.lines.some((l) => l.pickingQuantity > 0)
  if (!hasAny) {
    ElMessage.warning('请至少填写一项物料的本次领料数量')
    return false
  }
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (line.pickingQuantity > line.requiredQuantity - line.pickedQuantity) {
      ElMessage.warning(`第 ${i + 1} 行：本次领料数量不能超过未领数量`)
      return false
    }
  }
  return true
}

// ==================== 提交 ====================
async function doSubmit(status: 'DRAFT' | 'CONFIRMED') {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  const payload = {
    orderId: form.orderId,
    warehouseId: form.warehouseId,
    pickingDate: form.pickingDate,
    lines: form.lines.filter((l) => l.pickingQuantity > 0).map((l) => ({
      materialId: l.materialId,
      quantity: l.pickingQuantity,
    })),
  }

  try {
    if (isEdit.value) {
      await api.put(`/production/pickings/${route.query.id}`, payload)
    } else {
      const result = await api.post<{ id: number }>('/production/pickings', payload)
      if (status === 'CONFIRMED') {
        await api.post(`/production/pickings/confirm/${result.id}`)
      }
    }
    const actionLabel = status === 'DRAFT' ? '保存草稿' : '确认领料'
    ElMessage.success(`${actionLabel}成功`)
    router.push('/production/pickings')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleSaveDraft() {
  await doSubmit('DRAFT')
}

async function handleConfirm() {
  await doSubmit('CONFIRMED')
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
    // 查看模式：加载已有领料单
    loadPicking(route.query.id as string)
  }
})

async function loadPicking(id: string) {
  try {
    const data = await api.get<{ orderId: number; warehouseId: number; pickingDate: string; lines: PickingLine[] }>(`/production/pickings/${id}`)
    form.orderId = data.orderId
    form.warehouseId = data.warehouseId
    form.pickingDate = data.pickingDate
    form.lines = data.lines.map((l) => ({ ...l }))
  } catch {
    ElMessage.error('加载领料单数据失败')
  }
}
</script>

<style scoped>
.picking-create-page {
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
