<template>
  <div class="requisitions-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="申请单号">
          <el-input
            v-model="searchForm.reqNo"
            placeholder="请输入申请单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="申请人">
          <el-select
            v-model="searchForm.applicantId"
            placeholder="请选择申请人"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="e in employeeOptions"
              :key="e.value"
              :label="e.label"
              :value="e.value"
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
            <el-option label="已提交" value="submitted" />
            <el-option label="已审核" value="approved" />
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
          v-permission="'purchase:requisition:create'"
          @click="navigateTo('/purchase/requisitions/create')"
        >
          新增申请
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
      >
        <el-table-column prop="reqNo" label="申请单号" width="160" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="reqDate" label="申请日期" width="120" />
        <el-table-column label="物料" min-width="200">
          <template #default="{ row }">
            {{ row.materialSummary }}
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="数量" width="100" align="right" />
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
                type="primary"
                size="small"
                link
                v-permission="'purchase:requisition:create'"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-popconfirm
                title="确认删除该申请？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    type="danger"
                    size="small"
                    link
                    v-permission="'purchase:requisition:create'"
                  >删除</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认提交该申请？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleSubmit(row)"
              >
                <template #reference>
                  <el-button
                    type="warning"
                    size="small"
                    link
                    v-permission="'purchase:requisition:create'"
                  >提交</el-button>
                </template>
              </el-popconfirm>
            </template>

            <template v-else-if="row.status === 'submitted'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
              <el-popconfirm
                title="确认审核通过该申请？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleApprove(row)"
              >
                <template #reference>
                  <el-button
                    type="success"
                    size="small"
                    link
                    v-permission="'purchase:requisition:create'"
                  >审核</el-button>
                </template>
              </el-popconfirm>
            </template>

            <template v-else-if="row.status === 'approved'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
              <el-button
                type="success"
                size="small"
                link
                v-permission="'purchase:requisition:create'"
                @click="handleCreateOrder(row)"
              >
                生成采购订单
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
interface Requisition {
  id: number
  reqNo: string
  applicantId: number
  applicantName: string
  reqDate: string
  materialSummary: string
  totalQuantity: number
  status: 'draft' | 'submitted' | 'approved'
}

interface SearchForm {
  reqNo: string
  applicantId: number | null
  status: string
  dateRange: [string, string] | null
}

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  draft: '草稿',
  submitted: '已提交',
  approved: '已审核',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
  }
  return map[s] ?? 'default'
}

// ==================== 选项（从 API 加载） ====================
const employeeOptions = ref<{ label: string; value: number }[]>([])

async function fetchEmployeeOptions() {
  try {
    const result = await api.page<{ id: number; name: string }>(
      '/base/employees', 1, 1000,
    )
    employeeOptions.value = result.list.map((e) => ({ label: e.name, value: e.id }))
  } catch { /* ignore */ }
}

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  reqNo: '',
  applicantId: null,
  status: '',
  dateRange: null,
})

const loading = ref(false)
const tableData = ref<Requisition[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

async function fetchData() {
  loading.value = true
  try {
    const extraQuery: Record<string, string | number | undefined> = {}
    if (searchForm.reqNo) extraQuery.reqNo = searchForm.reqNo
    if (searchForm.applicantId) extraQuery.applicantId = searchForm.applicantId
    if (searchForm.status) extraQuery.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      extraQuery.startDate = searchForm.dateRange[0]
      extraQuery.endDate = searchForm.dateRange[1]
    }
    const result = await api.page<Requisition>(
      '/purchase/requisitions',
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
  searchForm.reqNo = ''
  searchForm.applicantId = null
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ==================== 行操作 ====================
function handleView(row: Requisition) {
  router.push(`/purchase/requisitions/create?id=${row.id}`)
}

function handleEdit(row: Requisition) {
  router.push(`/purchase/requisitions/create?id=${row.id}`)
}

async function handleDelete(row: Requisition) {
  try {
    await api.del(`/purchase/requisitions/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleSubmit(row: Requisition) {
  try {
    await api.put(`/purchase/requisitions/${row.id}`, { status: 'submitted' })
    ElMessage.success('提交成功')
    fetchData()
  } catch {
    ElMessage.error('提交失败')
  }
}

async function handleApprove(row: Requisition) {
  try {
    await api.put(`/purchase/requisitions/${row.id}`, { status: 'approved' })
    ElMessage.success('审核通过')
    fetchData()
  } catch {
    ElMessage.error('审核失败')
  }
}

async function handleCreateOrder(row: Requisition) {
  try {
    await api.post(`/purchase/requisitions/create-order/${row.id}`)
    ElMessage.success('已生成采购订单')
    router.push(`/purchase/orders/create?fromRequisition=${row.id}`)
  } catch {
    ElMessage.error('生成采购订单失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
  fetchEmployeeOptions()
})
</script>

<style scoped>
.requisitions-page {
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
