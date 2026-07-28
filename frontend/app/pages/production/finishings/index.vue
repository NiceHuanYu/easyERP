<template>
  <div class="finishings-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="入库单号">
          <el-input
            v-model="searchForm.finishingNo"
            placeholder="请输入入库单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="工单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入工单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已入库" value="CONFIRMED" />
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
          @click="router.push('/production/finishings/create')"
        >
          新增入库单
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
      >
        <el-table-column prop="finishingNo" label="入库单号" width="160" />
        <el-table-column label="工单号" width="160">
          <template #default="{ row }">
            <el-link
              type="primary"
              @click="router.push(`/production/orders/${row.orderId}`)"
            >
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="物料" min-width="180" />
        <el-table-column prop="quantity" label="入库数量" width="120" align="right" />
        <el-table-column prop="finishingDate" label="入库日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DRAFT' ? 'info' : 'success'" size="small">
              {{ row.status === 'DRAFT' ? '草稿' : '已入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="router.push(`/production/finishings/create?id=${row.id}`)"
            >
              查看
            </el-button>
            <template v-if="row.status === 'DRAFT'">
              <el-popconfirm
                title="确认入库？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleConfirm(row)"
              >
                <template #reference>
                  <el-button type="success" size="small" link>确认入库</el-button>
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
interface Finishing {
  id: number
  finishingNo: string
  orderId: number
  orderNo: string
  materialName: string
  quantity: number
  finishingDate: string
  status: 'DRAFT' | 'CONFIRMED'
}

interface SearchForm {
  finishingNo: string
  orderNo: string
  status: string
  dateRange: [string, string] | null
}

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  finishingNo: '',
  orderNo: '',
  status: '',
  dateRange: null,
})

const loading = ref(false)
const tableData = ref<Finishing[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

function buildSearchQuery(): Record<string, string | number | undefined> {
  const q: Record<string, string | number | undefined> = {}
  if (searchForm.finishingNo) q.finishingNo = searchForm.finishingNo
  if (searchForm.orderNo) q.orderNo = searchForm.orderNo
  if (searchForm.status) q.status = searchForm.status
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    q.startDate = searchForm.dateRange[0]
    q.endDate = searchForm.dateRange[1]
  }
  return q
}

async function fetchData() {
  loading.value = true
  try {
    const result = await api.page<Finishing>(
      '/production/finishings',
      pagination.page,
      pagination.pageSize,
      buildSearchQuery(),
    )
    tableData.value = result.list
    pagination.total = result.total
  } catch {
    ElMessage.error('加载入库单列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.finishingNo = ''
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ==================== 行操作 ====================
async function handleConfirm(row: Finishing) {
  try {
    await api.post(`/production/finishings/confirm/${row.id}`)
    ElMessage.success('确认入库成功')
    fetchData()
  } catch {
    ElMessage.error('确认入库失败')
  }
}

async function handleDelete(row: Finishing) {
  try {
    await api.del(`/production/finishings/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.finishings-page {
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
