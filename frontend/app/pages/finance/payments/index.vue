<template>
  <div class="payments-page">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="收付款单号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入单号" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="收款" value="RECEIVE" />
            <el-option label="付款" value="PAY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>收付款管理</span>
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="paginatedData" stripe border>
        <el-table-column prop="paymentNo" label="单号" width="160" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="(row.type === 'RECEIVE' || row.type === '收款') ? 'success' : 'danger'" size="small">
              {{ (row.type === 'RECEIVE' || row.type === '收款') ? '收款' : '付款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="往来单位" min-width="160" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ (row.amount ?? 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="日期" width="110" sortable />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="(row.status === 'CONFIRMED' || row.status === '已确认') ? 'success' : 'info'" size="small">
              {{ row.status === 'DRAFT' || row.status === '草稿' ? '草稿' : row.status === 'CONFIRMED' || row.status === '已确认' ? '已确认' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'DRAFT' || row.status === '草稿'" type="success" link size="small" @click.stop="handleConfirm(row)">
              确认
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="totalItems" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
        background class="pagination" />
    </el-card>

    <el-dialog v-model="viewDialogVisible" title="收付款单详情" width="600px">
      <template v-if="viewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="单号">{{ viewRow.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ (viewRow.type === 'RECEIVE' || viewRow.type === '收款') ? '收款' : '付款' }}</el-descriptions-item>
          <el-descriptions-item label="往来单位">{{ viewRow.counterpartyName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ viewRow.paymentDate }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ (viewRow.amount ?? 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="银行账户">{{ viewRow.companyAccountName || viewRow.bankAccount || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ viewRow.status === 'DRAFT' || viewRow.status === '草稿' ? '草稿' : viewRow.status === 'CONFIRMED' || viewRow.status === '已确认' ? '已确认' : viewRow.status }}</el-descriptions-item>
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
import { computed, onMounted, reactive, ref } from 'vue'

definePageMeta({ middleware: 'auth' })

const searchForm = reactive({
  paymentNo: '',
  type: '' as string,
})

const allData = ref<any[]>([])
const totalItems = ref(0)
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 10 })

async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, any> = {}
    if (searchForm.paymentNo) extraQuery.paymentNo = searchForm.paymentNo
    if (searchForm.type) extraQuery.type = searchForm.type
    const result = await api.page<any>('/finance/payments', pagination.page, pagination.pageSize, extraQuery)
    allData.value = result.list
    totalItems.value = result.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const paginatedData = computed(() => allData.value)

const viewDialogVisible = ref(false)
const viewRow = ref<any>(null)
function handleView(row: any) { viewRow.value = row; viewDialogVisible.value = true }

const router = useRouter()
function handleCreate() { router.push('/finance/payments/create') }

async function handleConfirm(row: any) {
  try {
    await ElMessageBox.confirm('确认变更单据状态？', '确认操作',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await api.post(`/finance/payments/${row.id}/confirm`)
    ElMessage.success('单据已确认')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.paymentNo = ''; searchForm.type = ''; pagination.page = 1; fetchData() }

onMounted(() => { fetchData() })
</script>

<style scoped>
.payments-page { display: flex; flex-direction: column; gap: 16px; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
