<template>
  <div class="sales-orders-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单编号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单编号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="客户">
          <el-select
            v-model="searchForm.customerId"
            placeholder="请选择客户"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="c in customerOptions"
              :key="c.value"
              :label="c.label"
              :value="c.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            style="width: 200px"
          >
            <el-option label="草稿" value="draft" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已审核" value="approved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 & 表格 -->
    <el-card class="table-card" shadow="never">
      <!-- 顶部操作栏 -->
      <div class="action-bar">
        <div class="action-bar-left">
          <el-button
            v-permission="'sales:order:create'"
            type="primary"
            :icon="Plus"
            @click="navigateTo('/sales/orders/create')"
          >
            新增订单
          </el-button>
          <el-button
            v-permission="'sales:order:delete'"
            type="danger"
            :icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="customerName" label="客户" width="160" />
        <el-table-column prop="orderDate" label="订单日期" width="120" />
        <el-table-column prop="deliveryDate" label="交货日期" width="120" />
        <el-table-column prop="totalAmount" label="金额总计" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'draft'">
              <el-button
                v-permission="'sales:order:edit'"
                type="primary"
                size="small"
                link
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-popconfirm
                title="确认删除该订单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'sales:order:delete'"
                    type="danger"
                    size="small"
                    link
                  >
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认提交该订单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleSubmit(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'sales:order:submit'"
                    type="warning"
                    size="small"
                    link
                  >
                    提交
                  </el-button>
                </template>
              </el-popconfirm>
            </template>

            <template v-else-if="row.status === 'submitted'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
              <el-popconfirm
                title="确认审核通过该订单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleApprove(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'sales:order:approve'"
                    type="success"
                    size="small"
                    link
                  >
                    审核
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认反审核该订单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleUnapprove(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'sales:order:approve'"
                    type="warning"
                    size="small"
                    link
                  >
                    反审核
                  </el-button>
                </template>
              </el-popconfirm>
            </template>

            <template v-else-if="row.status === 'approved'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
              <el-popconfirm
                title="确认生成生产工单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleCreateProductionOrder(row)"
              >
                <template #reference>
                  <el-button type="success" size="small" link>
                    生成生产工单
                  </el-button>
                </template>
              </el-popconfirm>
              <el-button
                type="warning"
                size="small"
                link
                @click="handleCreateDelivery(row)"
              >
                生成发货单
              </el-button>
            </template>

            <template v-else-if="row.status === 'closed'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { formatMoney } from '~/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// ==================== 类型定义 ====================
interface SalesOrder {
  id: number
  orderNo: string
  customerId: number
  customerName: string
  orderDate: string
  deliveryDate: string
  totalAmount: number
  status: 'draft' | 'submitted' | 'approved' | 'closed'
}

interface SearchForm {
  orderNo: string
  customerId: number | null
  status: string[]
  dateRange: [string, string] | null
}

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  draft: '草稿',
  submitted: '已提交',
  approved: '已审核',
  closed: '已关闭',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
    closed: 'default',
  }
  return map[s] ?? 'default'
}

// ==================== 客户选项 ====================
const customerOptions = ref<{ label: string; value: number }[]>([])

async function fetchCustomerOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>('/base/customers', 1, 1000)
    customerOptions.value = result.list.map((item) => ({ label: item.name, value: item.id }))
  } catch (e: any) {
    // options load silently; the dropdown just stays empty
  }
}

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  orderNo: '',
  customerId: null,
  status: [],
  dateRange: null,
})

// ==================== 表格 & 分页 ====================
const loading = ref(false)
const tableData = ref<SalesOrder[]>([])
const selectedIds = ref<number[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

function handleSelectionChange(rows: SalesOrder[]) {
  selectedIds.value = rows.map((r) => r.id)
}

async function fetchData() {
  loading.value = true
  try {
    const query: Record<string, string | number | undefined> = {}
    if (searchForm.orderNo) query.orderNo = searchForm.orderNo
    if (searchForm.customerId) query.customerId = searchForm.customerId
    if (searchForm.status.length > 0) query.status = searchForm.status.join(',')
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      query.startDate = searchForm.dateRange[0]
      query.endDate = searchForm.dateRange[1]
    }

    const result = await api.page<SalesOrder>(
      '/sales/orders',
      pagination.page,
      pagination.pageSize,
      query,
    )
    tableData.value = result.list
    pagination.total = result.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.orderNo = ''
  searchForm.customerId = null
  searchForm.status = []
  searchForm.dateRange = null
  handleSearch()
}

// ==================== 行操作 ====================
const router = useRouter()

function handleView(row: SalesOrder) {
  router.push(`/sales/orders/${row.id}`)
}

function handleEdit(row: SalesOrder) {
  router.push(`/sales/orders/create?id=${row.id}`)
}

async function handleDelete(row: SalesOrder) {
  try {
    await api.del(`/sales/orders/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function handleSubmit(row: SalesOrder) {
  try {
    await api.post(`/sales/orders/${row.id}/submit`)
    ElMessage.success('提交成功')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  }
}

async function handleApprove(row: SalesOrder) {
  try {
    await api.post(`/sales/orders/${row.id}/approve`)
    ElMessage.success('审核通过')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

async function handleUnapprove(row: SalesOrder) {
  try {
    await api.post(`/sales/orders/${row.id}/unapprove`)
    ElMessage.success('已反审核')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '反审核失败')
  }
}

function handleCreateProductionOrder(row: SalesOrder) {
  router.push(`/production/orders/create?fromSalesOrder=${row.id}`)
}

function handleCreateDelivery(row: SalesOrder) {
  router.push(`/sales/deliveries/create?fromOrder=${row.id}`)
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的订单')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedIds.value.length} 条订单？`,
      '批量删除',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
    await Promise.all(selectedIds.value.map((id) => api.del(`/sales/orders/${id}`)))
    selectedIds.value = []
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '批量删除失败')
    }
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchCustomerOptions()
  fetchData()
})
</script>

<style scoped>
.sales-orders-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-bar-left {
  display: flex;
  gap: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
