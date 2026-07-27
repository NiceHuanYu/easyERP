<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.code" placeholder="请输入角色编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" v-permission="'system:role:create'" @click="handleAdd">
          新增角色
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="角色名称" width="140" />
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'system:role:edit'" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'system:role:delete'" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button link type="warning" @click="handleAssignPermission(row)">
              分配权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="table-pagination"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-tree
        ref="treeRef"
        :data="permTreeData"
        :props="{ children: 'children', label: 'label' }"
        node-key="id"
        show-checkbox
        default-expand-all
        :default-checked-keys="checkedPermKeys"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handlePermSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ middleware: 'auth' })

// --------------- 类型 ---------------
interface Role {
  id: number
  name: string
  code: string
  description: string
  userCount: number
  permissions: string[]
  createdAt: string
}

interface PermTreeNode {
  id: string
  label: string
  children?: PermTreeNode[]
}

// --------------- 权限树数据 ---------------
const permTreeData: PermTreeNode[] = [
  {
    id: 'sales',
    label: '销售管理',
    children: [
      { id: 'sales:order:view', label: '查看订单' },
      { id: 'sales:order:create', label: '创建订单' },
      { id: 'sales:order:edit', label: '编辑订单' },
      { id: 'sales:order:delete', label: '删除订单' },
      { id: 'sales:order:submit', label: '提交订单' },
      { id: 'sales:order:approve', label: '审批订单' },
    ],
  },
  {
    id: 'delivery',
    label: '发货管理',
    children: [
      { id: 'delivery:order:view', label: '查看发货单' },
      { id: 'delivery:order:create', label: '创建发货单' },
      { id: 'delivery:order:edit', label: '编辑发货单' },
      { id: 'delivery:order:delete', label: '删除发货单' },
      { id: 'delivery:order:submit', label: '提交发货单' },
      { id: 'delivery:order:approve', label: '审批发货单' },
    ],
  },
  {
    id: 'production',
    label: '生产管理',
    children: [
      { id: 'production:order:view', label: '查看生产单' },
      { id: 'production:order:create', label: '创建生产单' },
      { id: 'production:order:edit', label: '编辑生产单' },
      { id: 'production:order:delete', label: '删除生产单' },
      { id: 'production:order:submit', label: '提交生产单' },
      { id: 'production:order:approve', label: '审批生产单' },
    ],
  },
  {
    id: 'purchase',
    label: '采购管理',
    children: [
      { id: 'purchase:order:view', label: '查看采购单' },
      { id: 'purchase:order:create', label: '创建采购单' },
      { id: 'purchase:order:edit', label: '编辑采购单' },
      { id: 'purchase:order:delete', label: '删除采购单' },
      { id: 'purchase:order:submit', label: '提交采购单' },
      { id: 'purchase:order:approve', label: '审批采购单' },
    ],
  },
  {
    id: 'inventory',
    label: '库存管理',
    children: [
      { id: 'inventory:stock:view', label: '查看库存' },
      { id: 'inventory:stock:create', label: '入库' },
      { id: 'inventory:stock:edit', label: '库存调整' },
      { id: 'inventory:stock:delete', label: '出库' },
      { id: 'inventory:stock:submit', label: '提交盘点' },
      { id: 'inventory:stock:approve', label: '审批盘点' },
    ],
  },
  {
    id: 'finance',
    label: '财务管理',
    children: [
      { id: 'finance:order:view', label: '查看财务单据' },
      { id: 'finance:order:create', label: '创建财务单据' },
      { id: 'finance:order:edit', label: '编辑财务单据' },
      { id: 'finance:order:delete', label: '删除财务单据' },
      { id: 'finance:order:submit', label: '提交财务单据' },
      { id: 'finance:order:approve', label: '审批财务单据' },
    ],
  },
  {
    id: 'system',
    label: '系统管理',
    children: [
      { id: 'system:user:view', label: '查看用户' },
      { id: 'system:user:create', label: '创建用户' },
      { id: 'system:user:edit', label: '编辑用户' },
      { id: 'system:user:delete', label: '删除用户' },
      { id: 'system:role:view', label: '查看角色' },
      { id: 'system:role:create', label: '创建角色' },
      { id: 'system:role:edit', label: '编辑角色' },
      { id: 'system:role:delete', label: '删除角色' },
      { id: 'system:dict:view', label: '查看字典' },
      { id: 'system:dict:create', label: '创建字典' },
      { id: 'system:dict:edit', label: '编辑字典' },
      { id: 'system:dict:delete', label: '删除字典' },
    ],
  },
]

