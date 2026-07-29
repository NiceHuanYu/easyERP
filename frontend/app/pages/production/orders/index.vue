<template>
  <div class="production-orders-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="工单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入工单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="销售订单号">
          <el-input
            v-model="searchForm.salesOrderNo"
            placeholder="请输入销售订单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="物料">
          <el-select
            v-model="searchForm.materialId"
            placeholder="请选择物料"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="m in materialOptions"
              :key="m.value"
              :label="m.label"
              :value="m.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 160px"
          >
            <el-option label="待排产" value="DRAFT" />
            <el-option label="已下达" value="RELEASED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
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
      <div class="action-bar">
        <div class="action-bar-left">
          <el-button
            v-permission="'production:order:create'"
            type="primary"
            :icon="Plus"
            @click="navigateTo('/production/orders/create')"
          >
            新增工单
          </el-button>
          <el-button
            v-permission="'production:order:delete'"
            type="danger"
            :icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 12px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="orderNo" label="工单号" width="160" />
        <el-table-column prop="salesOrderNo" label="销售订单号" width="160">
          <template #default="{ row }">
            <el-link
              v-if="row.salesOrderId"
              type="primary"
              @click="navigateTo(`/sales/orders/${row.salesOrderId}`)"
            >
              {{ row.salesOrderNo }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="planQuantity" label="计划数量" width="110" align="right" />
        <el-table-column prop="finishQuantity" label="已完工数量" width="120" align="right" />
        <el-table-column prop="startDate" label="计划开始" width="120" />
        <el-table-column prop="endDate" label="计划结束" width="120" />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 待排产 -->
            <template v-if="row.status === 'DRAFT'">
              <el-button
                v-permission="'production:order:update'"
                type="primary"
                size="small"
                link
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-popconfirm
                title="确认删除该工单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'production:order:delete'"
                    type="danger"
                    size="small"
                    link
                  >
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                title="确认下达该工单？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleRelease(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'production:order:release'"
                    type="warning"
                    size="small"
                    link
                  >
                    下达
                  </el-button>
                </template>
              </el-popconfirm>
            </template>

            <!-- 已下达 -->
            <template v-else-if="row.status === 'RELEASED'">
              <el-button type="primary" size="small" link @click="handleView(row)">
                查看
              </el-button>
              <el-button
                type="success"
                size="small"
                link
                @click="handleCreatePicking(row)"
              >
                创建领料单
              </el-button>
              <el-popconfirm
                title="确认完工入库？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleFinish(row)"
              >
                <template #reference>
                  <el-button
                    v-permission="'production:order:finish'"
                    type="warning"
                    size="small"
                    link
                  >
                    完工入库
                  </el-button>
                </template>
              </el-popconfirm>
            </template>

            <!-- 已完成 -->
            <template v-else-if="row.status === 'COMPLETED'">
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
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const router = useRouter()

// ==================== 类型定义 ====================
interface ProductionOrder {
  id: number
  orderNo: string
  salesOrderId: number | null
  salesOrderNo: string
  materialId: number
  materialName: string
  planQuantity: number
  finishQuantity: number
  startDate: string
  endDate: string
  status: string
}

interface SearchForm {
  orderNo: string
  salesOrderNo: string
  materialId: number | null
  status: string
}

// ==================== 状态工具 ====================
const statusMap: Record<string, string> = {
  DRAFT: '待排产',
  RELEASED: '已下达',
  COMPLETED: '已完成',
}

function statusLabel(s: string): string {
  return statusMap[s] ?? s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | '' {
  const map: Record<string, 'info' | 'warning' | 'success' | ''> = {
    DRAFT: 'info',
    RELEASED: 'warning',
    COMPLETED: 'success',
  }
  return map[s] ?? ''
}

// ==================== 物料选项 ====================
const materialOptions = ref<{ label: string; value: number }[]>([])

async function loadMaterialOptions() {
  try {
    const data = await api.page<{ id: number; name: string; spec: string }>('/base/materials', 1, 1000)
    materialOptions.value = data.list.map((m) => ({
      label: m.spec ? `${m.name} (${m.spec})` : m.name,
      value: m.id,
    }))
  } catch {
    // options load silently
  }
}

// ==================== 搜索 ====================
const searchForm = reactive<SearchForm>({
  orderNo: '',
  salesOrderNo: '',
  materialId: null,
  status: '',
})

// ==================== 表格 & 分页 ====================
const loading = ref(false)
const tableData = ref<ProductionOrder[]>([])
const selectedIds = ref<number[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

function handleSelectionChange(rows: ProductionOrder[]) {
  selectedIds.value = rows.map((r) => r.id)
}

function buildSearchQuery(): Record<string, string | number | undefined> {
  const q: Record<string, string | number | undefined> = {}
  if (searchForm.orderNo) q.orderNo = searchForm.orderNo
  if (searchForm.salesOrderNo) q.salesOrderNo = searchForm.salesOrderNo
  if (searchForm.materialId) q.materialId = searchForm.materialId
  if (searchForm.status) q.status = searchForm.status
  return q
}

async function fetchData() {
  loading.value = true
  try {
    const result = await api.page<ProductionOrder>(
      '/production/orders',
      pagination.page,
      pagination.pageSize,
      buildSearchQuery(),
    )
    tableData.value = result.list
    pagination.total = result.total
  } catch {
    ElMessage.error('加载工单列表失败')
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
  searchForm.salesOrderNo = ''
  searchForm.materialId = null
  searchForm.status = ''
  handleSearch()
}

watch([() => pagination.page, () => pagination.pageSize], () => {
  fetchData()
})

// ==================== 行操作 ====================
function handleView(row: ProductionOrder) {
  router.push(`/production/orders/${row.id}`)
}

function handleEdit(row: ProductionOrder) {
  router.push(`/production/orders/create?id=${row.id}`)
}

async function handleDelete(row: ProductionOrder) {
  try {
    await api.del(`/production/orders/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleRelease(row: ProductionOrder) {
  try {
    await api.post(`/production/orders/release/${row.id}`)
    ElMessage.success('工单已下达')
    fetchData()
  } catch {
    ElMessage.error('下达失败')
  }
}

async function handleFinish(row: ProductionOrder) {
  try {
    await api.post(`/production/orders/${row.id}/finish`)
    ElMessage.success('已提交完工入库')
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleCreatePicking(row: ProductionOrder) {
  router.push(`/production/pickings/create?fromOrder=${row.id}`)
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的工单')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedIds.value.length} 条工单？`,
      '批量删除',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
    await Promise.all(selectedIds.value.map((id) => api.del(`/production/orders/${id}`)))
    selectedIds.value = []
    ElMessage.success('批量删除成功')
    fetchData()
  } catch {
    // cancelled or error — no-op
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadMaterialOptions()
  fetchData()
})
</script>

<style scoped>
.production-orders-page {
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
