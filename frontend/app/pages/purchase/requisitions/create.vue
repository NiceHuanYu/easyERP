<template>
  <div class="requisition-create-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ isEdit ? '编辑采购申请' : '新增采购申请' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="requisition-form"
    >
      <!-- 申请头信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="section-title">申请信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申请日期" prop="reqDate">
              <el-date-picker
                v-model="form.reqDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申请人" prop="applicantId">
              <el-select
                v-model="form.applicantId"
                placeholder="请选择申请人"
                style="width: 100%"
              >
                <el-option
                  v-for="e in employeeOptions"
                  :key="e.value"
                  :label="e.label"
                  :value="e.value"
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

      <!-- 申请明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">申请明细</span>
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
          <el-table-column label="规格" width="160">
            <template #default="{ row }">
              <el-input
                :model-value="row.spec"
                readonly
                placeholder="自动带出"
              />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="100">
            <template #default="{ row }">
              <el-input
                :model-value="row.unit"
                readonly
                placeholder="自动带出"
              />
            </template>
          </el-table-column>
          <el-table-column label="申请数量" width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :precision="0"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="建议供应商" width="200">
            <template #default="{ row }">
              <el-select
                v-model="row.suggestedSupplierId"
                placeholder="请选择"
                clearable
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
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="handleSubmit">提交</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import { formatDate } from '~/utils'
import { useAuthStore } from '../../../stores/auth'

definePageMeta({ middleware: 'auth' })

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ==================== 编辑/新增模式 ====================
const isEdit = computed(() => !!route.query.id)

// ==================== 选项数据 ====================
const employeeOptions = ref([
  { label: '张三', value: 1 },
  { label: '李四', value: 2 },
  { label: '王五', value: 3 },
  { label: '赵六', value: 4 },
])

const materialOptions = ref([
  { label: 'PCB-001 主板基板', value: 1, spec: 'FR-4 1.6mm', unit: '片' },
  { label: 'CPU-002 中央处理器', value: 2, spec: 'ARM Cortex-A78', unit: '个' },
  { label: 'LCD-003 液晶显示屏', value: 3, spec: '7寸 1024x600', unit: '块' },
  { label: 'BAT-004 锂电池组', value: 4, spec: '3.7V 5000mAh', unit: '组' },
  { label: 'CHS-005 充电器套件', value: 5, spec: '5V 2A USB-C', unit: '套' },
  { label: 'ANT-006 天线模块', value: 6, spec: '2.4GHz 3dBi', unit: '个' },
])

const supplierOptions = ref([
  { label: '深圳华强电子有限公司', value: 1 },
  { label: '广州万国元件有限公司', value: 2 },
  { label: '东莞正泰科技有限公司', value: 3 },
  { label: '上海锐拓半导体有限公司', value: 4 },
])

// ==================== 表单数据 ====================
interface RequisitionLine {
  materialId: number | null
  materialName: string
  spec: string
  unit: string
  quantity: number
  suggestedSupplierId: number | null
}

interface RequisitionForm {
  reqDate: string
  applicantId: number | null
  remark: string
  lines: RequisitionLine[]
}

function createEmptyLine(): RequisitionLine {
  return {
    materialId: null,
    materialName: '',
    spec: '',
    unit: '',
    quantity: 0,
    suggestedSupplierId: null,
  }
}

const form = reactive<RequisitionForm>({
  reqDate: formatDate(new Date(), 'YYYY-MM-DD'),
  applicantId: null,
  remark: '',
  lines: [createEmptyLine()],
})

// ==================== 表单校验规则 ====================
const formRef = ref()

const rules = {
  reqDate: [
    { required: true, message: '请选择申请日期', trigger: 'change' },
  ],
  applicantId: [
    { required: true, message: '请选择申请人', trigger: 'change' },
  ],
}

function validateLines(): boolean {
  if (form.lines.length === 0) {
    ElMessage.warning('请至少添加一行物料')
    return false
  }
  for (let i = 0; i < form.lines.length; i++) {
    const line = form.lines[i]
    if (!line.materialId) {
      ElMessage.warning(`第 ${i + 1} 行：请选择物料`)
      return false
    }
    if (!line.quantity || line.quantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行：申请数量必须大于 0`)
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
    line.spec = (mat as any)?.spec ?? ''
    line.unit = (mat as any)?.unit ?? ''
  } else {
    line.materialName = ''
    line.spec = ''
    line.unit = ''
  }
}

// ==================== Mock 数据（编辑模式） ====================
function loadMockRequisition(_id: string) {
  form.reqDate = '2025-06-10'
  form.applicantId = 1
  form.remark = '生产用料申请'
  form.lines = [
    {
      materialId: 1,
      materialName: 'PCB-001 主板基板',
      spec: 'FR-4 1.6mm',
      unit: '片',
      quantity: 100,
      suggestedSupplierId: 1,
    },
    {
      materialId: 2,
      materialName: 'CPU-002 中央处理器',
      spec: 'ARM Cortex-A78',
      unit: '个',
      quantity: 50,
      suggestedSupplierId: 4,
    },
  ]
}

// ==================== 提交操作 ====================
async function handleSaveDraft() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  ElMessage.success('草稿保存成功')
  router.push('/purchase/requisitions')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!validateLines()) return

  ElMessage.success('申请提交成功')
  router.push('/purchase/requisitions')
}

// ==================== 初始化 ====================
onMounted(() => {
  if (route.query.id) {
    loadMockRequisition(route.query.id as string)
  }
})
</script>

<style scoped>
.requisition-create-page {
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
