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

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ==================== 编辑/新增模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据 ====================
const orderOptions = ref([
  { label: 'MO-00001 / PCB-001 主板基板（执行中）', value: 1 },
  { label: 'MO-00002 / CPU-002 中央处理器（执行中）', value: 2 },
  { label: 'MO-00003 / LCD-003 液晶显示屏（完工待入库）', value: 3 },
  { label: 'MO-00005 / BAT-004 锂电池组（执行中）', value: 5 },
])

const warehouseOptions = ref([
  { label: '成品仓A', value: 1 },
  { label: '成品仓B', value: 2 },
  { label: '中转仓', value: 3 },
])

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

// ==================== Mock：根据工单加载产品 ====================
const orderLinesMap: Record<number, FinishingLine[]> = {
  1: [
    { materialId: 1, materialName: 'PCB-001 主板基板', orderQuantity: 100, finishedQuantity: 60, finishingQuantity: 20, unit: '块' },
  ],
  2: [
    { materialId: 2, materialName: 'CPU-002 中央处理器', orderQuantity: 50, finishedQuantity: 0, finishingQuantity: 30, unit: '个' },
  ],
  3: [
    { materialId: 3, materialName: 'LCD-003 液晶显示屏', orderQuantity: 200, finishedQuantity: 150, finishingQuantity: 50, unit: '块' },
  ],
  5: [
    { materialId: 4, materialName: 'BAT-004 锂电池组', orderQuantity: 100, finishedQuantity: 30, finishingQuantity: 40, unit: '组' },
  ],
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
function onOrderChange(val: number | null) {
  if (!val) {
    form.lines = []
    return
  }
  const lines = orderLinesMap[val]
  if (lines) {
    form.lines = lines.map((l) => ({ ...l }))
  } else {
    form.lines = []
    ElMessage.warning('未找到该工单的产品信息')
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

  const actionLabel = status === 'draft' ? '保存草稿' : '确认入库'
  ElMessage.success(`${actionLabel}成功`)
  router.push('/production/finishings')
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
  if (route.query.fromOrder) {
    prefillFromOrder(route.query.fromOrder as string)
  }
  if (route.query.id) {
    // 编辑/查看模式：加载已有入库单
    form.orderId = 1
    form.warehouseId = 1
    form.finishingDate = '2025-06-20'
    onOrderChange(1)
    form.lines.forEach((l) => {
      l.finishingQuantity = 30
    })
  }
})
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
