<template>
  <div class="receiving-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '编辑收货单' : '新增收货单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="110px"
      class="receiving-form"
    >
      <!-- 收货头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">收货信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="采购订单" prop="orderId">
              <el-select
                v-model="form.orderId"
                placeholder="请选择采购订单"
                filterable
                style="width: 100%"
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
            <el-form-item label="收货仓库" prop="warehouseId">
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
            <el-form-item label="收货日期" prop="receivingDate">
              <el-date-picker
                v-model="form.receivingDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="2"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 收货明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">收货明细</span>
        </template>
        <el-table :data="form.lines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="物料" width="220">
            <template #default="{ row }">
              <el-input :model-value="row.materialName" readonly />
            </template>
          </el-table-column>
          <el-table-column label="订单数量" width="120" align="right">
            <template #default="{ row }">{{ row.orderQuantity }}</template>
          </el-table-column>
          <el-table-column label="已收数量" width="120" align="right">
            <template #default="{ row }">
              <span>
                {{ row.receivedQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="本次收货数量" width="160" align="center">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.receivingQuantity"
                :min="0"
                :max="row.orderQuantity - row.receivedQuantity"
                :precision="0"
                style="width: 140px"
                @change="recalcLine($index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.subtotal) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 底部汇总 -->
      <el-card shadow="never" class="section-card">
        <el-row :gutter="20">
          <el-col :span="6" :offset="18">
            <div class="total-amount">
              <span class="total-label">收货金额合计：</span>
              <span class="total-value">{{ formatMoney(totalAmount) }}</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 12px">
          <el-col :span="6">
            <div class="meta-item">
              <span class="meta-label">制单人：</span>
              <span>{{ authStore.userInfo?.name ?? '当前用户' }}</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="meta-item">
              <span class="meta-label">制单时间：</span>
              <span>{{ formatDate(new Date()) }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="handleSubmit">确认收货</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { formatMoney, formatDate } from '~/utils'
import { useAuthStore } from '../../../stores/auth'
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ==================== 编辑/新增模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据（TODO: 后续替换为 API 调用） ====================
const orderOptions = ref([
  { label: 'PO-00001 / 深圳华强电子有限公司', value: 1 },
  { label: 'PO-00002 / 广州万国元件有限公司', value: 2 },
  { label: 'PO-00003 / 东莞正泰科技有限公司', value: 3 },
  { label: 'PO-00004 / 上海锐拓半导体有限公司', value: 4 },
  { label: 'PO-00005 / 深圳华强电子有限公司', value: 5 },
])

const warehouseOptions = ref([
  { label: '原材料仓A', value: 1 },
  { label: '原材料仓B', value: 2 },
  { label: '半成品仓', value: 3 },
])

// ==================== 类型定义 ====================
interface ReceivingLine {
  materialId: number
  materialName: string
  orderQuantity: number
  receivedQuantity: number
  receivingQuantity: number
  price: number
  subtotal: number
}

interface ReceivingForm {
  orderId: number | null
  warehouseId: number | null
  receivingDate: string
  remark: string
  lines: ReceivingLine[]
}

// ==================== 表单 ====================
const form = reactive<ReceivingForm>({
  orderId: null,
  warehouseId: null,
  receivingDate: formatDate(new Date(), 'YYYY-MM-DD'),
  remark: '',
  lines: [],
})

const formRef = ref()
const rules = {
  orderId: [{ required: true, message: '请选择采购订单', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择收货仓库', trigger: 'change' }],
  receivingDate: [{ required: true, message: '请选择收货日期', trigger: 'change' }],
}

// ==================== 根据采购订单加载可收货行 ====================
async function onOrderChange(val: number | null) {
  if (!val) {
    form.lines = []
    return
  }
  try {
    const data = await api.get<any>(`/purchase/orders/${val}`)
    if (data.lines && data.lines.length > 0) {
      form.lines = data.lines.map((l: any) => ({
        materialId: l.materialId ?? 0,
        materialName: l.materialName ?? '',
        orderQuantity: l.quantity ?? 0,
        receivedQuantity: l.receivedQuantity ?? 0,
        receivingQuantity: (l.quantity ?? 0) - (l.receivedQuantity ?? 0),
        price: l.price ?? 0,
        subtotal: parseFloat((((l.quantity ?? 0) - (l.receivedQuantity ?? 0)) * (l.price ?? 0)).toFixed(2)),
      }))
    } else {
      form.lines = []
    }
  } catch {
    ElMessage.error('加载采购订单明细失败')
  }
}

function recalcLine(index: number) {
  const line = form.lines[index]
  line.subtotal = parseFloat((line.receivingQuantity * line.price).toFixed(2))
}

// ==================== 合计 ====================
const totalAmount = computed(() => {
  return form.lines.reduce((sum, l) => sum + l.subtotal, 0)
})

// ==================== 校验 ====================
function validateLines(): boolean {
  if (form.lines.length === 0) {
    ElMessage.warning('请先选择采购订单，加载待收货明细')
    return false
  }
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (!line.receivingQuantity || line.receivingQuantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行：本次收货数量必须大于 0`)
      return false
    }
    const maxReceivable = line.orderQuantity - line.receivedQuantity
    if (line.receivingQuantity > maxReceivable) {
      ElMessage.warning(`第 ${i + 1} 行：本次收货数量不能超过可收数量 ${maxReceivable}`)
      return false
    }
  }
  return true
}

// ==================== 提交 ====================
async function handleSaveDraft() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  try {
    const payload = { ...form, status: 'draft', totalAmount: totalAmount.value }
    if (isEdit.value) {
      await api.put(`/purchase/receivings/${route.query.id}`, payload)
    } else {
      await api.post('/purchase/receivings', payload)
    }
    ElMessage.success('收货单草稿保存成功')
    router.push('/purchase/receivings')
  } catch {
    ElMessage.error('保存失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  try {
    const payload = { ...form, status: 'received', totalAmount: totalAmount.value }
    if (isEdit.value) {
      await api.put(`/purchase/receivings/${route.query.id}`, payload)
    } else {
      const result = await api.post<any>('/purchase/receivings', payload)
      // Auto-confirm if creating new
      if (result.id) {
        await api.post(`/purchase/receivings/confirm/${result.id}`)
      }
    }
    ElMessage.success('收货确认成功')
    router.push('/purchase/receivings')
  } catch {
    ElMessage.error('提交失败')
  }
}

// ==================== 编辑模式：加载已有收货单 ====================
async function loadReceiving(id: string) {
  try {
    const data = await api.get<any>(`/purchase/receivings/${id}`)
    form.orderId = data.orderId ?? null
    form.warehouseId = data.warehouseId ?? null
    form.receivingDate = data.receivingDate ?? formatDate(new Date(), 'YYYY-MM-DD')
    form.remark = data.remark ?? ''
    if (data.lines && data.lines.length > 0) {
      form.lines = data.lines.map((l: any) => ({
        materialId: l.materialId ?? 0,
        materialName: l.materialName ?? '',
        orderQuantity: l.orderQuantity ?? 0,
        receivedQuantity: l.receivedQuantity ?? 0,
        receivingQuantity: l.receivingQuantity ?? 0,
        price: l.price ?? 0,
        subtotal: l.subtotal ?? 0,
      }))
    }
  } catch {
    ElMessage.error('加载收货单数据失败')
  }
}

// ==================== 预填逻辑（来自采购订单） ====================
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
    loadReceiving(route.query.id as string)
  }
})
</script>

<style scoped>
.receiving-create-page {
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

.total-amount {
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.total-label {
  color: #606266;
}

.total-value {
  color: #f56c6c;
  font-size: 18px;
}

.meta-item {
  font-size: 13px;
  color: #909399;
}

.meta-label {
  margin-right: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 20px 0;
}
</style>
