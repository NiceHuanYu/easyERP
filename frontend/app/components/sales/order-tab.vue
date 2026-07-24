<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box">
          <span class="erp-search-icon">🔍</span>
          <input v-model="search" type="text" placeholder="搜索订单号、客户..." class="erp-search-input" />
        </div>
        <select v-model="filterStatus" class="erp-filter-select">
          <option value="">全部状态</option>
          <option v-for="st in statusList" :key="st" :value="st">{{ st }}</option>
        </select>
      </div>
      <div class="erp-toolbar-right">
        <button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建订单</button>
      </div>
    </div>

    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th @click="sortBy('code')" class="sortable">
              订单号{{ sortField === 'code' ? (sortAsc ? '▲' : '▼') : '' }}
            </th>
            <th @click="sortBy('customer')" class="sortable">
              客户{{ sortField === 'customer' ? (sortAsc ? '▲' : '▼') : '' }}
            </th>
            <th>产品明细</th>
            <th style="text-align:right;">总数量</th>
            <th style="text-align:right;">总金额(元)</th>
            <th>交期</th>
            <th>状态</th>
            <th style="text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td>
            <td>{{ row.customer }}</td>
            <td class="erp-cell-spec">{{ summary(row.items) }}</td>
            <td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td class="erp-cell-num">{{ totalAmount(row.items).toFixed(2) }}</td>
            <td class="erp-cell-spec">{{ row.delivery }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
              <button class="erp-lnk" style="color:#2e7d32;" v-if="row.status==='草稿'" @click="submit(row)">提交</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDelete(row)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length === 0">
            <td colspan="8" class="erp-cell-empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="pageSize" />

    <!-- 订单明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'订单明细 - ' + detailCode" @close="showDetail = false" @save="showDetail = false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">
        客户：{{ detailCustomer }} ｜ 交期：{{ detailDelivery }}
      </div>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead>
            <tr>
              <th>物料/产品</th>
              <th style="text-align:right;">数量</th>
              <th>单位</th>
              <th style="text-align:right;">金额(元)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in detailItems" :key="idx">
              <td>{{ item.material }}</td>
              <td class="erp-cell-num">{{ item.qty }}</td>
              <td>{{ item.unit }}</td>
              <td class="erp-cell-num">{{ item.amount.toFixed(2) }}</td>
            </tr>
            <tr style="font-weight:600;background:#f9fafb;">
              <td>合计</td>
              <td class="erp-cell-num">{{ totalQty(detailItems) }}</td>
              <td></td>
              <td class="erp-cell-num">{{ totalAmount(detailItems).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </FormModal>

    <!-- 订单表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing ? '编辑订单' : '新建订单'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm = false" @save="save"
    >
      <div class="erp-form-grid">
        <div class="erp-form-group">
          <label>订单号</label>
          <input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" />
        </div>
        <div class="erp-form-group">
          <label>客户 <span class="erp-form-req">*</span></label>
          <input v-model="f.customer" placeholder="客户名称" />
        </div>
        <div class="erp-form-group">
          <label>交货日期</label>
          <input v-model="f.delivery" type="text" placeholder="2025-08-15" />
        </div>
        <div class="erp-form-group" v-if="editing">
          <label>状态</label>
          <select v-model="f.status">
            <option v-for="st in statusList" :key="st" :value="st">{{ st }}</option>
          </select>
        </div>
        <div class="erp-form-group full">
          <label>备注</label>
          <textarea v-model="f.remark" rows="2" placeholder="可选"></textarea>
        </div>
      </div>

      <FormSection title="产品明细" hint="添加订单中的产品，金额将自动汇总">
        <div class="erp-table-wrap">
          <table class="erp-table">
            <thead>
              <tr>
                <th>物料/产品 <span class="erp-form-req">*</span></th>
                <th style="text-align:right;">数量</th>
                <th>单位</th>
                <th style="text-align:right;">金额(元)</th>
                <th style="text-align:center;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in f.items" :key="idx">
                <td><input v-model="item.material" placeholder="产品名称" class="erp-tbl-input" /></td>
                <td><input v-model.number="item.qty" type="number" min="1" placeholder="1" class="erp-tbl-input erp-tbl-input-num" /></td>
                <td>
                  <select v-model="item.unit" class="erp-tbl-select">
                    <option>台</option><option>套</option><option>件</option><option>个</option>
                  </select>
                </td>
                <td><input v-model.number="item.amount" type="number" step="0.01" placeholder="0.00" class="erp-tbl-input erp-tbl-input-num" /></td>
                <td class="erp-cell-acts"><button class="erp-lnk erp-lnk-danger" @click="f.items.splice(idx, 1)">删除</button></td>
              </tr>
              <tr v-if="f.items.length === 0">
                <td colspan="5" class="erp-cell-empty">暂无产品，请添加</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;margin-top:10px;">
          <button class="erp-btn erp-btn-primary" style="padding:6px 16px;font-size:12px;" @click="addItem()">＋ 添加产品</button>
          <span style="font-size:13px;color:#555;">
            合计金额：<strong style="color:#1a73e8;">¥{{ totalAmount(f.items).toFixed(2) }}</strong>
          </span>
        </div>
      </FormSection>
    </FormModal>

    <ConfirmDialog :show="showDelete" title="确认删除" @confirm="doDelete" @cancel="showDelete = false">
      <p>确定要删除订单 <strong>{{ deleteTarget?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
interface OrderItem {
  material: string; qty: number; unit: string; amount: number
}

interface Order {
  code: string; customer: string; items: OrderItem[]
  delivery: string; status: string; remark: string; sc: string
}

const statusList = ['草稿', '待审核', '已审核', '生产中', '待发货', '已发货', '已完成', '已取消']

const data = ref<Order[]>([
  { code: 'SO-2025-0089', customer: '华强电子', items: [{ material: '减速器 SA67', qty: 5, unit: '台', amount: 14250 }], delivery: '2025-08-15', status: '待审核', remark: '', sc: 'pending' },
  { code: 'SO-2025-0088', customer: '精密模具', items: [{ material: '电动滚筒 5.5kW', qty: 3, unit: '台', amount: 20400 }, { material: '减速器 SA67', qty: 2, unit: '台', amount: 5700 }], delivery: '2025-08-10', status: '生产中', remark: '', sc: 'progress' },
  { code: 'SO-2025-0087', customer: '鑫达科技', items: [{ material: '皮带输送机 10m', qty: 1, unit: '套', amount: 32500 }], delivery: '2025-07-25', status: '已发货', remark: '物流已发出', sc: 'shipped' },
  { code: 'SO-2025-0086', customer: '华强电子', items: [{ material: '螺旋输送机 LS400', qty: 2, unit: '台', amount: 37200 }], delivery: '2025-07-18', status: '已完成', remark: '已签收', sc: 'completed' },
  { code: 'SO-2025-0085', customer: '丰华重型', items: [{ material: '振动电机 YZO-8-4', qty: 10, unit: '台', amount: 16500 }, { material: '减速器 SA67', qty: 3, unit: '台', amount: 8550 }], delivery: '2025-08-20', status: '草稿', remark: '', sc: 'draft' },
  { code: 'SO-2025-0084', customer: '天工精密', items: [{ material: '刮板机配件-链节', qty: 50, unit: '件', amount: 4250 }], delivery: '2025-07-30', status: '已取消', remark: '客户取消', sc: 'cancelled' },
  { code: 'SO-2025-0083', customer: '宏远机械', items: [{ material: '皮带输送机 10m', qty: 1, unit: '套', amount: 32500 }], delivery: '2025-08-05', status: '已审核', remark: '加急处理', sc: 'approved' },
])

// ---- 使用 composables ----
const { search, sortField, sortAsc, page, pageSize, sortBy, filteredWith, pagedFrom } = useTable({
  data,
  searchFields: ['code', 'customer'],
  initialSort: 'code',
})

const filterStatus = ref('')
const filtered = filteredWith(item => {
  if (filterStatus.value && item.status !== filterStatus.value) return false
  return true
})
const paged = pagedFrom(filtered)

watch(filterStatus, () => { page.value = 1 })

const { showDelete, deleteTarget, confirmDelete, doDelete } = useDelete(data, 'code')

// ---- 明细查看 ----
const showDetail = ref(false)
const detailCode = ref('')
const detailCustomer = ref('')
const detailDelivery = ref('')
const detailItems = ref<OrderItem[]>([])

function viewItems(row: Order) {
  detailCode.value = row.code
  detailCustomer.value = row.customer
  detailDelivery.value = row.delivery
  detailItems.value = [...row.items]
  showDetail.value = true
}

// ---- 表单 ----
const numberingMode = ref('auto')
const showForm = ref(false)
const editing = ref(false)
const f = reactive<{ code: string; customer: string; items: OrderItem[]; delivery: string; status: string; remark: string }>({
  code: '', customer: '', items: [], delivery: '', status: '草稿', remark: '',
})
let ec = ''

function addItem() { f.items.push({ material: '', qty: 1, unit: '台', amount: 0 }) }

function openForm(item?: Order) {
  if (item) {
    editing.value = true; ec = item.code
    Object.assign(f, { ...item, items: item.items.map(i => ({ ...i })) })
  } else {
    editing.value = false; numberingMode.value = 'auto'
    f.code = `SO-${new Date().getFullYear()}-${String(data.value.filter(m => m.code.startsWith('SO-')).length + 1).padStart(4, '0')}`
    f.customer = ''; f.items = [{ material: '', qty: 1, unit: '台', amount: 0 }]
    f.delivery = ''; f.status = '草稿'; f.remark = ''
  }
  showForm.value = true
}

function save() {
  if (!f.customer) { alert('请填写客户'); return }
  if (!f.items.length || f.items.some(i => !i.material)) { alert('请填写产品明细'); return }
  const copy = { ...f, items: f.items.map(i => ({ ...i })) }
  if (editing.value) {
    const i = data.value.findIndex(m => m.code === ec)
    if (i !== -1) data.value[i] = copy as Order
  } else {
    data.value.push(copy as Order)
  }
  showForm.value = false
}

function submit(item: Order) { item.status = '待审核'; item.sc = 'pending' }
</script>

<style scoped>
.erp-tag.draft { background: #f5f5f5; color: #999; }
.erp-tag.pending { background: #fff3e0; color: #e65100; }
.erp-tag.approved { background: #e3f2fd; color: #1565c0; }
.erp-tag.progress { background: #e8f5e9; color: #2e7d32; }
.erp-tag.shipped { background: #e0f2f1; color: #00695c; }
.erp-tag.completed { background: #f3e5f5; color: #6a1b9a; }
.erp-tag.cancelled { background: #fce4ec; color: #c62828; }

.erp-tbl-input { width: 100%; padding: 6px 8px; border: 1px solid #e0e0e0; border-radius: 4px; font-size: 12px; outline: none; background: #fafafa; font-family: inherit; }
.erp-tbl-input:focus { border-color: #1a73e8; background: #fff; }
.erp-tbl-input-num { text-align: right; }
.erp-tbl-select { padding: 6px 4px; border: 1px solid #e0e0e0; border-radius: 4px; font-size: 12px; outline: none; background: #fafafa; }
</style>
