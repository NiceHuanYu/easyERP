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
            style="min-width: 200px"
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
            style="min-width: 140px"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已收货" value="CONFIRMED" />
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
          v-permission="'purchase:order:create'"
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
        <el-table-column prop="receivingNo" label="收货单号" min-width="160" />
        <el-table-column prop="orderNo" label="采购单号" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/purchase/orders/create?id=${row.orderId}`)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" min-width="200" />
        <el-table-column prop="receivingDate" label="收货日期" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DRAFT' ? 'info' : 'success'" size="small">
              {{ row.status === 'DRAFT' ? '草稿' : '已收货' }}
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
            <template v-if="row.status === 'DRAFT'">
              <el-popconfirm
                title="确认收货？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleConfirm(row)"
              >
                <template #reference>
                  <el-button
                    type="success"
                    size="small"
                    link
                    v-permission="'purchase:order:approve'"
                  >确认收货</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认删除该收货单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    type="danger"
                    size="small"
                    link
                    v-permission="'purchase:order:create'"
                  >删除</el-button>
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

// ==================== 类型定义 ====================
interface Receiving {
  id: number
  receivingNo: string
  orderId: number
  orderNo: string
  supplierId: number
  supplierName: string
  receivingDate: string
  status: string
}

interface SearchForm {
  receivingNo: string
  orderNo: string
  supplierId: number | null
  status: string
  dateRange: [string, string] | null
}

// ==================== 选项（从 API 加载） ====================
const supplierOptions = ref<{ label: string; value: number }[]>([])

async function fetchSupplierOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>(
      '/base/suppliers', 1, 1000,
    )
    supplierOptions.value = result.list.map((s) => ({ label: s.name, value: s.id }))
  } catch { /* ignore */ }
}

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

async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, string | number | undefined> = {}
    if (searchForm.receivingNo) extraQuery.receivingNo = searchForm.receivingNo
    if (searchForm.orderNo) extraQuery.orderNo = searchForm.orderNo
    if (searchForm.supplierId) extraQuery.supplierId = searchForm.supplierId
    if (searchForm.status) extraQuery.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<Receiving>(
      '/purchase/receivings',
      pagination.page,
      pagination.pageSize,
      extraQuery,
    )
    tableData.value = result.list
    pagination.total = result.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
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

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ==================== 行操作 ====================
function handleView(row: Receiving) {
  router.push(`/purchase/receivings/create?id=${row.id}`)
}

async function handleConfirm(row: Receiving) {
  try {
    await api.post(`/purchase/receivings/confirm/${row.id}`)
    ElMessage.success('确认收货成功')
    fetchData()
  } catch {
    ElMessage.error('确认收货失败')
  }
}

async function handleDelete(row: Receiving) {
  try {
    await api.del(`/purchase/receivings/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
  fetchSupplierOptions()
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
