<template>
  <div class="payments-page">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="收付款单号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入单号" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="收款" value="收款" />
            <el-option label="付款" value="付款" />
          </el-select>
        </el-form-item>
        <el-form-item label="往来单位">
          <el-input v-model="searchForm.counterparty" placeholder="请输入往来单位" clearable />
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
        <div class="card-header">
          <span>收付款管理</span>
          <el-button v-permission="'finance:order:view'" type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="paginatedData" stripe border>
        <el-table-column prop="paymentNo" label="单号" width="160" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === '收款' ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="counterparty" label="往来单位" min-width="160" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="reconciledAmount" label="核销金额" width="120">
          <template #default="{ row }">¥{{ row.reconciledAmount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="110" sortable />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已确认' ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'finance:order:view'" type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === '草稿'"
              v-permission="'finance:order:approve'"
              type="success"
              link
              size="small"
              @click.stop="handleConfirm(row)"
            >
              确认
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
    <el-dialog v-model="viewDialogVisible" title="收付款单详情" width="600px">
      <template v-if="viewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="单号">{{ viewRow.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="viewRow.type === '收款' ? 'success' : 'danger'" size="small">{{ viewRow.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="往来单位">{{ viewRow.counterparty }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ viewRow.date }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ viewRow.amount.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="核销金额">¥{{ viewRow.reconciledAmount.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="银行账户">{{ viewRow.bankAccount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewRow.status === '已确认' ? 'success' : 'info'" size="small">{{ viewRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ viewRow.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// ── Types ──────────────────────────────────────────
interface Payment {
  paymentNo: string
  type: '收款' | '付款'
  counterparty: string
  amount: number
  reconciledAmount: number
  date: string
  status: '草稿' | '已确认'
  bankAccount: string
  remark: string
}

// ── Search Form ────────────────────────────────────
const searchForm = reactive({
  paymentNo: '',
  type: '' as string,
  counterparty: '',
  dateRange: null as [string, string] | null,
})

// ── Data ───────────────────────────────────────────
const allData = ref<Payment[]>([])
const totalItems = ref(0)
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10 })

// ── Fetch ──────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, string | number | undefined> = {}
    if (searchForm.paymentNo) extraQuery.paymentNo = searchForm.paymentNo
    if (searchForm.type) extraQuery.type = searchForm.type
    if (searchForm.counterparty) extraQuery.counterparty = searchForm.counterparty
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<Payment>(
      '/finance/payments',
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
const viewRow = ref<Payment | null>(null)

function handleView(row: Payment) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ── Actions ────────────────────────────────────────
const router = useRouter()

function handleCreate() {
  router.push('/finance/payments/create')
}

async function handleConfirm(row: Payment) {
  try {
    await ElMessageBox.confirm(
      `确认将单据 ${row.paymentNo} 从"草稿"变更为"已确认"？`,
      '确认操作',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
    await api.post(`/finance/payments/${row.paymentNo}/confirm`)
    ElMessage.success('单据已确认')
    fetchData()
  } catch {
    // cancelled or error
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.paymentNo = ''
  searchForm.type = ''
  searchForm.counterparty = ''
  searchForm.dateRange = null
  pagination.page = 1
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ── Init ───────────────────────────────────────────
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.payments-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
