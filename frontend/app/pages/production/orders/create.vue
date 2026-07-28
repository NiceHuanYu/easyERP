<template>
  <div class="order-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '编辑生产工单' : '新增生产工单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="order-form"
    >
      <!-- 工单头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">工单信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="销售订单" prop="salesOrderId">
              <el-select
                v-model="form.salesOrderId"
                placeholder="请选择销售订单（可选）"
                filterable
                clearable
                style="width: 100%"
                @change="onSalesOrderChange"
              >
                <el-option
                  v-for="o in salesOrderOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料" prop="materialId">
              <el-select
                v-model="form.materialId"
                placeholder="请选择物料"
                filterable
                style="width: 100%"
                @change="onMaterialChange"
              >
                <el-option
                  v-for="m in bomProductOptions"
                  :key="m.value"
                  :label="m.label"
                  :value="m.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划数量" prop="planQuantity">
              <el-input-number
                v-model="form.planQuantity"
                :min="1"
                :precision="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划开始日期" prop="planStartDate">
              <el-date-picker
                v-model="form.planStartDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划结束日期" prop="planEndDate">
              <el-date-picker
                v-model="form.planEndDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生产车间" prop="workshopId">
              <el-select
                v-model="form.workshopId"
                placeholder="请选择生产车间"
                style="width: 100%"
              >
                <el-option
                  v-for="w in workshopOptions"
                  :key="w.value"
                  :label="w.label"
                  :value="w.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- BOM 物料需求预览 -->
      <el-card v-if="bomLines.length > 0" shadow="never" class="section-card">
        <template #header>
          <span class="section-title">BOM 物料需求预览</span>
        </template>
        <el-table :data="bomLines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialName" label="物料名称" min-width="180" />
          <el-table-column prop="unitUsage" label="单位用量" width="120" align="right" />
          <el-table-column prop="requiredQuantity" label="需求数量" width="140" align="right">
            <template #default="{ row }">
              {{ row.requiredQuantity }}
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="warehouseName" label="默认仓库" width="140" />
        </el-table>
      </el-card>
      <el-card v-else-if="form.materialId" shadow="never" class="section-card">
        <template #header>
          <span class="section-title">BOM 物料需求预览</span>
        </template>
        <el-empty description="该物料暂无 BOM 配置" />
      </el-card>

      <!-- 底部信息 -->
      <el-card shadow="never" class="section-card">
        <el-row :gutter="20">
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
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button type="success" @click="handleSaveAndRelease">保存并下达</el-button>
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

// ==================== 选项数据（从后端加载） ====================
const salesOrderOptions = ref<{ label: string; value: number }[]>([])
const bomProductOptions = ref<{ label: string; value: number }[]>([])
const workshopOptions = ref<{ label: string; value: number }[]>([])

async function loadOptions() {
  try {
    const [orders, products, workshops] = await Promise.all([
      api.get<{ id: number; orderNo: string; customerName: string; productName: string }[]>('/sales/orders?status=confirmed'),
      api.get<{ id: number; name: string }[]>('/production/materials'),
      api.get<{ id: number; name: string }[]>('/system/workshops'),
    ])
    salesOrderOptions.value = orders.map((o) => ({
      label: `${o.orderNo} / ${o.customerName} - ${o.productName}`,
      value: o.id,
    }))
    bomProductOptions.value = products.map((p) => ({ label: p.name, value: p.id }))
    workshopOptions.value = workshops.map((w) => ({ label: w.name, value: w.id }))
  } catch {
    // options load silently
  }
}

// ==================== BOM 需求行 ====================
interface BomLine {
  materialId: number
  materialName: string
  unitUsage: number
  requiredQuantity: number
  unit: string
  warehouseName: string
}

const bomLines = ref<BomLine[]>([])

async function recalcBom() {
  if (!form.materialId || !form.planQuantity) {
    bomLines.value = []
    return
  }
  try {
    const data = await api.get<BomLine[]>(`/production/materials/${form.materialId}/bom`)
    bomLines.value = data.map((l) => ({
      ...l,
      requiredQuantity: parseFloat((l.unitUsage * form.planQuantity).toFixed(4)),
    }))
  } catch {
    bomLines.value = []
  }
}

// ==================== 表单数据 ====================
interface OrderForm {
  orderNo: string
  salesOrderId: number | null
  materialId: number | null
  planQuantity: number
  planStartDate: string
  planEndDate: string
  workshopId: number | null
  remark: string
}

const form = reactive<OrderForm>({
  orderNo: '',
  salesOrderId: null,
  materialId: null,
  planQuantity: 100,
  planStartDate: formatDate(new Date(), 'YYYY-MM-DD'),
  planEndDate: '',
  workshopId: null,
  remark: '',
})

const formRef = ref()
const rules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  planQuantity: [{ required: true, message: '请输入计划数量', trigger: 'blur' }],
  planStartDate: [{ required: true, message: '请选择计划开始日期', trigger: 'change' }],
  planEndDate: [{ required: true, message: '请选择计划结束日期', trigger: 'change' }],
  workshopId: [{ required: true, message: '请选择生产车间', trigger: 'change' }],
}

// ==================== 事件处理 ====================
async function onSalesOrderChange(val: number | null) {
  if (!val) return
  try {
    const so = await api.get<{ productId: number }>(`/sales/orders/${val}`)
    if (so?.productId) {
      form.materialId = so.productId
      recalcBom()
    }
  } catch {
    // silently fail
  }
}

function onMaterialChange(_val: number | null) {
  recalcBom()
}

// ==================== 提交 ====================
async function doSave(status: 'DRAFT' | 'RELEASED') {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const payload = { ...form }

  try {
    const id = route.query.id as string | undefined
    if (id) {
      await api.put(`/production/orders/${id}`, payload)
    } else {
      await api.post('/production/orders', payload)
    }
    if (status === 'RELEASED') {
      const savedId = id ?? (await api.get<{ id: number }>(`/production/orders/latest`)).id
      await api.post(`/production/orders/release/${savedId}`)
    }
    const actionLabel = status === 'DRAFT' ? '保存' : '下达'
    ElMessage.success(`工单${actionLabel}成功`)
    router.push('/production/orders')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleSave() {
  await doSave('DRAFT')
}

async function handleSaveAndRelease() {
  await doSave('RELEASED')
}

// ==================== 编辑模式加载 ====================
async function loadOrder(id: string) {
  try {
    const data = await api.get<OrderForm>(`/production/orders/${id}`)
    Object.assign(form, data)
    recalcBom()
  } catch {
    ElMessage.error('加载工单数据失败')
  }
}

// ==================== 从销售订单预填 ====================
function prefillFromSalesOrder(orderId: string) {
  form.salesOrderId = Number(orderId)
  onSalesOrderChange(form.salesOrderId)
}

// ==================== 初始化 ====================
onMounted(() => {
  loadOptions()
  if (!route.query.id) {
    form.orderNo = generateCode('production')
  }
  if (route.query.id) {
    loadOrder(route.query.id as string)
  }
  if (route.query.fromSalesOrder) {
    prefillFromSalesOrder(route.query.fromSalesOrder as string)
  }
})
</script>

<style scoped>
.order-create-page {
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
