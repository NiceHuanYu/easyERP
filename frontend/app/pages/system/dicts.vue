<template>
  <div class="page-container">
    <div class="dict-layout">
      <!-- 左侧字典类型树 -->
      <el-card shadow="never" class="dict-tree-card">
        <template #header>
          <div class="card-header">
            <span>字典类型</span>
            <el-button link type="primary" :icon="Plus" v-permission="'system:dict:create'" @click="handleAddType" />
          </div>
        </template>
        <el-tree
          ref="treeRef"
          :data="dictTypeTree"
          :props="{ children: 'children', label: 'label' }"
          node-key="id"
          highlight-current
          default-expand-all
          :current-node-key="selectedTypeId"
          @node-click="handleTypeClick"
        />
      </el-card>

      <!-- 右侧字典项 -->
      <el-card shadow="never" class="dict-items-card">
        <template #header>
          <span v-if="selectedType" class="card-title">
            {{ selectedType.label }}（{{ selectedType.code }}）
          </span>
          <span v-else>请选择字典类型</span>
        </template>

        <!-- 搜索栏 -->
        <el-form :model="searchForm" inline class="item-search">
          <el-form-item label="标签">
            <el-input v-model="searchForm.label" placeholder="请输入标签" clearable />
          </el-form-item>
          <el-form-item label="值">
            <el-input v-model="searchForm.value" placeholder="请输入值" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 工具栏 -->
        <div class="table-toolbar">
          <el-button type="primary" :icon="Plus" :disabled="!selectedType" v-permission="'system:dict:create'" @click="handleAddItem">
            新增字典项
          </el-button>
          <el-button :disabled="!selectedType" v-permission="'system:dict:edit'" @click="handleEditType">
            编辑字典类型
          </el-button>
          <el-button :disabled="!selectedType" type="danger" v-permission="'system:dict:delete'" @click="handleDeleteType">
            删除字典类型
          </el-button>
        </div>

        <!-- 字典项表格 -->
        <el-table :data="itemTableData" v-loading="itemLoading" border stripe>
          <el-table-column prop="label" label="标签" width="160" />
          <el-table-column prop="value" label="值" width="140" />
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" v-permission="'system:dict:edit'" @click="handleEditItem(row)">
                编辑
              </el-button>
              <el-button link type="danger" :icon="Delete" v-permission="'system:dict:delete'" @click="handleDeleteItem(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="itemPagination.page"
          v-model:page-size="itemPagination.pageSize"
          :total="itemPagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="table-pagination"
          @current-change="applyItemFilters"
          @size-change="applyItemFilters"
        />
      </el-card>
    </div>

    <!-- 新增/编辑字典类型对话框 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleTypeDialogClosed"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="类型标签" prop="label">
          <el-input v-model="typeForm.label" placeholder="请输入类型标签" />
        </el-form-item>
        <el-form-item label="类型编码" prop="code">
          <el-input v-model="typeForm.code" placeholder="请输入类型编码" :disabled="isTypeEdit" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="typeForm.parentId" placeholder="请选择上级分类（可选）" clearable style="width: 100%">
            <el-option
              v-for="t in typeOptionsForParent"
              :key="t.id"
              :label="t.label"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="typeForm.sort" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="handleTypeSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑字典项对话框 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleItemDialogClosed"
    >
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="标签" prop="label">
          <el-input v-model="itemForm.label" placeholder="请输入标签" />
        </el-form-item>
        <el-form-item label="值" prop="value">
          <el-input v-model="itemForm.value" placeholder="请输入值" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="itemForm.sort" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            :model-value="itemForm.status === 'active'"
            active-value="active"
            inactive-value="inactive"
            @change="(val: boolean) => { itemForm.status = val ? 'active' : 'inactive' }"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitLoading" @click="handleItemSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

// --------------- 类型 ---------------
interface DictType {
  id: number
  label: string
  code: string
  parentId: number | null
  sort: number
  remark: string
  children?: DictType[]
}

interface DictItem {
  id: number
  typeCode: string
  label: string
  value: string
  sort: number
  status: string
  remark: string
}

// --------------- Mock 字典类型数据 ---------------
// (loaded from API)

// --------------- Mock 字典项数据 ---------------
// (loaded from API)

