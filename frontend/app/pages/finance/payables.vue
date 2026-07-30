<template>
  <div class="payables-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable filterable>
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
            <el-option label="未核销" value="UNPAID" />
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
        <span>应付账款</span>
      </template>

      <el-table v-loading="loading" :data="paginatedData" stripe border>
        <el-table-column prop="payableNo" label="应付单号" width="150" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="receivingNo" label="收货单号" width="140" />
        <el-table-column prop="payableAmount" label="应付金额" width="120">
          <template #default="{ row }">¥{{ (row.payableAmount ?? 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已付金额" width="120">
          <template #default="{ row }">¥{{ (row.paidAmount ?? 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="120">
          <template #default="{ row }">
            <span class="unpaid">¥{{ ((row.payableAmount ?? 0) - (row.paidAmount ?? 0)).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="应付日期" width="140" sortable>
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
    <el-dialog v-model="viewDialogVisible" title="应付单详情" width="650px">
      <template v-if="viewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="应付单号">{{ viewRow.payableNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ viewRow.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="收货单号">{{ viewRow.receivingNo }}</el-descriptions-item>
          <el-descriptions-item label="应付日期">{{ viewRow.dueDate }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ (viewRow.payableAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">¥{{ (viewRow.paidAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未付金额">
            <span class="unpaid">¥{{ ((viewRow.payableAmount ?? 0) - (viewRow.paidAmount ?? 0)).toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagMap[viewRow.status]" size="small">{{ statusLabelMap[viewRow.status] || viewRow.status }}</el-tag>
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
          <el-descriptions-item label="应付金额">¥{{ (reconcileRow.payableAmount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="未付金额">
            <span class="unpaid">¥{{ ((reconcileRow.payableAmount ?? 0) - (reconcileRow.paidAmount ?? 0)).toLocaleString() }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">可用的付款记录</el-divider>
        <el-table :data="availablePayments" stripe border @selection-change="handlePaymentSelection">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="paymentNo" label="付款单号" width="150" />
          <el-table-column prop="amount" label="付款金额" width="120">
            <template #default="{ row }">¥{{ row.amount.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="可核销金额" width="120">
            <template #default="{ row }">¥{{ ((row.amount ?? 0) - (row.reconciledAmount ?? 0)).toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="本次核销金额" min-width="160">
            <template #default="{ row: payment }">
              <el-input-number v-model="reconcileAmounts[payment.paymentNo]" :min="0"
                :max="Math.max(0, (payment.amount ?? 0) - (payment.reconciledAmount ?? 0))"
                :precision="2" controls-position="right" size="small" />
            </template>
          </el-table-column>
        </el-table>

        <div class="reconcile-summary">
          <span>本次核销合计：</span>
          <strong class="unpaid">¥{{ totalReconcileAmount.toLocaleString() }}</strong>
          <span class="reconcile-max">
            （可核销上限：¥{{ ((reconcileRow.payableAmount ?? 0) - (reconcileRow.paidAmount ?? 0)).toLocaleString() }}）
          </span>
        </div>
      </template>

      <template #footer>
        <el-button @click="reconcileDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="totalReconcileAmount <= 0 || totalReconcileAmount > ((reconcileRow.payableAmount ?? 0) - (reconcileRow.paidAmount ?? 0))"
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
interface Payable {
  id: number
  payableNo: string
  supplierName: string
  receivingNo: string
  payableAmount: number
  paidAmount: number
  dueDate: string
  status: string
}

interface PaymentRecord {
  paymentNo: string
  amount: number
  reconciledAmount: number
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  supplierId: '' as any,
  status: '' as string,
  dateRange: null as [string, string] | null,
})

const supplierOptions = ref<{ label: string; value: number }[]>([])

async function fetchSupplierOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/suppliers', 1, 1000)
    supplierOptions.value = result.list.map((item) => ({ label: item.name, value: item.id }))
  } catch {
    // options load silently; the dropdown just stays empty
  }
}

const statusLabelMap: Record<string, string> = {
  'UNPAID': '未核销',
  'PARTIALLY_PAID': '部分核销',
  'FULLY_PAID': '已核销',
  'PENDING': '未核销',
  'PAID': '已核销',
}

const statusTagMap: Record<string, 'warning' | 'success' | 'info'> = {
  'UNPAID': 'warning',
  'PARTIALLY_PAID': 'warning' as const,
  'FULLY_PAID': 'success',
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
    if (searchForm.supplierId) extraQuery.supplierId = searchForm.supplierId
    if (searchForm.status) extraQuery.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<any>(
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

async function handleDueDateChange(row: Payable, val: string) {
  if (!val) return
  try {
    await api.put(`/finance/payables/${row.id}/due-date`, { dueDate: val })
    ElMessage.success('应付日期已更新')
  } catch {
    ElMessage.error('更新失败')
  }
}

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

async function fetchAvailablePayments(_supplierName: string) {
  try {
    const result = await api.page<any>('/finance/payments', 1, 200, { type: 'PAY', status: 'CONFIRMED' })
    availablePayments.value = result.list
      .filter((p: any) => (p.amount ?? 0) > (p.reconciledAmount ?? 0))
      .map((p: any) => ({
      paymentNo: p.paymentNo || '',
      amount: p.amount ?? 0,
      reconciledAmount: p.reconciledAmount ?? 0,
    }))
  } catch { availablePayments.value = [] }
}

const totalReconcileAmount = computed(() =>
  selectedPayments.value.reduce((sum, p) => sum + (reconcileAmounts[p.paymentNo] || 0), 0),
)

function handleReconcile(row: Payable) {
  reconcileRow.value = row
  for (const key of Object.keys(reconcileAmounts)) delete reconcileAmounts[key]
  fetchAvailablePayments(row.supplierName)
  reconcileDialogVisible.value = true
}

watch(availablePayments, (payments) => {
  for (const key of Object.keys(reconcileAmounts)) delete reconcileAmounts[key]
  payments.forEach(p => { reconcileAmounts[p.paymentNo] = 0 })
})

function handlePaymentSelection(selection: PaymentRecord[]) {
  selectedPayments.value = selection
}

function resetReconcileForm() {
  for (const key of Object.keys(reconcileAmounts)) delete reconcileAmounts[key]
  selectedPayments.value = []
}

async function confirmReconcile() {
  if (!reconcileRow.value) return
  const total = totalReconcileAmount.value
  const unpaidAmount = (reconcileRow.value.payableAmount ?? 0) - (reconcileRow.value.paidAmount ?? 0)
  if (total <= 0 || total > unpaidAmount) {
    ElMessage.warning('核销金额无效')
    return
  }
  // 逐笔校验不超过各付款记录的可用金额
  for (const p of selectedPayments.value) {
    const amt = reconcileAmounts[p.paymentNo] || 0
    const available = (p.amount ?? 0) - (p.reconciledAmount ?? 0)
    if (amt > available) {
      ElMessage.warning(`付款单 ${p.paymentNo} 核销金额超出可核销金额`)
      return
    }
  }
  try {
    await api.post(`/finance/payables/${reconcileRow.value.id}/reconcile`, {
      amount: total,
      payments: selectedPayments.value.map(p => ({
        paymentNo: p.paymentNo,
        amount: reconcileAmounts[p.paymentNo] || 0,
      })),
    })
    ElMessage.success('核销成功')
    reconcileDialogVisible.value = false
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
  searchForm.supplierId = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

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