// --------------- Mock 数据 ---------------
const mockRoles: Role[] = [
  { id: 1, name: '系统管理员', code: 'admin', description: '拥有系统全部权限', userCount: 2, permissions: permTreeData.flatMap((g) => (g.children ?? []).map((c) => c.id)), createdAt: '2024-01-15 09:00:00' },
  { id: 2, name: '销售经理', code: 'sales_manager', description: '管理销售订单的创建与审批', userCount: 5, permissions: ['sales:order:view', 'sales:order:create', 'sales:order:edit', 'sales:order:submit', 'sales:order:approve', 'delivery:order:view'], createdAt: '2024-01-15 10:30:00' },
  { id: 3, name: '生产主管', code: 'production_supervisor', description: '管理生产计划与执行', userCount: 3, permissions: ['production:order:view', 'production:order:create', 'production:order:edit', 'production:order:submit', 'production:order:approve', 'inventory:stock:view'], createdAt: '2024-02-10 08:15:00' },
  { id: 4, name: '采购员', code: 'purchaser', description: '负责采购下单与跟踪', userCount: 4, permissions: ['purchase:order:view', 'purchase:order:create', 'purchase:order:edit', 'purchase:order:submit', 'inventory:stock:view'], createdAt: '2024-02-20 14:00:00' },
  { id: 5, name: '财务', code: 'finance', description: '财务审核与对账', userCount: 3, permissions: ['finance:order:view', 'finance:order:create', 'finance:order:edit', 'finance:order:submit', 'finance:order:approve'], createdAt: '2024-03-05 11:20:00' },
  { id: 6, name: '仓管员', code: 'warehouse_keeper', description: '仓库出入库管理', userCount: 6, permissions: ['inventory:stock:view', 'inventory:stock:create', 'inventory:stock:edit', 'inventory:stock:delete', 'inventory:stock:submit'], createdAt: '2024-03-10 16:45:00' },
  { id: 7, name: '普通用户', code: 'user', description: '基础查看权限', userCount: 18, permissions: ['sales:order:view', 'delivery:order:view', 'production:order:view', 'purchase:order:view', 'inventory:stock:view', 'finance:order:view'], createdAt: '2024-01-01 00:00:00' },
]

// --------------- 状态 ---------------
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const permDialogVisible = ref(false)
const permSubmitLoading = ref(false)
const editingRoleForPerm = ref<Role | null>(null)
const checkedPermKeys = ref<string[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()

const searchForm = reactive({
  name: '',
  code: '',
})

const form = reactive({
  name: '',
  code: '',
  description: '',
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref<Role[]>([])

const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }],
}

// --------------- 计算 ---------------
const dialogTitle = computed(() => (isEdit.value ? '编辑角色' : '新增角色'))

// --------------- 方法 ---------------
function fetchData() {
  loading.value = true
  setTimeout(() => {
    let list = [...mockRoles]

    if (searchForm.name) {
      list = list.filter((r) => r.name.includes(searchForm.name))
    }
    if (searchForm.code) {
      list = list.filter((r) => r.code.includes(searchForm.code))
    }

    pagination.total = list.length
    const start = (pagination.page - 1) * pagination.pageSize
    tableData.value = list.slice(start, start + pagination.pageSize)
    loading.value = false
  }, 300)
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.code = ''
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Role) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.code = row.code
  form.description = row.description
  dialogVisible.value = true
}

function handleDelete(row: Role) {
  if (row.userCount > 0) {
    ElMessage.warning(`角色「${row.name}」下有 ${row.userCount} 个用户，无法删除`)
    return
  }
  ElMessageBox.confirm(`确定要删除角色「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    const idx = mockRoles.findIndex((r) => r.id === row.id)
    if (idx > -1) mockRoles.splice(idx, 1)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleAssignPermission(row: Role) {
  editingRoleForPerm.value = row
  checkedPermKeys.value = [...row.permissions]
  permDialogVisible.value = true
}

function handlePermSubmit() {
  if (!editingRoleForPerm.value || !treeRef.value) return
  permSubmitLoading.value = true
  setTimeout(() => {
    const checked = treeRef.value!.getCheckedKeys(false) as string[]
    const halfChecked = treeRef.value!.getHalfCheckedKeys() as string[]
    editingRoleForPerm.value!.permissions = [...checked, ...halfChecked]
    permSubmitLoading.value = false
    permDialogVisible.value = false
    ElMessage.success('权限分配成功')
  }, 300)
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value && editingId.value !== null) {
        const item = mockRoles.find((r) => r.id === editingId.value)
        if (item) Object.assign(item, { ...form })
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockRoles.map((r) => r.id), 0) + 1
        mockRoles.push({
          id: newId,
          name: form.name,
          code: form.code,
          description: form.description,
          userCount: 0,
          permissions: [],
          createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
        })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      dialogVisible.value = false
      fetchData()
    }, 300)
  })
}

function handleDialogClosed() {
  resetForm()
  formRef.value?.resetFields()
}

function resetForm() {
  form.name = ''
  form.code = ''
  form.description = ''
}

// --------------- 初始化 ---------------
fetchData()
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.table-toolbar {
  margin-bottom: 12px;
}

.table-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
