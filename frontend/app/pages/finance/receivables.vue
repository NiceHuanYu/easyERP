<template>
  <div class="receivables-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable>
            <el-option
              v-for="c in customerOptions"
              :key="c.value"
              :label="c.label"
              :value="c.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未核销" value="PENDING" />
            <el-option label="部分核销" value="PARTIALLY_PAID" />
            <el-option label="已核销" value="FULLY_PAID" />
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
        <span>应收账款</span>
      </template>

      <el-table v-loading="loading" :data="paginatedData" stripe border>
        <el-table-column prop="receivableNo" label="应收单号" width="150" />
        <el-table-column prop="customerName" label="客户" min-width="160" />
        <el-table-column prop="deliveryNo" label="发货单号" width="140" />
        <el-table-column prop="receivableAmount" label="应收金额" width="120">
          <template #default="{ row }">¥{{ (row.receivableAmount ?? 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="receivedAmount" label="已收金额" width="120">
          <template #default="{ row }">¥{{ (row.receivedAmount ?? 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="未收金额" width="120">
          <template #default="{ row }">
            <span class="unreceived">¥{{ ((row.receivableAmount ?? 0) - (row.receivedAmount ?? 0)).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="应收日期" width="140" sortable>
          <template #default="{ row }">
            <el-date-picker
              v-model="row.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              size="small"
              style="width:130px"
              @change="(val: string) => handleDueDateChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" size="small" effect="plain">
              {{ statusLabelMap[row.status] || row.status }}
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
              :disabled="row.status === 'FULLY_PAID' || row.status === 'PAID'"
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
      />
    </el-card>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="应收单详情" width="650px">
      <template v-if="viewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="应收单号">{{ viewRow.receivableNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ viewRow.customerName }}</el-descriptions-item>
          <el-descriptions-item label="发货单号">{{ viewRow.deliveryNo }}</el-descriptions-item>
          <el-descriptions-item label="应收日期">{{ viewRow.dueDate }}</el-descriptions-item>
          <el-descriptions-item label="应收金额">¥{{ (viewRow.receivableAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="已收金额">¥{{ (viewRow.receivedAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未收金额">
            <span class="unreceived">¥{{ ((viewRow.receivableAmount ?? 0) - (viewRow.receivedAmount ?? 0)).toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagMap[viewRow.status]" size="small">{{ statusLabelMap[viewRow.status] || viewRow.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- Reconcile Dialog -->
    <el-dialog v-model="reconcileDialogVisible" title="核销应收账款" width="800px" @close="resetReconcileForm">
      <template v-if="reconcileRow">
        <el-descriptions :column="2" border class="reconcile-desc">
          <el-descriptions-item label="应收单号">{{ reconcileRow.receivableNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ reconcileRow.customerName }}</el-descriptions-item>
          <el-descriptions-item label="应收金额">¥{{ (reconcileRow.receivableAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未收金额">
            <span class="unreceived">¥{{ ((reconcileRow.receivableAmount ?? 0) - (reconcileRow.receivedAmount ?? 0)).toLocaleString() }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">可用的收款记录</el-divider>

        <el-table
          :data="availablePayments"
          stripe
          border
          @selection-change="handlePaymentSelection"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="paymentNo" label="收款单号" width="150" />
          <el-table-column prop="amount" label="收款金额" width="120">
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
          <strong class="unreceived">¥{{ totalReconcileAmount.toLocaleString() }}</strong>
          <span class="reconcile-max">
            （可核销上限：¥{{ ((reconcileRow.receivableAmount ?? 0) - (reconcileRow.receivedAmount ?? 0)).toLocaleString() }}）
          </span>
        </div>
      </template>

      <template #footer>
        <el-button @click="reconcileDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="totalReconcileAmount <= 0 || totalReconcileAmount > ((reconcileRow.receivableAmount ?? 0) - (reconcileRow.receivedAmount ?? 0))"
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
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface Receivable {
  id: number
  receivableNo: string
  customerName: string
  deliveryNo: string
  receivableAmount: number
  receivedAmount: number
  dueDate: string
  status: string
}

interface Payment {
  paymentNo: string
  amount: number
  availableAmount: number
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  customerId: '' as any,
  status: '' as string,
  dateRange: null as [string, string] | null,
})

const customerOptions = ref<{ label: string; value: number }[]>([])

async function fetchCustomerOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/customers', 1, 1000)
    customerOptions.value = result.list.map((item) => ({ label: item.name, value: item.id }))
  } catch {
    // options load silently; the dropdown just stays empty
  }
}

const statusLabelMap: Record<string, string> = {
  'PENDING': '未核销',
  'PARTIALLY_PAID': '部分核销',
  'FULLY_PAID': '已核销',
  'UNPAID': '未核销',
  'PAID': '已核销',
}

const statusTagMap: Record<string, 'warning' | 'success' | 'info'> = {
  'PENDING': 'warning',
  'UNPAID': 'warning',
  'PARTIALLY_PAID': 'warning' as const,
  'FULLY_PAID': 'success',
  'PAID': 'success',
}

// ── Data ───────────────────────────────────────────
const allData = ref<Receivable[]>([])
const totalItems = ref(0)
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10 })

// ── Fetch ──────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, string | number | undefined> = {}
    if (searchForm.customerId) extraQuery.customerId = searchForm.customerId
    if (searchForm.status) extraQuery.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<any>(
      '/finance/receivables',
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
const viewRow = ref<Receivable | null>(null)

async function handleDueDateChange(row: Receivable, val: string) {
  if (!val) return
  try {
    await api.put(`/finance/receivables/${row.id}/due-date`, { dueDate: val })
    ElMessage.success('应收日期已更新')
  } catch {
    ElMessage.error('更新失败')
  }
}

function handleView(row: Receivable) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ── Reconcile Dialog ───────────────────────────────
const reconcileDialogVisible = ref(false)
const reconcileRow = ref<Receivable | null>(null)
const availablePayments = ref<Payment[]>([])
const reconcileAmounts = reactive<Record<string, number>>({})
const selectedPayments = ref<Payment[]>([])

async function fetchAvailablePayments(_customerName: string) {
  try {
    const result = await api.page<any>('/finance/payments', 1, 200, { type: 'RECEIVE' })
    availablePayments.value = result.list.map((p: any) => ({
      paymentNo: p.paymentNo,
      amount: p.amount,
      availableAmount: p.amount ?? 0,
    }))
  } catch {
    // Fallback: empty list
    availablePayments.value = []
  }
}

const totalReconcileAmount = computed(() =>
  Object.values(reconcileAmounts).reduce((sum, val) => sum + (val || 0), 0),
)

function handleReconcile(row: Receivable) {
  reconcileRow.value = row
  fetchAvailablePayments(row.customerName)

  // Reset amounts
  for (const key of Object.keys(reconcileAmounts)) {
    delete reconcileAmounts[key]
  }

  reconcileDialogVisible.value = true
}

// Watch availablePayments to init amounts once loaded
watch(availablePayments, (payments) => {
  for (const key of Object.keys(reconcileAmounts)) {
    delete reconcileAmounts[key]
  }
  payments.forEach((p) => {
    reconcileAmounts[p.paymentNo] = 0
  })
})

function handlePaymentSelection(selection: Payment[]) {
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
  const unpaidAmount = (reconcileRow.value.receivableAmount ?? 0) - (reconcileRow.value.receivedAmount ?? 0)
  if (total <= 0 || total > unpaidAmount) {
    ElMessage.warning('核销金额无效')
    return
  }

  try {
    await api.post(`/finance/receivables/${reconcileRow.value.id}/reconcile`, {
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
  searchForm.customerId = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ── Init ───────────────────────────────────────────
onMounted(() => {
  fetchCustomerOptions()
  fetchData()
})
</script>

<style scoped>
.receivables-page {
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

.unreceived {
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
