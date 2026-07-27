<template>
  <div class="deliveries-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="发货单号">
          <el-input
            v-model="searchForm.deliveryNo"
            placeholder="请输入发货单号"
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
            clearable
            style="width: 140px"
          >
            <el-option label="草稿" value="draft" />
            <el-option label="已发货" value="shipped" />
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
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <div class="action-bar">
        <el-button
          type="primary"
          :icon="Plus"
          @click="router.push('/sales/deliveries/create')"
        >
          新增发货单
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
      >
        <el-table-column prop="deliveryNo" label="发货单号" width="160" />
        <el-table-column prop="orderNo" label="销售订单号" width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/sales/orders/${row.orderId}`)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" width="180" />
        <el-table-column prop="deliveryDate" label="发货日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
              {{ row.status === 'draft' ? '草稿' : '已发货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="router.push(`/sales/deliveries/create?id=${row.id}`)"
            >
              查看
            </el-button>
            <template v-if="row.status === 'draft'">
              <el-popconfirm
                title="确认发货？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleConfirm(row)"
              >
                <template #reference>
                  <el-button type="success" size="small" link>确认发货</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认删除？"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button type="danger" size="small" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>

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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()

// ==================== 类型 ====================
interface Delivery {
  id: number
  deliveryNo: string
  orderId: number
  orderNo: string
  customerId: number
  customerName: string
  deliveryDate: string
  status: 'draft' | 'shipped'
}

interface SearchForm {
  deliveryNo: string
  customerId: number | null
  status: string
  dateRange: [string, string] | null
}

// ==================== 客户选项 ====================
const customerOptions = ref<{ label: string; value: number }[]>([])

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  deliveryNo: '',
  customerId: null,
  status: '',
  dateRange: null,
})

const loading = ref(false)
const tableData = ref<Delivery[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

async function fetchData() {
  loading.value = true
  try {
    const query: Record<string, string | number | undefined> = {}
    if (searchForm.deliveryNo) query.deliveryNo = searchForm.deliveryNo
    if (searchForm.customerId) query.customerId = searchForm.customerId
    if (searchForm.status) query.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      query.startDate = searchForm.dateRange[0]
      query.endDate = searchForm.dateRange[1]
    }

    const result = await api.page<Delivery>(
      '/sales/deliveries',
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
  searchForm.deliveryNo = ''
  searchForm.customerId = null
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

async function handleConfirm(row: Delivery) {
  try {
    await api.post(`/sales/deliveries/${row.id}/confirm`)
    ElMessage.success('确认发货成功')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '确认发货失败')
  }
}

async function handleDelete(row: Delivery) {
  try {
    await api.del(`/sales/deliveries/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.deliveries-page {
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
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
