<template>
  <div class="po-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '编辑采购订单' : '新增采购订单' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="po-form"
    >
      <!-- 订单头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">订单信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="form.supplierId"
                placeholder="请选择供应商"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="s in supplierOptions"
                  :key="s.value"
                  :label="s.label"
                  :value="s.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购申请" prop="requisitionId">
              <el-select
                v-model="form.requisitionId"
                placeholder="请选择（可选）"
                clearable
                filterable
                style="width: 100%"
                @change="onRequisitionChange"
              >
                <el-option
                  v-for="r in requisitionOptions"
                  :key="r.value"
                  :label="r.label"
                  :value="r.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购日期" prop="orderDate">
              <el-date-picker
                v-model="form.orderDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="交货日期" prop="deliveryDate">
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
                :rows="3"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 订单明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">订单明细</span>
            <el-button type="primary" size="small" :icon="Plus" @click="addLine">
              添加行
            </el-button>
          </div>
        </template>

        <el-table :data="form.lines" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="物料" width="220">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.materialId"
                placeholder="请选择物料"
                filterable
                style="width: 100%"
                @change="(val: number | null) => onMaterialChange($index, val)"
              >
                <el-option
                  v-for="m in materialOptions"
                  :key="m.value"
                  :label="m.label"
                  :value="m.value"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :precision="0"
                style="width: 100%"
                @change="recalcLine($index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                style="width: 100%"
                @change="recalcLine($index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="140" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.amount) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                circle
                :disabled="form.lines.length <= 1"
                @click="removeLine($index)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 底部汇总 -->
      <el-card shadow="never" class="section-card">
        <el-row :gutter="20">
          <el-col :span="6" :offset="18">
            <div class="total-amount">
              <span class="total-label">金额合计：</span>
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
        <el-button type="success" @click="handleSubmit">提交并下达</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import { formatMoney, formatDate } from '~/utils'
import { useAuthStore } from '../../../stores/auth'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ==================== 编辑/新增模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据 ====================
const supplierOptions = ref([
  { label: '深圳华强电子有限公司', value: 1 },
  { label: '广州万国元件有限公司', value: 2 },
  { label: '东莞正泰科技有限公司', value: 3 },
  { label: '上海锐拓半导体有限公司', value: 4 },
])

const requisitionOptions = ref([
  { label: 'PR-00001 / 张三 / PCB主板等2项', value: 1 },
  { label: 'PR-00003 / 李四 / CPU处理器等3项', value: 3 },
  { label: 'PR-00005 / 王五 / 锂电池组等1项', value: 5 },
])

const materialOptions = ref([
  { label: 'PCB-001 主板基板', value: 1 },
  { label: 'CPU-002 中央处理器', value: 2 },
  { label: 'LCD-003 液晶显示屏', value: 3 },
  { label: 'BAT-004 锂电池组', value: 4 },
  { label: 'CHS-005 充电器套件', value: 5 },
  { label: 'ANT-006 天线模块', value: 6 },
])

// ==================== 表单数据 ====================
interface OrderLine {
  materialId: number | null
  materialName: string
  quantity: number
  price: number
  amount: number
}

interface OrderForm {
  supplierId: number | null
  requisitionId: number | null
  orderDate: string
  deliveryDate: string
  remark: string
  lines: OrderLine[]
}

function createEmptyLine(): OrderLine {
  return {
    materialId: null,
    materialName: '',
    quantity: 0,
    price: 0,
    amount: 0,
  }
}

const form = reactive<OrderForm>({
  supplierId: null,
  requisitionId: null,
  orderDate: formatDate(new Date(), 'YYYY-MM-DD'),
  deliveryDate: '',
  remark: '',
  lines: [createEmptyLine()],
})

// ==================== 表单校验规则 ====================
const formRef = ref()

const rules = {
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' },
  ],
  orderDate: [
    { required: true, message: '请选择采购日期', trigger: 'change' },
  ],
}

function validateLines(): boolean {
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (!line.materialId) {
      ElMessage.warning(`第 ${i + 1} 行：请选择物料`)
      return false
    }
    if (!line.quantity || line.quantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行：数量必须大于 0`)
      return false
    }
    if (!line.price || line.price <= 0) {
      ElMessage.warning(`第 ${i + 1} 行：单价必须大于 0`)
      return false
    }
  }
  return true
}

// ==================== 明细行操作 ====================
function addLine() {
  form.lines.push(createEmptyLine())
}

function removeLine(index: number) {
  if (form.lines.length <= 1) return
  form.lines.splice(index, 1)
}

function onMaterialChange(index: number, val: number | null) {
  const line = form.lines[index]
  if (val) {
    const mat = materialOptions.value.find((m) => m.value === val)
    line.materialName = mat?.label ?? ''
  } else {
    line.materialName = ''
  }
  recalcLine(index)
}

function recalcLine(index: number) {
  const line = form.lines[index]
  line.amount = parseFloat((line.quantity * line.price).toFixed(2))
}

// ==================== 金额合计 ====================
const totalAmount = computed(() => {
  return form.lines.reduce((sum, l) => sum + l.amount, 0)
})

// ==================== 采购申请联动 ====================
function onRequisitionChange(val: number | null) {
  if (!val) return
  // Mock: 根据采购申请加载物料明细
  const mockLines: OrderLine[] = [
    { materialId: 1, materialName: 'PCB-001 主板基板', quantity: 100, price: 25.50, amount: 2550.00 },
    { materialId: 2, materialName: 'CPU-002 中央处理器', quantity: 50, price: 180.00, amount: 9000.00 },
  ]
  form.lines = mockLines
  ElMessage.success('已从采购申请加载物料明细')
}

// ==================== Mock 数据（编辑模式） ====================
function loadMockOrder(_id: string) {
  form.supplierId = 1
  form.orderDate = '2025-06-15'
  form.deliveryDate = '2025-07-01'
  form.remark = '生产采购订单'
  form.lines = [
    { materialId: 1, materialName: 'PCB-001 主板基板', quantity: 100, price: 25.50, amount: 2550.00 },
    { materialId: 2, materialName: 'CPU-002 中央处理器', quantity: 50, price: 180.00, amount: 9000.00 },
    { materialId: 4, materialName: 'BAT-004 锂电池组', quantity: 200, price: 35.00, amount: 7000.00 },
  ]
}

// ==================== 提交操作 ====================
async function handleSaveDraft() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  ElMessage.success('草稿保存成功')
  router.push('/purchase/orders')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  ElMessage.success('采购订单已提交并下达')
  router.push('/purchase/orders')
}

// ==================== 预填逻辑（来自采购申请） ====================
function prefillFromRequisition(reqId: string) {
  form.requisitionId = Number(reqId)
  onRequisitionChange(form.requisitionId)
}

// ==================== 初始化 ====================
onMounted(() => {
  if (route.query.fromRequisition) {
    prefillFromRequisition(route.query.fromRequisition as string)
  }
  if (route.query.id) {
    loadMockOrder(route.query.id as string)
  }
})
</script>

<style scoped>
.po-create-page {
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

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
