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
const customerOptions = ref([
  { label: '华为技术有限公司', value: 1 },
  { label: '中兴通讯股份有限公司', value: 2 },
  { label: '比亚迪股份有限公司', value: 3 },
  { label: '富士康科技集团', value: 4 },
])

// ==================== Mock 数据 ====================
function generateMockData(): Delivery[] {
  const customers = customerOptions.value
  const data: Delivery[] = []
  for (let i = 1; i <= 25; i++) {
    const customer = customers[i % customers.length]
    data.push({
      id: i,
      deliveryNo: `DLV-${String(i).padStart(5, '0')}`,
      orderId: i,
      orderNo: `SO-${String(i).padStart(5, '0')}`,
      customerId: customer.value,
      customerName: customer.label,
      deliveryDate: `2025-${String((i % 12) + 1).padStart(2, '0')}-${String((i % 28) + 1).padStart(2, '0')}`,
      status: i % 3 === 0 ? 'draft' : 'shipped',
    })
  }
  return data
}

const allData = ref<Delivery[]>(generateMockData())

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

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...allData.value]

    if (searchForm.deliveryNo) {
      filtered = filtered.filter((d) =>
        d.deliveryNo.toLowerCase().includes(searchForm.deliveryNo.toLowerCase()),
      )
    }
    if (searchForm.customerId) {
      filtered = filtered.filter((d) => d.customerId === searchForm.customerId)
    }
    if (searchForm.status) {
      filtered = filtered.filter((d) => d.status === searchForm.status)
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const [start, end] = searchForm.dateRange
      filtered = filtered.filter(
        (d) => d.deliveryDate >= start && d.deliveryDate <= end,
      )
    }

    pagination.total = filtered.length
    const start = (pagination.page - 1) * pagination.pageSize
    tableData.value = filtered.slice(start, start + pagination.pageSize)
    loading.value = false
  }, 300)
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

// ==================== 行操作 ====================
function handleConfirm(row: Delivery) {
  const target = allData.value.find((d) => d.id === row.id)
  if (target) {
    target.status = 'shipped'
  }
  ElMessage.success('确认发货成功')
  fetchData()
}

function handleDelete(row: Delivery) {
  const idx = allData.value.findIndex((d) => d.id === row.id)
  if (idx > -1) {
    allData.value.splice(idx, 1)
  }
  ElMessage.success('删除成功')
  fetchData()
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
