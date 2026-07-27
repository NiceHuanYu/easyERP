<template>
  <div class="pickings-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="领料单号">
          <el-input
            v-model="searchForm.pickingNo"
            placeholder="请输入领料单号"
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
            <el-option label="草稿" value="draft" />
            <el-option label="已领料" value="picked" />
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
          @click="router.push('/production/pickings/create')"
        >
          新增领料单
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
      >
        <el-table-column prop="pickingNo" label="领料单号" width="160" />
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
        <el-table-column prop="materialSummary" label="领料物料" min-width="200" />
        <el-table-column prop="pickingDate" label="领料日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'draft' ? 'info' : 'success'" size="small">
              {{ row.status === 'draft' ? '草稿' : '已领料' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="router.push(`/production/pickings/create?id=${row.id}`)"
            >
              查看
            </el-button>
            <template v-if="row.status === 'draft'">
              <el-popconfirm
                title="确认领料？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleConfirm(row)"
              >
                <template #reference>
                  <el-button type="success" size="small" link>确认领料</el-button>
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

// ==================== 类型定义 ====================
interface Picking {
  id: number
  pickingNo: string
  orderId: number
  orderNo: string
  materialSummary: string
  pickingDate: string
  status: 'draft' | 'picked'
}

interface SearchForm {
  pickingNo: string
  orderNo: string
  status: string
  dateRange: [string, string] | null
}

// ==================== Mock 数据 ====================
const materialNames = ['电阻 10KΩ', '电容 100μF', 'PCB 裸板', '锡膏', '散热片', 'LCD 面板', '背光模组', '排线 FPC']

function generateMockData(): Picking[] {
  const data: Picking[] = []
  for (let i = 1; i <= 25; i++) {
    const matCount = (i % 3) + 1
    const summary = matCount === 1
      ? materialNames[i % materialNames.length]
      : `${materialNames[i % materialNames.length]} 等${matCount}项`
    data.push({
      id: i,
      pickingNo: `PK-${String(i).padStart(5, '0')}`,
      orderId: i,
      orderNo: `MO-${String(i).padStart(5, '0')}`,
      materialSummary: summary,
      pickingDate: `2025-${String((i % 12) + 1).padStart(2, '0')}-${String((i % 28) + 1).padStart(2, '0')}`,
      status: i % 3 === 0 ? 'draft' : 'picked',
    })
  }
  return data
}

const allData = ref<Picking[]>(generateMockData())

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  pickingNo: '',
  orderNo: '',
  status: '',
  dateRange: null,
})

const loading = ref(false)
const tableData = ref<Picking[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...allData.value]

    if (searchForm.pickingNo) {
      filtered = filtered.filter((p) =>
        p.pickingNo.toLowerCase().includes(searchForm.pickingNo.toLowerCase()),
      )
    }
    if (searchForm.orderNo) {
      filtered = filtered.filter((p) =>
        p.orderNo.toLowerCase().includes(searchForm.orderNo.toLowerCase()),
      )
    }
    if (searchForm.status) {
      filtered = filtered.filter((p) => p.status === searchForm.status)
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const [start, end] = searchForm.dateRange
      filtered = filtered.filter(
        (p) => p.pickingDate >= start && p.pickingDate <= end,
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
  searchForm.pickingNo = ''
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

// ==================== 行操作 ====================
function handleConfirm(row: Picking) {
  const target = allData.value.find((p) => p.id === row.id)
  if (target) {
    target.status = 'picked'
  }
  ElMessage.success('确认领料成功')
  fetchData()
}

function handleDelete(row: Picking) {
  const idx = allData.value.findIndex((p) => p.id === row.id)
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
.pickings-page {
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
