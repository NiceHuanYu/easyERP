<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="账户归属">
          <el-radio-group v-model="accountSource" @change="handleSearch">
            <el-radio-button value="company">本公司</el-radio-button>
            <el-radio-button value="customer">客户</el-radio-button>
            <el-radio-button value="supplier">供应商</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="名称/账号/户名" clearable style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" v-permission="'base-data:company-account:create'" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width:100%">
        <el-table-column prop="name" label="名称" min-width="150" v-if="accountSource !== 'company'" />
        <el-table-column prop="bankName" label="银行名称" min-width="120" />
        <el-table-column prop="branchName" label="支行" min-width="140" v-if="accountSource === 'company'" />
        <el-table-column prop="accountNo" label="账号" min-width="140" />
        <el-table-column prop="accountName" label="户名" min-width="120" />
        <el-table-column prop="currency" label="币种" min-width="70" v-if="accountSource === 'company'" />
        <el-table-column prop="accountType" label="类型" min-width="80" v-if="accountSource === 'company'">
          <template #default="{ row }">{{ typeMap[row.accountType] ?? row.accountType }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="70" v-if="accountSource === 'company'">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-permission="'base-data:company-account:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" v-permission="'base-data:company-account:delete'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="pagination.total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        background style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 客户/供应商：关联实体 -->
        <el-form-item v-if="accountSource !== 'company'" label="名称" prop="name">
          <el-select v-model="form.name" placeholder="请选择" filterable style="width:100%" :disabled="isEdit"
            @change="onEntityChange">
            <el-option v-for="e in entityOptions" :key="e.value" :label="e.label" :value="e.label" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="银行名称" prop="bankName">
              <el-input v-model="form.bankName" placeholder="如：招商银行" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="accountSource === 'company'">
            <el-form-item label="支行">
              <el-input v-model="form.branchName" placeholder="如：深圳分行" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="银行账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户名" prop="accountName">
              <el-input v-model="form.accountName" placeholder="户名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16" v-if="accountSource === 'company'">
          <el-col :span="8">
            <el-form-item label="币种" label-width="70px">
              <el-select v-model="form.currency" style="width:100%"><el-option label="CNY" value="CNY" /><el-option label="USD" value="USD" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型" label-width="70px">
              <el-select v-model="form.accountType" style="width:100%"><el-option label="均可" value="BOTH" /><el-option label="收款" value="RECEIVE" /><el-option label="付款" value="PAY" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" label-width="70px">
              <el-select v-model="form.status" style="width:100%"><el-option label="启用" :value="1" /><el-option label="禁用" :value="0" /></el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" v-if="accountSource === 'company'">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { api } from '../../composables/useApi'

definePageMeta({ middleware: 'auth' })

const typeMap: Record<string, string> = { RECEIVE: '收款', PAY: '付款', BOTH: '均可' }

