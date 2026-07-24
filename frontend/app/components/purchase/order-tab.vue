<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索订单号、供应商..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建订单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">订单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('supplier')" class="sortable">供应商{{ sf==='supplier'?(sa?'▲':'▼'):'' }}</th>
          <th>物料明细</th><th style="text-align:right;">总数量</th><th style="text-align:right;">总金额(元)</th><th>交货日期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.supplier }}</td>
            <td class="erp-cell-spec">{{ summary(row.items) }}</td>
            <td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td class="erp-cell-num">{{ totalAmount(row.items).toFixed(2) }}</td>
            <td class="erp-cell-spec">{{ row.delivery }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <!-- 明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'订单明细 - '+detailCode" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">供应商：{{ detailSupplier }} ｜ 交期：{{ detailDelivery }}</div>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr><th>物料</th><th style="text-align:right;">数量</th><th>单位</th><th style="text-align:right;">金额(元)</th></tr></thead>
          <tbody>
            <tr v-for="(item,idx) in detailItems" :key="idx">
              <td>{{ item.material }}</td><td class="erp-cell-num">{{ item.qty }}</td>
              <td>{{ item.unit }}</td><td class="erp-cell-num">{{ item.amount.toFixed(2) }}</td>
            </tr>
            <tr style="font-weight:600;background:#f9fafb;">
              <td>合计</td><td class="erp-cell-num">{{ totalQty(detailItems) }}</td>
              <td></td><td class="erp-cell-num">{{ totalAmount(detailItems).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </FormModal>

    <!-- 订单表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing?'编辑订单':'新建订单'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm=false" @save="save"
    >
      <!-- 基本信息 -->
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>订单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>供应商 <span class="erp-form-req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="erp-form-group"><label>交货日期</label><input v-model="f.delivery" type="text" placeholder="2025-08-20" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>

      <!-- 物料明细区 -->
      <ErpItemTable
        v-model:items="f.items"
        :columns="itemColumns"
        empty-text="暂无物料，请添加"
        add-label="＋ 添加物料"
      />
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除订单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待确认','已确认','已发货','已到货','已完成','已取消']

interface PurchaseItem {
  material: string
  qty: number
  unit: string
  amount: number
}

interface PurchaseOrder {
  code: string
  supplier: string
  items: PurchaseItem[]
  delivery: string
  status: string
  remark: string
  sc: string
}

function totalQty(items: PurchaseItem[]) { return items.reduce((s,i) => s + (i.qty||0), 0) }
function totalAmount(items: PurchaseItem[]) { return items.reduce((s,i) => s + (i.amount||0), 0) }
function summary(items: PurchaseItem[]) {
  if (!items.length) return '-'
  return items.length === 1 ? items[0].material : `${items[0].material} 等${items.length}项`
}

const data = ref<PurchaseOrder[]>([
  { code:'PO-2025-0056',supplier:'宏远钢铁',items:[{material:'45#圆钢 Φ20',qty:500,unit:'kg',amount:2400}],delivery:'2025-08-10',status:'待确认',remark:'',sc:'pending'},
  { code:'PO-2025-0055',supplier:'宏远钢铁',items:[{material:'Q235槽钢 10#',qty:20,unit:'根',amount:704}],delivery:'2025-07-25',status:'已到货',remark:'',sc:'arrived'},
  { code:'PO-2025-0054',supplier:'正泰电气',items:[{material:'电动滚筒 5.5kW',qty:3,unit:'台',amount:15600},{material:'减速器 SA67',qty:2,unit:'台',amount:5700}],delivery:'2025-08-05',status:'已确认',remark:'',sc:'confirmed'},
  { code:'PO-2025-0053',supplier:'东明标准件',items:[{material:'M8×30六角螺栓',qty:2000,unit:'个',amount:900}],delivery:'2025-08-15',status:'草稿',remark:'',sc:'draft'},
  { code:'PO-2025-0052',supplier:'宏达橡塑',items:[{material:'O型密封圈 Φ50×3.5',qty:1000,unit:'只',amount:180}],delivery:'2025-07-28',status:'已完成',remark:'',sc:'completed'},
  { code:'PO-2025-0051',supplier:'宏远钢铁',items:[{material:'铜排 TMY-40×4',qty:50,unit:'m',amount:4300}],delivery:'2025-08-01',status:'已发货',remark:'物流在途',sc:'shipped'},
])

const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.supplier.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailCode=ref('');const detailSupplier=ref('');const detailDelivery=ref('');const detailItems=ref<PurchaseItem[]>([])
function viewItems(row:PurchaseOrder){detailCode.value=row.code;detailSupplier.value=row.supplier;detailDelivery.value=row.delivery;detailItems.value=[...row.items];showDetail.value=true}

// ---- 物品表格列配置 ----
const itemColumns = [
  { key: 'material', label: '物料名称', type: 'autocomplete' as const, required: true, placeholder: '搜索或输入物料名' },
  { key: 'qty', label: '数量', type: 'number' as const, align: 'right' as const, min: 1, total: true },
  { key: 'unit', label: '单位', type: 'select' as const, options: ['个', '件', 'kg', '张', '根', '桶'] },
  { key: 'amount', label: '金额(元)', type: 'number' as const, align: 'right' as const, step: 0.01, total: true, totalLabel: '合计金额' },
]

// 表单
const numberingMode=ref('auto')
const showForm=ref(false);const editing=ref(false)
const f=reactive<{code:string;supplier:string;items:PurchaseItem[];delivery:string;status:string;remark:string}>({code:'',supplier:'',items:[],delivery:'',status:'草稿',remark:''})
let ec=''

function openForm(item?:PurchaseOrder){
  if(item){
    editing.value=true;ec=item.code
    Object.assign(f,{...item,items:item.items.map(i=>({...i}))})
  }else{
    editing.value=false;numberingMode.value='auto'
    f.code=`PO-${new Date().getFullYear()}-${String(data.value.filter(m=>m.code.startsWith('PO-')).length+1).padStart(4,'0')}`
    f.supplier='';f.items=[];f.delivery='';f.status='草稿';f.remark=''
  }
  showForm.value=true
}

function save(){
  if(!f.supplier){alert('请填写供应商');return}
  if(!f.items.length||f.items.some(i=>!i.material)){alert('请填写物料明细');return}
  const copy={...f,items:f.items.map(i=>({...i}))}
  if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]=copy as any}
  else data.value.push(copy as any)
  showForm.value=false
}

const showDel=ref(false);const dt=ref<PurchaseOrder|null>(null)
function confirmDel(item:PurchaseOrder){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>

<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.confirmed{background:#e3f2fd;color:#1565c0;}.erp-tag.shipped{background:#e0f2f1;color:#00695c;}
.erp-tag.arrived{background:#e8f5e9;color:#2e7d32;}.erp-tag.completed{background:#f3e5f5;color:#6a1b9a;}.erp-tag.cancelled{background:#fce4ec;color:#c62828;}

.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-input-num{text-align:right;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