// --------------- 字典类型树（转为 el-tree 所需格式） ---------------
const allDictTypes = ref<DictType[]>([])

function buildTypeTree(types: DictType[]): DictType[] {
  return types
    .filter((t) => !t.parentId)
    .map((t) => ({
      ...t,
      children: types.filter((c) => c.parentId === t.id),
    }))
}

const dictTypeTree = computed(() => buildTypeTree(allDictTypes.value))

const typeOptionsForParent = computed(() =>
  allDictTypes.value.filter((t) => t.id !== (typeEditingId.value ?? -1)),
)

// --------------- 状态 ---------------
const itemLoading = ref(false)

// 字典类型对话框
const typeDialogVisible = ref(false)
const isTypeEdit = ref(false)
const typeEditingId = ref<number | null>(null)
const typeSubmitLoading = ref(false)
const typeFormRef = ref<FormInstance>()

const typeForm = reactive({
  label: '',
  code: '',
  parentId: null as number | null,
  sort: 0,
  remark: '',
})

const typeRules: FormRules = {
  label: [{ required: true, message: '请输入类型标签', trigger: 'blur' }],
  code: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
}

// 字典项对话框
const itemDialogVisible = ref(false)
const isItemEdit = ref(false)
const itemEditingId = ref<number | null>(null)
const itemSubmitLoading = ref(false)
const itemFormRef = ref<FormInstance>()

const itemForm = reactive({
  label: '',
  value: '',
  sort: 0,
  status: 'active',
  remark: '',
})

const itemRules: FormRules = {
  label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入值', trigger: 'blur' }],
}

// 搜索与分页
const searchForm = reactive({
  label: '',
  value: '',
  status: '',
})

const itemPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const itemTableData = ref<DictItem[]>([])

// 当前选中类型
const selectedTypeId = ref<number | null>(null)
const allItems = ref<DictItem[]>([])

// --------------- 计算 ---------------
const selectedType = computed(() => {
  if (!selectedTypeId.value) return null
  return allDictTypes.value.find((t) => t.id === selectedTypeId.value) ?? null
})

const typeDialogTitle = computed(() => (isTypeEdit.value ? '编辑字典类型' : '新增字典类型'))
const itemDialogTitle = computed(() => (isItemEdit.value ? '编辑字典项' : '新增字典项'))

// --------------- 方法 ---------------
function handleTypeClick(data: DictType) {
  selectedTypeId.value = data.id
  itemPagination.page = 1
  searchForm.label = ''
  searchForm.value = ''
  searchForm.status = ''
  fetchItems()
}

function fetchItems() {
  if (!selectedType.value) {
    itemTableData.value = []
    itemPagination.total = 0
    return
  }
  itemLoading.value = true
  api.get<DictItem[]>(`/system/dicts/${selectedType.value.code}/items`)
    .then((items) => {
      allItems.value = items
      applyItemFilters()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '获取字典项失败')
    })
    .finally(() => {
      itemLoading.value = false
    })
}

function applyItemFilters() {
  let list = [...allItems.value]
  if (searchForm.label) {
    list = list.filter((i) => i.label.includes(searchForm.label))
  }
  if (searchForm.value) {
    list = list.filter((i) => i.value.includes(searchForm.value))
  }
  if (searchForm.status) {
    list = list.filter((i) => i.status === searchForm.status)
  }
  itemPagination.total = list.length
  const start = (itemPagination.page - 1) * itemPagination.pageSize
  itemTableData.value = list.slice(start, start + itemPagination.pageSize)
}

function handleSearch() {
  itemPagination.page = 1
  applyItemFilters()
}

function handleReset() {
  searchForm.label = ''
  searchForm.value = ''
  searchForm.status = ''
  itemPagination.page = 1
  applyItemFilters()
}

// --------------- 字典类型 CRUD ---------------
function handleAddType() {
  isTypeEdit.value = false
  typeEditingId.value = null
  resetTypeForm()
  typeDialogVisible.value = true
}

function handleEditType() {
  if (!selectedType.value) return
  const t = selectedType.value
  isTypeEdit.value = true
  typeEditingId.value = t.id
  typeForm.label = t.label
  typeForm.code = t.code
  typeForm.parentId = t.parentId
  typeForm.sort = t.sort
  typeForm.remark = t.remark
  typeDialogVisible.value = true
}

