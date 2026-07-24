<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索发货单号、订单号..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建发货单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">发货单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联订单</th><th>客户</th><th>产品明细</th><th style="text-align:right;">总数量</th>
          <th>物流公司</th><th>物流单号</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.soCode }}</td>
            <td>{{ row.customer }}</td><td class="erp-cell-spec">{{ summary(row.items) }}</td><td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td>{{ row.logistics }}</td><td class="erp-cell-spec">{{ row.trackingNo }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <!-- 发货明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'发货明细 - '+detailCode" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">客户：{{ detailCustomer }} ｜ 物流：{{ detailLogistics }} {{ detailTrackingNo }}</div>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr><th>物料/产品</th><th style="text-align:right;">数量</th><th>单位</th></tr></thead>
          <tbody>
            <tr v-for="(item,idx) in detailItems" :key="idx">
              <td>{{ item.material }}</td><td class="erp-cell-num">{{ item.qty }}</td><td>{{ item.unit }}</td>
            </tr>
            <tr style="font-weight:600;background:#f9fafb;">
              <td>合计</td><td class="erp-cell-num">{{ totalQty(detailItems) }}</td><td></td>
            </tr>
          </tbody>
        </table>
      </div>
    </FormModal>

    <!-- 发货表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing?'编辑发货单':'新建发货单'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm=false" @save="save"
    >
      <!-- 基本信息 -->
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>发货单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联订单号</label><input v-model="f.soCode" placeholder="SO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>客户 <span class="erp-form-req">*</span></label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="erp-form-group"><label>物流公司</label><input v-model="f.logistics" placeholder="如 顺丰速运" /></div>
        <div class="erp-form-group"><label>物流单号</label><input v-model="f.trackingNo" placeholder="快递单号" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>

      <!-- 产品明细区 -->
      <ErpItemTable
        v-model:items="f.items"
        :columns="itemColumns"
        empty-text="暂无产品，请添加"
        add-label="＋ 添加产品"
      />
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除发货单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['待发货','已发货','已签收']

interface ShipmentItem {
  material: string
  qty: number
  unit: string
}

interface Shipment {
  code: string
  soCode: string
  customer: string
  items: ShipmentItem[]
  logistics: string
  trackingNo: string
  status: string
  remark: string
  sc: string
}

function totalQty(items: ShipmentItem[]) { return items.reduce((s,i) => s + (i.qty||0), 0) }
function summary(items: ShipmentItem[]) {
  if (!items.length) return '-'
  return items.length === 1 ? items[0].material : `${items[0].material} 等${items.length}项`
}

const data = ref<Shipment[]>([
  { code:'SHP-001',soCode:'SO-2025-0087',customer:'鑫达科技',items:[{material:'皮带输送机 10m',qty:1,unit:'套'}],logistics:'德邦物流',trackingNo:'DB1234567890',status:'已发货',remark:'',sc:'shipped'},
  { code:'SHP-002',soCode:'SO-2025-0083',customer:'宏远机械',items:[{material:'皮带输送机 10m',qty:1,unit:'套'}],logistics:'',trackingNo:'',status:'待发货',remark:'',sc:'pending'},
  { code:'SHP-003',soCode:'SO-2025-0088',customer:'精密模具',items:[{material:'电动滚筒 5.5kW',qty:2,unit:'台'},{material:'减速器 SA67',qty:1,unit:'台'}],logistics:'顺丰速运',trackingNo:'SF9876543210',status:'已签收',remark:'已确认',sc:'received'},
  { code:'SHP-004',soCode:'SO-2025-0086',customer:'华强电子',items:[{material:'螺旋输送机 LS400',qty:1,unit:'台'}],logistics:'',trackingNo:'',status:'待发货',remark:'等待生产完工',sc:'pending'},
])

const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q)||m.soCode.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailCode=ref('');const detailCustomer=ref('');const detailLogistics=ref('');const detailTrackingNo=ref('');const detailItems=ref<ShipmentItem[]>([])
function viewItems(row:Shipment){detailCode.value=row.code;detailCustomer.value=row.customer;detailLogistics.value=row.logistics;detailTrackingNo.value=row.trackingNo;detailItems.value=[...row.items];showDetail.value=true}

// 表单
// ---- 物品表格列配置 ----
const itemColumns = [
  { key: 'material', label: '物料/产品', type: 'autocomplete' as const, required: true, placeholder: '搜索或输入产品名' },
  { key: 'qty', label: '数量', type: 'number' as const, align: 'right' as const, min: 1, total: true },
  { key: 'unit', label: '单位', type: 'select' as const, options: ['台', '套', '件', '个'] },
]

const numberingMode=ref('auto')
const showForm=ref(false);const editing=ref(false)
const f=reactive<{code:string;soCode:string;customer:string;items:ShipmentItem[];logistics:string;trackingNo:string;status:string;remark:string}>({code:'',soCode:'',customer:'',items:[],logistics:'',trackingNo:'',status:'待发货',remark:''})
let ec=''

function openForm(item?:Shipment){
  if(item){
    editing.value=true;ec=item.code
    Object.assign(f,{...item,items:item.items.map(i=>({...i}))})
  }else{
    editing.value=false;numberingMode.value='auto'
    f.code=`SHP-${String(data.value.length+1).padStart(3,'0')}`
    f.soCode='';f.customer='';f.items=[];f.logistics='';f.trackingNo='';f.status='待发货';f.remark=''
  }
  showForm.value=true
}

function save(){
  if(!f.customer){alert('请填写客户');return}
  if(!f.items.length||f.items.some(i=>!i.material)){alert('请填写产品明细');return}
  const copy={...f,items:f.items.map(i=>({...i}))}
  if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]=copy as any}
  else data.value.push(copy as any)
  showForm.value=false
}

const showDel=ref(false);const dt=ref<Shipment|null>(null)
function confirmDel(item:Shipment){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>

<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.shipped{background:#e3f2fd;color:#1565c0;}
.erp-tag.received{background:#e8f5e9;color:#2e7d32;}

/* 表格内输入框 */
.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-input-num{text-align:right;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
