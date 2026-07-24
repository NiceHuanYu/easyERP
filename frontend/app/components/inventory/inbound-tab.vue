<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box">
          <span class="erp-search-icon">🔍</span>
          <input v-model="search" placeholder="搜索入库单号、物料..." class="erp-search-input" />
        </div>
        <select v-model="filterType" class="erp-filter-select">
          <option value="">全部类型</option>
          <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
        </select>
      </div>
      <div class="erp-toolbar-right">
        <button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建入库单</button>
      </div>
    </div>

    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th @click="sortBy('code')" class="sortable">
              入库单号{{ sortField === 'code' ? (sortAsc ? '▲' : '▼') : '' }}
            </th>
            <th>类型</th>
            <th>物料</th>
            <th style="text-align:right;">数量</th>
            <th>仓库</th>
            <th>来源单号</th>
            <th>日期</th>
            <th>状态</th>
            <th style="text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in paged" :key="r.code">
            <td class="erp-cell-code">{{ r.code }}</td>
            <td><span class="erp-tag">{{ r.type }}</span></td>
            <td class="erp-cell-spec">{{ r.material }}</td>
            <td class="erp-cell-num">{{ r.qty }}{{ r.unit }}</td>
            <td>{{ r.wh }}</td>
            <td class="erp-cell-spec">{{ r.src }}</td>
            <td class="erp-cell-spec">{{ r.date }}</td>
            <td><span :class="['erp-tag', r.sc]">{{ r.status }}</span></td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="openForm(r)">编辑</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDelete(r)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length === 0">
            <td colspan="9" class="erp-cell-empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="pageSize" />

    <FormModal
      :show="showForm"
      :title="editing ? '编辑入库单' : '新建入库单'"
      @close="showForm = false"
      @save="save"
    >
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label">
          <input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span>
        </label>
        <label class="erp-radio-label">
          <input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span>
        </label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group">
          <label>入库单号</label>
          <input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" />
        </div>
        <div class="erp-form-group">
          <label>入库类型</label>
          <select v-model="f.type">
            <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
        <div class="erp-form-group">
          <label>物料 <span class="erp-form-req">*</span></label>
          <input v-model="f.material" placeholder="物料名称" />
        </div>
        <div class="erp-form-group">
          <label>数量</label>
          <input v-model.number="f.qty" type="number" min="1" />
        </div>
        <div class="erp-form-group">
          <label>单位</label>
          <select v-model="f.unit">
            <option>个</option><option>件</option><option>kg</option><option>张</option><option>根</option>
          </select>
        </div>
        <div class="erp-form-group">
          <label>仓库</label>
          <input v-model="f.wh" placeholder="如 原材料库" />
        </div>
        <div class="erp-form-group">
          <label>来源单号</label>
          <input v-model="f.src" placeholder="关联单号" />
        </div>
        <div class="erp-form-group">
          <label>日期</label>
          <input v-model="f.date" type="text" placeholder="2025-08-01" />
        </div>
        <div class="erp-form-group">
          <label>状态</label>
          <select v-model="f.status">
            <option>草稿</option><option>已入库</option>
          </select>
        </div>
        <div class="erp-form-group full">
          <label>备注</label>
          <textarea v-model="f.remark" rows="2"></textarea>
        </div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDelete" title="确认删除" @confirm="doDelete" @cancel="showDelete = false">
      <p>确定删除入库单 <strong>{{ deleteTarget?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
interface InboundRecord {
  code: string; type: string; material: string; qty: number; unit: string
  wh: string; src: string; date: string; status: string; remark: string; sc: string
}

const types = ['采购入库', '完工入库', '退货入库', '调拨入库']

const data = ref<InboundRecord[]>([
  { code: 'IN-001', type: '采购入库', material: '45#圆钢 Φ20', qty: 500, unit: 'kg', wh: '原材料库', src: 'PO-2025-0055', date: '2025-07-25', status: '已入库', remark: '', sc: 'done' },
  { code: 'IN-002', type: '采购入库', material: '铜排 TMY-40×4', qty: 50, unit: 'm', wh: '原材料库', src: 'PO-2025-0051', date: '2025-08-01', status: '已入库', remark: '', sc: 'done' },
  { code: 'IN-003', type: '完工入库', material: '减速器 SA67', qty: 10, unit: '台', wh: '成品库', src: 'MO-2025-0041', date: '2025-07-25', status: '已入库', remark: '', sc: 'done' },
  { code: 'IN-004', type: '完工入库', material: '螺旋输送机 LS400', qty: 1, unit: '台', wh: '成品库', src: 'MO-2025-0042', date: '2025-07-28', status: '已入库', remark: '', sc: 'done' },
  { code: 'IN-005', type: '调拨入库', material: '304不锈钢板 2mm', qty: 5, unit: '张', wh: '钢材专用库', src: 'TRF-001', date: '2025-07-30', status: '草稿', remark: '', sc: 'draft' },
])

// ---- 使用 composables ----
const { search, sortField, sortAsc, page, pageSize, sortBy, filteredWith, pagedFrom } = useTable({
  data,
  searchFields: ['code', 'material'],
  initialSort: 'code',
})

const filterType = ref('')
const filtered = filteredWith(item => {
  if (filterType.value && item.type !== filterType.value) return false
  return true
})
const paged = pagedFrom(filtered)

watch(filterType, () => { page.value = 1 })

const { showDelete, deleteTarget, confirmDelete, doDelete } = useDelete(data, 'code')

// ---- 表单 ----
const showForm = ref(false)
const editing = ref(false)
const numberingMode = ref('auto')
const f = reactive({ code: '', type: '采购入库', material: '', qty: 1, unit: '个', wh: '', src: '', date: '', status: '草稿', remark: '' })
let ec = ''

function openForm(item?: InboundRecord) {
  if (item) {
    editing.value = true; ec = item.code
    Object.assign(f, { ...item })
  } else {
    editing.value = false; numberingMode.value = 'auto'
    f.code = `IN-${String(data.value.length + 1).padStart(3, '0')}`
    f.type = '采购入库'; f.material = ''; f.qty = 1; f.unit = '个'
    f.wh = ''; f.src = ''; f.date = ''; f.status = '草稿'; f.remark = ''
  }
  showForm.value = true
}

function save() {
  if (!f.material) { alert('请填写物料'); return }
  const copy = { ...f }
  if (editing.value) {
    const i = data.value.findIndex(m => m.code === ec)
    if (i !== -1) data.value[i] = copy as InboundRecord
  } else {
    data.value.push(copy as InboundRecord)
  }
  showForm.value = false
}
</script>

<style scoped>
.erp-tag.done { background: #e8f5e9; color: #2e7d32; }
.erp-tag.draft { background: #f5f5f5; color: #999; }
</style>
