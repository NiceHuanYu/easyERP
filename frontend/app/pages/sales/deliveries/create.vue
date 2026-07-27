<template>
  <div class="delivery-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '编辑发货单' : '新增发货单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="110px"
      class="delivery-form"
    >
      <!-- 发货头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">发货信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="销售订单" prop="orderId">
              <el-select
                v-model="form.orderId"
                placeholder="请选择销售订单"
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
            <el-form-item label="发货仓库" prop="warehouseId">
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
            <el-form-item label="发货日期" prop="deliveryDate">
              <el-date-picker
                v-model="form.deliveryDate"
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

      <!-- 发货明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">发货明细</span>
        </template>
        <el-table :data="form.lines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialName" label="物料" width="200" />
          <el-table-column prop="orderQuantity" label="订单数量" width="120" align="right" />
          <el-table-column prop="deliverableQuantity" label="可发货数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="{ 'low-stock': row.deliverableQuantity < row.orderQuantity }">
                {{ row.deliverableQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="本次发货数量" width="160" align="center">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.deliveryQuantity"
                :min="0"
                :max="row.deliverableQuantity"
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
              <span class="total-label">发货金额合计：</span>
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
        <el-button type="success" @click="handleSubmit">提交</el-button>
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

// ==================== 编辑模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据 ====================
const orderOptions = ref<{ label: string; value: number }[]>([])
const warehouseOptions = ref<{ label: string; value: number }[]>([])

async function fetchWarehouseOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/warehouses', 1, 1000)
    warehouseOptions.value = result.list.map((item) => ({ label: item.name, value: item.id }))
  } catch (e: any) {
    // options load silently
  }
}

// ==================== 类型 ====================
interface DeliveryLine {
  materialId: number
  materialName: string
  orderQuantity: number
  deliverableQuantity: number
  deliveryQuantity: number
  price: number
  subtotal: number
}

interface DeliveryForm {
  orderId: number | null
  warehouseId: number | null
  deliveryDate: string
  remark: string
  lines: DeliveryLine[]
}

// ==================== 表单 ====================
const form = reactive<DeliveryForm>({
  orderId: null,
  warehouseId: null,
  deliveryDate: formatDate(new Date(), 'YYYY-MM-DD'),
  remark: '',
  lines: [],
})

const formRef = ref()
const rules = {
  orderId: [{ required: true, message: '请选择销售订单', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择发货仓库', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '请选择发货日期', trigger: 'change' }],
}

// ==================== 根据订单加载可发货行 ====================
async function onOrderChange(val: number | null) {
  if (!val) {
    form.lines = []
    return
  }
  try {
    const items = await api.get<DeliveryLine[]>(`/sales/orders/${val}/deliverable-items`)
    form.lines = items.map((l) => ({
      ...l,
      deliveryQuantity: l.deliverableQuantity,
      subtotal: parseFloat((l.deliverableQuantity * l.price).toFixed(2)),
    }))
  } catch (e: any) {
    ElMessage.error(e?.message || '加载可发货物料失败')
  }
}

function recalcLine(index: number) {
  const line = form.lines[index]
  line.subtotal = parseFloat((line.deliveryQuantity * line.price).toFixed(2))
}

// ==================== 合计 ====================
const totalAmount = computed(() => {
  return form.lines.reduce((sum, l) => sum + l.subtotal, 0)
})

// ==================== 提交 ====================
function validateLines(): boolean {
  if (form.lines.length === 0) {
    ElMessage.warning('请先选择销售订单，加载可发货物料')
    return false
  }
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (!line.deliveryQuantity || line.deliveryQuantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行：本次发货数量必须大于 0`)
      return false
    }
    if (line.deliveryQuantity > line.deliverableQuantity) {
      ElMessage.warning(`第 ${i + 1} 行：本次发货数量不能超过可发货数量`)
      return false
    }
  }
  return true
}

async function handleSaveDraft() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  try {
    if (isEdit.value) {
      await api.put(`/sales/deliveries/${route.query.id}`, form)
      ElMessage.success('发货单更新成功')
    } else {
      await api.post('/sales/deliveries', form)
      ElMessage.success('发货单草稿保存成功')
    }
    router.push('/sales/deliveries')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  try {
    let deliveryId: number | string | undefined
    if (isEdit.value) {
      await api.put(`/sales/deliveries/${route.query.id}`, form)
      deliveryId = route.query.id as string
    } else {
      const created = await api.post<{ id: number }>('/sales/deliveries', form)
      deliveryId = created.id
    }
    await api.post(`/sales/deliveries/${deliveryId}/confirm`)
    ElMessage.success('发货单提交成功')
    router.push('/sales/deliveries')
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  }
}

// ==================== 预填逻辑 ====================
function prefillFromOrder(orderId: string) {
  form.orderId = Number(orderId)
  onOrderChange(form.orderId)
}

// ==================== 初始化 ====================
onMounted(async () => {
  fetchWarehouseOptions()
  if (route.query.fromOrder) {
    prefillFromOrder(route.query.fromOrder as string)
  }
  if (route.query.id) {
    try {
      const data = await api.get<DeliveryForm>(`/sales/deliveries/${route.query.id}`)
      form.orderId = data.orderId
      form.warehouseId = data.warehouseId
      form.deliveryDate = data.deliveryDate
      form.remark = data.remark || ''
      if (data.orderId) {
        await onOrderChange(data.orderId)
        // Overwrite lines with saved delivery quantities
        form.lines = data.lines
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '加载发货单失败')
    }
  }
})
</script>

<style scoped>
.delivery-create-page {
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

.low-stock {
  color: #e6a23c;
  font-weight: 600;
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
