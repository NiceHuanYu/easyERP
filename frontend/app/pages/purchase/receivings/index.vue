<template>
  <div class="receivings-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="收货单号">
          <el-input
            v-model="searchForm.receivingNo"
            placeholder="请输入收货单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="采购单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入采购单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select
            v-model="searchForm.supplierId"
            placeholder="请选择供应商"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="s in supplierOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
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
            <el-option label="已收货" value="received" />
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

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <div class="action-bar">
        <el-button
          type="primary"
          :icon="Plus"
          @click="navigateTo('/purchase/receivings/create')"
        >
          新增收货单
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
      >
        <el-table-column prop="receivingNo" label="收货单号" width="160" />
        <el-table-column prop="orderNo" label="采购单号" width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/purchase/orders/create?id=${row.orderId}`)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="200" />
        <el-table-column prop="receivingDate" label="收货日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
              {{ row.status === 'draft' ? '草稿' : '已收货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <template v-if="row.status === 'draft'">
              <el-popconfirm
                title="确认收货？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleConfirm(row)"
              >
                <template #reference>
                  <el-button type="success" size="small" link>确认收货</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认删除该收货单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

definePageMeta({ middleware: 'auth' })

const router = useRouter()

// ==================== 类型定义 ====================
interface Receiving {
  id: number
  receivingNo: string
  orderId: number
  orderNo: string
  supplierId: number
  supplierName: string
  receivingDate: string
  status: 'draft' | 'received'
}

interface SearchForm {
  receivingNo: string
  orderNo: string
  supplierId: number | null
  status: string
  dateRange: [string, string] | null
}

// ==================== 选项 ====================
const supplierOptions = ref([
  { label: '深圳华强电子有限公司', value: 1 },
  { label: '广州万国元件有限公司', value: 2 },
  { label: '东莞正泰科技有限公司', value: 3 },
  { label: '上海锐拓半导体有限公司', value: 4 },
])

// ==================== Mock 数据 ====================
function generateMockData(): Receiving[] {
  const suppliers = supplierOptions.value
  const data: Receiving[] = []
  for (let i = 1; i <= 22; i++) {
    const supplier = suppliers[i % suppliers.length]
    data.push({
      id: i,
      receivingNo: `RCV-${String(i).padStart(5, '0')}`,
      orderId: i,
      orderNo: `PO-${String(i).padStart(5, '0')}`,
      supplierId: supplier.value,
      supplierName: supplier.label,
      receivingDate: `2025-${String((i % 12) + 1).padStart(2, '0')}-${String((i % 28) + 1).padStart(2, '0')}`,
      status: i % 3 === 0 ? 'draft' : 'received',
    })
  }
  return data
}

const allData = ref<Receiving[]>(generateMockData())

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  receivingNo: '',
  orderNo: '',
  supplierId: null,
  status: '',
  dateRange: null,
})

const loading = ref(false)
const tableData = ref<Receiving[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...allData.value]

    if (searchForm.receivingNo) {
      filtered = filtered.filter((r) =>
        r.receivingNo.toLowerCase().includes(searchForm.receivingNo.toLowerCase()),
      )
    }
    if (searchForm.orderNo) {
      filtered = filtered.filter((r) =>
        r.orderNo.toLowerCase().includes(searchForm.orderNo.toLowerCase()),
      )
    }
    if (searchForm.supplierId) {
      filtered = filtered.filter((r) => r.supplierId === searchForm.supplierId)
    }
    if (searchForm.status) {
      filtered = filtered.filter((r) => r.status === searchForm.status)
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const [start, end] = searchForm.dateRange
      filtered = filtered.filter(
        (r) => r.receivingDate >= start && r.receivingDate <= end,
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
  searchForm.receivingNo = ''
  searchForm.orderNo = ''
  searchForm.supplierId = null
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

// ==================== 行操作 ====================
function handleView(row: Receiving) {
  router.push(`/purchase/receivings/create?id=${row.id}`)
}

function handleConfirm(row: Receiving) {
  const target = allData.value.find((r) => r.id === row.id)
  if (target) {
    target.status = 'received'
  }
  ElMessage.success('确认收货成功')
  fetchData()
}

function handleDelete(row: Receiving) {
  const idx = allData.value.findIndex((r) => r.id === row.id)
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
.receivings-page {
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
