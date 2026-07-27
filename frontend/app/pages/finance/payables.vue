<template>
  <div class="payables-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplier" placeholder="请选择供应商" clearable filterable>
            <el-option
              v-for="s in supplierOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未核销" value="未核销" />
            <el-option label="部分核销" value="部分核销" />
            <el-option label="已核销" value="已核销" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <span>应付账款</span>
      </template>

      <el-table v-loading="loading" :data="paginatedData" stripe border>
        <el-table-column prop="payableNo" label="应付单号" width="150" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="receivingNo" label="收货单号" width="140" />
        <el-table-column prop="amount" label="应付金额" width="120">
          <template #default="{ row }">¥{{ row.amount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已付金额" width="120">
          <template #default="{ row }">¥{{ row.paidAmount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="unpaidAmount" label="未付金额" width="120">
          <template #default="{ row }">
            <span class="unpaid">¥{{ row.unpaidAmount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="应付日期" width="110" sortable />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" size="small" effect="plain">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button
              type="warning"
              link
              size="small"
              :disabled="row.status === '已核销'"
              @click.stop="handleReconcile(row)"
            >
              核销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="totalItems"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="应付单详情" width="650px">
      <template v-if="viewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="应付单号">{{ viewRow.payableNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ viewRow.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="收货单号">{{ viewRow.receivingNo }}</el-descriptions-item>
          <el-descriptions-item label="应付日期">{{ viewRow.dueDate }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ viewRow.amount.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">¥{{ viewRow.paidAmount.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未付金额">
            <span class="unpaid">¥{{ viewRow.unpaidAmount.toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagMap[viewRow.status]" size="small">{{ viewRow.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- Reconcile Dialog -->
    <el-dialog v-model="reconcileDialogVisible" title="核销应付账款" width="800px" @close="resetReconcileForm">
      <template v-if="reconcileRow">
        <el-descriptions :column="2" border class="reconcile-desc">
          <el-descriptions-item label="应付单号">{{ reconcileRow.payableNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ reconcileRow.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ reconcileRow.amount.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未付金额">
            <span class="unpaid">¥{{ reconcileRow.unpaidAmount.toLocaleString() }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">可用的付款记录</el-divider>

        <el-table
          :data="availablePayments"
          stripe
          border
          @selection-change="handlePaymentSelection"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="paymentNo" label="付款单号" width="150" />
          <el-table-column prop="amount" label="付款金额" width="120">
            <template #default="{ row }">¥{{ row.amount.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="availableAmount" label="可核销金额" width="120">
            <template #default="{ row }">¥{{ row.availableAmount.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="本次核销金额" min-width="160">
            <template #default="{ row: payment }">
              <el-input-number
                v-model="reconcileAmounts[payment.paymentNo]"
                :min="0"
                :max="payment.availableAmount"
                :precision="2"
                controls-position="right"
                size="small"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="reconcile-summary">
          <span>本次核销合计：</span>
          <strong class="unpaid">¥{{ totalReconcileAmount.toLocaleString() }}</strong>
          <span class="reconcile-max">
            （可核销上限：¥{{ reconcileRow.unpaidAmount.toLocaleString() }}）
          </span>
        </div>
      </template>

      <template #footer>
        <el-button @click="reconcileDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="totalReconcileAmount <= 0 || totalReconcileAmount > reconcileRow!.unpaidAmount"
          @click="confirmReconcile"
        >
          确认核销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface Payable {
  payableNo: string
  supplierName: string
  receivingNo: string
  amount: number
  paidAmount: number
  unpaidAmount: number
  dueDate: string
  status: '未核销' | '部分核销' | '已核销'
}

interface PaymentRecord {
  paymentNo: string
  amount: number
  availableAmount: number
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  supplier: '',
  status: '' as string,
  dateRange: null as [string, string] | null,
})

const supplierOptions = ref<{ label: string; value: string }[]>([])

async function fetchSupplierOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/suppliers', 1, 1000)
    supplierOptions.value = result.list.map((item) => ({ label: item.name, value: item.name }))
  } catch {
    // options load silently; the dropdown just stays empty
  }
}

const statusTagMap: Record<string, 'warning' | 'success' | 'info'> = {
  '未核销': 'warning',
  '部分核销': '',
  '已核销': 'success',
}

// ── Data ───────────────────────────────────────────
const allData = ref<Payable[]>([])
const totalItems = ref(0)
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10 })

// ── Fetch ──────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, string | number | undefined> = {}
    if (searchForm.supplier) extraQuery.supplier = searchForm.supplier
    if (searchForm.status) extraQuery.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<Payable>(
      '/finance/payables',
      pagination.page,
      pagination.pageSize,
      extraQuery,
    )
    allData.value = result.list
    totalItems.value = result.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const paginatedData = computed(() => allData.value)

// ── View Dialog ────────────────────────────────────
const viewDialogVisible = ref(false)
const viewRow = ref<Payable | null>(null)

function handleView(row: Payable) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ── Reconcile Dialog ───────────────────────────────
const reconcileDialogVisible = ref(false)
const reconcileRow = ref<Payable | null>(null)
const availablePayments = ref<PaymentRecord[]>([])
const reconcileAmounts = reactive<Record<string, number>>({})
const selectedPayments = ref<PaymentRecord[]>([])

async function fetchAvailablePayments(supplierName: string) {
  try {
    const result = await api.get<any[]>('/finance/payments', { type: '付款', counterparty: supplierName })
    availablePayments.value = (result || []).map((p: any) => ({
      paymentNo: p.paymentNo,
      amount: p.amount,
      availableAmount: (p.amount ?? 0) - (p.reconciledAmount ?? 0),
    }))
  } catch {
    availablePayments.value = []
  }
}

const totalReconcileAmount = computed(() =>
  Object.values(reconcileAmounts).reduce((sum, val) => sum + (val || 0), 0),
)

function handleReconcile(row: Payable) {
  reconcileRow.value = row
  fetchAvailablePayments(row.supplierName)

  for (const key of Object.keys(reconcileAmounts)) {
    delete reconcileAmounts[key]
  }

  reconcileDialogVisible.value = true
}

watch(availablePayments, (payments) => {
  for (const key of Object.keys(reconcileAmounts)) {
    delete reconcileAmounts[key]
  }
  payments.forEach((p) => {
    reconcileAmounts[p.paymentNo] = 0
  })
})

function handlePaymentSelection(selection: PaymentRecord[]) {
  selectedPayments.value = selection
}

function resetReconcileForm() {
  for (const key of Object.keys(reconcileAmounts)) {
    delete reconcileAmounts[key]
  }
  selectedPayments.value = []
}

async function confirmReconcile() {
  if (!reconcileRow.value) return

  const total = totalReconcileAmount.value
  if (total <= 0 || total > reconcileRow.value.unpaidAmount) {
    ElMessage.warning('核销金额无效')
    return
  }

  try {
    await api.post(`/finance/payables/${reconcileRow.value.payableNo}/reconcile`, {
      reconcileAmount: total,
      payments: Object.entries(reconcileAmounts)
        .filter(([, amount]) => amount > 0)
        .map(([paymentNo, amount]) => ({ paymentNo, amount })),
    })
    ElMessage.success(`核销成功，本次核销 ¥${total.toLocaleString()}`)
    reconcileDialogVisible.value = false
    resetReconcileForm()
    fetchData()
  } catch {
    ElMessage.error('核销失败')
  }
}

// ── Actions ────────────────────────────────────────
function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.supplier = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
}

// ── Init ───────────────────────────────────────────
onMounted(() => {
  fetchSupplierOptions()
  fetchData()
})
</script>

<style scoped>
.payables-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.unpaid {
  color: #f56c6c;
  font-weight: 600;
}

.reconcile-desc {
  margin-bottom: 16px;
}

.reconcile-summary {
  margin-top: 16px;
  text-align: right;
  font-size: 16px;
}

.reconcile-max {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}
</style>