function handleDeleteType() {
  if (!selectedType.value) return
  const t = selectedType.value
  ElMessageBox.confirm(`确定要删除字典类型「${t.label}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      await api.del(`/system/dicts/types/${t.id}`)
      ElMessage.success('删除成功')
      selectedTypeId.value = null
      fetchAllTypes()
    } catch (e: any) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

function handleTypeSubmit() {
  typeFormRef.value?.validate(async (valid) => {
    if (!valid) return
    typeSubmitLoading.value = true
    try {
      const payload = {
        label: typeForm.label,
        code: typeForm.code,
        parentId: typeForm.parentId,
        sort: typeForm.sort,
        remark: typeForm.remark,
      }
      if (isTypeEdit.value && typeEditingId.value !== null) {
        await api.put(`/system/dicts/types/${typeEditingId.value}`, payload)
        ElMessage.success('编辑成功')
      } else {
        await api.post('/system/dicts/types', payload)
        ElMessage.success('新增成功')
      }
      typeDialogVisible.value = false
      fetchAllTypes()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      typeSubmitLoading.value = false
    }
  })
}

function handleTypeDialogClosed() {
  resetTypeForm()
  typeFormRef.value?.resetFields()
}

function resetTypeForm() {
  typeForm.label = ''
  typeForm.code = ''
  typeForm.parentId = null
  typeForm.sort = 0
  typeForm.remark = ''
}

// --------------- 字典项 CRUD ---------------
function handleAddItem() {
  if (!selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  isItemEdit.value = false
  itemEditingId.value = null
  resetItemForm()
  itemDialogVisible.value = true
}

function handleEditItem(row: DictItem) {
  isItemEdit.value = true
  itemEditingId.value = row.id
  itemForm.label = row.label
  itemForm.value = row.value
  itemForm.sort = row.sort
  itemForm.status = row.status
  itemForm.remark = row.remark
  itemDialogVisible.value = true
}

function handleDeleteItem(row: DictItem) {
  if (!selectedType.value) return
  ElMessageBox.confirm(`确定要删除字典项「${row.label}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      await api.del(`/system/dicts/${selectedType.value!.code}/items/${row.id}`)
      ElMessage.success('删除成功')
      fetchItems()
    } catch (e: any) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

function handleItemSubmit() {
  itemFormRef.value?.validate(async (valid) => {
    if (!valid) return
    if (!selectedType.value) return
    itemSubmitLoading.value = true
    try {
      const payload = {
        label: itemForm.label,
        value: itemForm.value,
        sort: itemForm.sort,
        status: itemForm.status,
        remark: itemForm.remark,
      }
      const typeCode = selectedType.value.code
      if (isItemEdit.value && itemEditingId.value !== null) {
        await api.put(`/system/dicts/${typeCode}/items/${itemEditingId.value}`, payload)
        ElMessage.success('编辑成功')
      } else {
        await api.post(`/system/dicts/${typeCode}/items`, payload)
        ElMessage.success('新增成功')
      }
      itemDialogVisible.value = false
      fetchItems()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      itemSubmitLoading.value = false
    }
  })
}

function handleItemDialogClosed() {
  resetItemForm()
  itemFormRef.value?.resetFields()
}

function resetItemForm() {
  itemForm.label = ''
  itemForm.value = ''
  itemForm.sort = 0
  itemForm.status = 'active'
  itemForm.remark = ''
}

// --------------- 初始化 ---------------
async function fetchAllTypes() {
  try {
    const types = await api.get<DictType[]>('/system/dicts/all')
    allDictTypes.value = types
    // Auto-select first type if none selected
    if (!selectedTypeId.value && types.length > 0) {
      selectedTypeId.value = types[0].id
    }
    if (selectedTypeId.value) {
      fetchItems()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取字典类型失败')
  }
}

fetchAllTypes()
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dict-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.dict-tree-card {
  width: 260px;
  flex-shrink: 0;
}

.dict-tree-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dict-items-card {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-weight: 600;
}

.item-search {
  margin-bottom: 8px;
}

.item-search :deep(.el-form-item) {
  margin-bottom: 8px;
}

.table-toolbar {
  margin-bottom: 12px;
}

.table-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