const accountSource = ref<'company' | 'customer' | 'supplier'>('company')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const entityId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const tableData = ref<any[]>([])
const entityOptions = ref<{ label: string; value: number }[]>([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const form = reactive({
  name: '', bankName: '', branchName: '', accountNo: '', accountName: '',
  currency: 'CNY', accountType: 'BOTH', status: 1, remark: '',
})
const rules = computed<FormRules>(() => ({
  bankName: [{ required: true, message: '请输入银行名称', trigger: 'blur' }],
  accountNo: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入户名', trigger: 'blur' }],
  name: accountSource.value !== 'company' ? [{ required: true, message: '请选择名称', trigger: 'change' }] : undefined as any,
}))

const dialogTitle = computed(() => isEdit.value ? '编辑账户' : '新增账户')

// ── Data Fetching ──────────────────────────────────
async function fetchCompanyAccounts() {
  const r = await api.page<any>('/base/company-accounts', pagination.page, pagination.pageSize,
    searchForm.keyword ? { keyword: searchForm.keyword } : undefined)
  tableData.value = r.list.map((a: any) => ({ ...a, name: a.accountName }))
  pagination.total = r.total
}

async function fetchCustomerAccounts() {
  const r = await api.page<any>('/base/counterparty-accounts', pagination.page, pagination.pageSize,
    { ownerType: 'CUSTOMER', ...(searchForm.keyword ? { keyword: searchForm.keyword } : {}) })
  tableData.value = r.list.map((a: any) => ({ ...a, name: a.ownerName }))
  pagination.total = r.total
}

async function fetchSupplierAccounts() {
  const r = await api.page<any>('/base/counterparty-accounts', pagination.page, pagination.pageSize,
    { ownerType: 'SUPPLIER', ...(searchForm.keyword ? { keyword: searchForm.keyword } : {}) })
  tableData.value = r.list.map((a: any) => ({ ...a, name: a.ownerName }))
  pagination.total = r.total
}

async function fetchData() {
  loading.value = true
  try {
    if (accountSource.value === 'company') await fetchCompanyAccounts()
    else if (accountSource.value === 'customer') await fetchCustomerAccounts()
    else await fetchSupplierAccounts()
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

async function loadEntityOptions() {
  try {
    const path = accountSource.value === 'customer' ? '/base/customers' : '/base/suppliers'
    const r = await api.page<any>(path, 1, 1000)
    entityOptions.value = r.list.map((e: any) => ({ label: e.name, value: e.id }))
  } catch { entityOptions.value = [] }
}

// ── Handlers ───────────────────────────────────────
function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
watch([() => pagination.page, () => pagination.pageSize], () => fetchData())
watch(accountSource, () => { handleSearch(); loadEntityOptions() })

function handleAdd() {
  isEdit.value = false; editingId.value = null; entityId.value = null
  Object.assign(form, { name: '', bankName: '', branchName: '', accountNo: '', accountName: '', currency: 'CNY', accountType: 'BOTH', status: 1, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; editingId.value = row.id; entityId.value = row.ownerId
  form.name = row.ownerName || row.name || ''
  form.bankName = row.bankName || ''
  form.branchName = row.branchName || ''
  form.accountNo = row.accountNo || ''
  form.accountName = row.accountName || ''
  if (accountSource.value === 'company') {
    form.currency = row.currency ?? 'CNY'
    form.accountType = row.accountType ?? 'BOTH'
    form.status = row.status ?? 1
    form.remark = row.remark ?? ''
  }
  dialogVisible.value = true
}

function onEntityChange(val: string) {
  const e = entityOptions.value.find(o => o.label === val)
  entityId.value = e ? e.value : null
}

async function handleSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  try {
    if (accountSource.value === 'company') {
      const payload: any = { bankName: form.bankName, branchName: form.branchName, accountNo: form.accountNo, accountName: form.accountName, currency: form.currency, accountType: form.accountType, status: form.status, remark: form.remark }
      if (isEdit.value) await api.put('/base/company-accounts/' + editingId.value, payload)
      else await api.post('/base/company-accounts', payload)
    } else {
      const payload: any = {
        ownerType: accountSource.value === 'customer' ? 'CUSTOMER' : 'SUPPLIER',
        ownerId: editingId.value ?? entityId.value,
        bankName: form.bankName, accountNo: form.accountNo, accountName: form.accountName,
      }
      if (isEdit.value) {
        await api.put('/base/counterparty-accounts/' + editingId.value, payload)
      } else {
        await api.post('/base/counterparty-accounts', payload)
      }
    }
    ElMessage.success(isEdit.value ? '已更新' : '已创建')
    dialogVisible.value = false; fetchData()
  } catch { ElMessage.error('保存失败') }
}

async function handleDelete(row: any) {
  const url = accountSource.value === 'company' ? '/base/company-accounts/' : '/base/counterparty-accounts/'
  try { await api.del(url + row.id); ElMessage.success('已删除'); fetchData() }
  catch { ElMessage.error('删除失败') }
}

onMounted(() => { fetchData(); loadEntityOptions() })
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap:12px; }
.search-card :deep(.el-card__body) { padding-bottom:0; }
.table-toolbar { margin-bottom:12px; }
</style>
