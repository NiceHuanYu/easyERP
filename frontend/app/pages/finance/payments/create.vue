<template>
  <div class="create-payment-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>新增收付款单</span>
        </div>
      </template>

      <!-- Header Form -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        class="header-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="form.type" @change="handleTypeChange">
                <el-radio value="收款">收款</el-radio>
                <el-radio value="付款">付款</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="往来单位" prop="counterparty">
              <el-select
                v-model="form.counterparty"
                :placeholder="form.type === '收款' ? '请选择客户' : '请选择供应商'"
                clearable
                filterable
                style="width: 100%"
                @change="handleCounterpartyChange"
              >
                <el-option
                  v-for="c in counterpartyOptions"
                  :key="c.value"
                  :label="c.label"
                  :value="c.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="金额" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0.01"
                :precision="2"
                :step="1000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="日期" prop="date">
              <el-date-picker
                v-model="form.date"
                type="date"
                placeholder="请选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="银行账户" prop="bankAccount">
              <el-select v-model="form.bankAccount" placeholder="请选择银行账户" style="width: 100%">
                <el-option label="工商银行 6222****8891" value="工商银行 6222****8891" />
                <el-option label="建设银行 6217****5623" value="建设银行 6217****5623" />
                <el-option label="中国银行 6216****3401" value="中国银行 6216****3401" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Write-off Details -->
    <el-card shadow="never" class="writeoff-card">
      <template #header>
        <div class="card-header">
          <span>核销明细</span>
          <span class="card-header-tip">
            合计核销金额：
            <strong :class="totalWriteOffAmount > form.amount ? 'exceed' : 'normal'">
              ¥{{ totalWriteOffAmount.toLocaleString() }}
            </strong>
            <span v-if="totalWriteOffAmount > form.amount" class="exceed-text">
              （超出金额 ¥{{ (totalWriteOffAmount - form.amount).toLocaleString() }}）
            </span>
          </span>
        </div>
      </template>

      <el-empty v-if="!form.counterparty" description="请先选择往来单位" />

      <template v-else>
        <el-alert
          v-if="openItems.length === 0"
          title="该往来单位没有待核销项目"
          type="success"
          :closable="false"
          show-icon
        />

        <el-table v-else :data="openItems" stripe border>
          <el-table-column
            :prop="form.type === '收款' ? 'receivableNo' : 'payableNo'"
            :label="form.type === '收款' ? '应收单号' : '应付单号'"
            width="150"
          />
          <el-table-column
            :prop="form.type === '收款' ? 'deliveryNo' : 'receivingNo'"
            :label="form.type === '收款' ? '发货单号' : '收货单号'"
            width="140"
          />
          <el-table-column prop="totalAmount" label="金额" width="130">
            <template #default="{ row }">¥{{ row.totalAmount.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="openAmount" label="未核销金额" width="130">
            <template #default="{ row }">
              <span class="open-amount">¥{{ row.openAmount.toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="到期日" width="110" />
          <el-table-column label="本次核销金额" min-width="180">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="writeOffAmounts[$index]"
                :min="0"
                :max="row.openAmount"
                :precision="2"
                controls-position="right"
                size="small"
                style="width: 100%"
              />
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- Actions -->
    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="primary"
        :disabled="totalWriteOffAmount > form.amount"
        :loading="submitting"
        @click="handleSubmit"
      >
        保存
      </el-button>
      <el-button
        type="success"
        :disabled="totalWriteOffAmount > form.amount"
        :loading="submitting"
        @click="handleSubmitAndConfirm"
      >
        保存并确认
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface OpenItem {
  receivableNo?: string
  payableNo?: string
  deliveryNo?: string
  receivingNo?: string
  totalAmount: number
  openAmount: number
  dueDate: string
}

// ── Form ───────────────────────────────────────────
const formRef = ref<FormInstance>()
const form = reactive({
  type: '收款' as '收款' | '付款',
  counterparty: '',
  amount: 0,
  date: new Date().toISOString().slice(0, 10),
  bankAccount: '',
  remark: '',
})

const formRules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  counterparty: [{ required: true, message: '请选择往来单位', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' },
  ],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  bankAccount: [{ required: true, message: '请选择银行账户', trigger: 'change' }],
}

// ── Counterparty Options ───────────────────────────
const customerOptions = [
  { label: '深圳创新科技有限公司', value: '深圳创新科技有限公司' },
  { label: '广州宏达实业集团', value: '广州宏达实业集团' },
  { label: '上海明远电子有限公司', value: '上海明远电子有限公司' },
  { label: '北京天成机械制造厂', value: '北京天成机械制造厂' },
  { label: '杭州华威贸易公司', value: '杭州华威贸易公司' },
]

const supplierOptions = [
  { label: '深圳钢贸有限公司', value: '深圳钢贸有限公司' },
  { label: '广州精工五金厂', value: '广州精工五金厂' },
  { label: '上海华龙电子材料', value: '上海华龙电子材料' },
  { label: '北京北方轴承集团', value: '北京北方轴承集团' },
  { label: '杭州恒达化工有限公司', value: '杭州恒达化工有限公司' },
]

const counterpartyOptions = computed(() =>
  form.type === '收款' ? customerOptions : supplierOptions,
)

// ── Open Items ─────────────────────────────────────
const openItems = ref<OpenItem[]>([])
const writeOffAmounts = ref<number[]>([])

const totalWriteOffAmount = computed(() =>
  writeOffAmounts.value.reduce((sum, val) => sum + (val || 0), 0),
)

function generateOpenReceivables(customerName: string): OpenItem[] {
  const date = new Date()
  return [
    {
      receivableNo: 'AR-000023',
      deliveryNo: 'SO-DLV-0045',
      totalAmount: 45000,
      openAmount: 28000,
      dueDate: new Date(date.getTime() + 15 * 86400000).toISOString().slice(0, 10),
    },
    {
      receivableNo: 'AR-000031',
      deliveryNo: 'SO-DLV-0058',
      totalAmount: 32000,
      openAmount: 32000,
      dueDate: new Date(date.getTime() + 30 * 86400000).toISOString().slice(0, 10),
    },
    {
      receivableNo: 'AR-000045',
      deliveryNo: 'SO-DLV-0072',
      totalAmount: 68000,
      openAmount: 40000,
      dueDate: new Date(date.getTime() - 10 * 86400000).toISOString().slice(0, 10),
    },
    {
      receivableNo: 'AR-000052',
      deliveryNo: 'SO-DLV-0089',
      totalAmount: 15000,
      openAmount: 15000,
      dueDate: new Date(date.getTime() + 45 * 86400000).toISOString().slice(0, 10),
    },
  ]
}

function generateOpenPayables(supplierName: string): OpenItem[] {
  const date = new Date()
  return [
    {
      payableNo: 'AP-000018',
      receivingNo: 'PO-RCV-0032',
      totalAmount: 38000,
      openAmount: 22000,
      dueDate: new Date(date.getTime() + 20 * 86400000).toISOString().slice(0, 10),
    },
    {
      payableNo: 'AP-000027',
      receivingNo: 'PO-RCV-0047',
      totalAmount: 55000,
      openAmount: 55000,
      dueDate: new Date(date.getTime() + 35 * 86400000).toISOString().slice(0, 10),
    },
    {
      payableNo: 'AP-000033',
      receivingNo: 'PO-RCV-0061',
      totalAmount: 24000,
      openAmount: 18000,
      dueDate: new Date(date.getTime() - 5 * 86400000).toISOString().slice(0, 10),
    },
  ]
}

// ── Event Handlers ─────────────────────────────────
function handleTypeChange() {
  form.counterparty = ''
  openItems.value = []
  writeOffAmounts.value = []
}

function handleCounterpartyChange() {
  if (!form.counterparty) {
    openItems.value = []
    writeOffAmounts.value = []
    return
  }

  if (form.type === '收款') {
    openItems.value = generateOpenReceivables(form.counterparty)
  } else {
    openItems.value = generateOpenPayables(form.counterparty)
  }

  writeOffAmounts.value = new Array(openItems.value.length).fill(0)
}

// ── Submit ─────────────────────────────────────────
const submitting = ref(false)
const router = useRouter()

async function handleSubmit() {
  await doSubmit('草稿')
}

async function handleSubmitAndConfirm() {
  await doSubmit('已确认')
}

async function doSubmit(status: '草稿' | '已确认') {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (totalWriteOffAmount.value > form.amount) {
    ElMessage.error('核销金额合计不能超过收付款金额')
    return
  }

  submitting.value = true

  // Simulate API call
  await new Promise((resolve) => setTimeout(resolve, 800))

  ElMessage.success(status === '已确认' ? '保存并确认成功' : '保存成功')
  submitting.value = false
  router.push('/finance/payments')
}

function handleCancel() {
  router.push('/finance/payments')
}
</script>

<style scoped>
.create-payment-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-tip {
  font-size: 14px;
  color: #606266;
}

.card-header-tip .normal {
  color: #409eff;
}

.card-header-tip .exceed {
  color: #f56c6c;
}

.exceed-text {
  color: #f56c6c;
  font-size: 13px;
}

.header-form {
  max-width: 100%;
}

.writeoff-card {
  flex: 1;
}

.open-amount {
  color: #f56c6c;
  font-weight: 600;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0;
}
</style>
