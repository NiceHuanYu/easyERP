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
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
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
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === '草稿'"
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
        :total="filteredData.length"
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

// ── Mock Data ──────────────────────────────────────
function generateMockPayments(): Payment[] {
  const types: Array<'收款' | '付款'> = ['收款', '付款']
  const counterparties = [
    '深圳创新科技有限公司',
    '广州宏达实业集团',
    '上海明远电子有限公司',
    '深圳钢贸有限公司',
    '广州精工五金厂',
    '北京北方轴承集团',
    '杭州华威贸易公司',
    '杭州恒达化工有限公司',
  ]
  const statuses: Array<'草稿' | '已确认'> = ['草稿', '已确认']
  const banks = ['工商银行 6222****8891', '建设银行 6217****5623', '中国银行 6216****3401']

  const data: Payment[] = []
  for (let i = 0; i < 35; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const amount = Math.floor(Math.random() * 100000) + 5000
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const reconciledAmount = status === '已确认' ? Math.floor(amount * (0.3 + Math.random() * 0.7)) : 0

    const date = new Date()
    date.setDate(date.getDate() - Math.floor(Math.random() * 60))
    const dateStr = date.toISOString().slice(0, 10)

    data.push({
      paymentNo: type === '收款' ? `RCV-${String(i + 1).padStart(6, '0')}` : `PMT-${String(i + 1).padStart(6, '0')}`,
      type,
      counterparty: counterparties[Math.floor(Math.random() * counterparties.length)],
      amount,
      reconciledAmount,
      date: dateStr,
      status,
      bankAccount: banks[Math.floor(Math.random() * banks.length)],
      remark: i % 5 === 0 ? '备注信息：已核对发票' : '',
    })
  }

  data.sort((a, b) => b.date.localeCompare(a.date) || b.paymentNo.localeCompare(a.paymentNo))
  return data
}

const mockData = ref<Payment[]>(generateMockPayments())
const loading = ref(false)

// ── Pagination ─────────────────────────────────────
const pagination = reactive({ page: 1, pageSize: 10 })

// ── Filtering ──────────────────────────────────────
const filteredData = computed(() => {
  let list = mockData.value

  if (searchForm.paymentNo) {
    list = list.filter((item) =>
      item.paymentNo.toLowerCase().includes(searchForm.paymentNo.toLowerCase()),
    )
  }
  if (searchForm.type) {
    list = list.filter((item) => item.type === searchForm.type)
  }
  if (searchForm.counterparty) {
    list = list.filter((item) => item.counterparty.includes(searchForm.counterparty))
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [start, end] = searchForm.dateRange
    list = list.filter((item) => item.date >= start && item.date <= end)
  }

  return list
})

const paginatedData = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredData.value.slice(start, start + pagination.pageSize)
})

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

function handleConfirm(row: Payment) {
  ElMessageBox.confirm(
    `确认将单据 ${row.paymentNo} 从"草稿"变更为"已确认"？`,
    '确认操作',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
  ).then(() => {
    const item = mockData.value.find((p) => p.paymentNo === row.paymentNo)
    if (item) {
      item.status = '已确认'
    }
    ElMessage.success('单据已确认')
  }).catch(() => {
    // cancelled
  })
}

function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  searchForm.paymentNo = ''
  searchForm.type = ''
  searchForm.counterparty = ''
  searchForm.dateRange = null
  pagination.page = 1
}
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
