<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索退货单号、订单号..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建退货单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">退货单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联订单</th><th>供应商</th><th>物料明细</th><th style="text-align:right;">数量</th>
          <th>退货原因</th><th>处理方式</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.poCode }}</td><td>{{ row.supplier }}</td>
            <td class="erp-cell-spec">{{ summary(row.items) }}</td>
            <td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td class="erp-cell-spec">{{ row.reason }}</td><td>{{ row.action }}</td>
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

    <!-- 明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'退货明细 - '+detailCode" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">供应商：{{ detailSupplier }} ｜ 关联订单：{{ detailPoCode }} ｜ 退货原因：{{ detailReason }} ｜ 处理方式：{{ detailAction }}</div>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr><th>物料</th><th style="text-align:right;">数量</th><th>单位</th></tr></thead>
          <tbody>
            <tr v-for="(item,idx) in detailItems" :key="idx">
              <td>{{ item.material }}</td><td class="erp-cell-num">{{ item.qty }}</td>
              <td>{{ item.unit }}</td>
            </tr>
            <tr style="font-weight:600;background:#f9fafb;">
              <td>合计</td><td class="erp-cell-num">{{ totalQty(detailItems) }}</td><td></td>
            </tr>
          </tbody>
        </table>
      </div>
    </FormModal>

    <!-- 退货单表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing?'编辑退货单':'新建退货单'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm=false" @save="save"
    >
      <!-- 基本信息 -->
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>退货单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联订单</label><input v-model="f.poCode" placeholder="PO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>供应商 <span class="erp-form-req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="erp-form-group"><label>退货原因</label><input v-model="f.reason" placeholder="如 质量不合格" /></div>
        <div class="erp-form-group"><label>处理方式</label><select v-model="f.action"><option>退货退款</option><option>换货</option><option>让步接收</option></select></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>

      <!-- 退货物料明细区 -->
      <ErpItemTable
        v-model:items="f.items"
        :columns="itemColumns"
        empty-text="暂无物料，请添加"
        add-label="＋ 添加物料"
      />
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除退货单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['待处理','供应商确认中','已退货','已关闭']

interface ReturnItem {
  material: string
  qty: number
  unit: string
}

interface ReturnOrder {
  code: string
  poCode: string
  supplier: string
  items: ReturnItem[]
  reason: string
  action: string
  status: string
  remark: string
  sc: string
}

function totalQty(items: ReturnItem[]) { return items.reduce((s,i) => s + (i.qty||0), 0) }
function summary(items: ReturnItem[]) {
  if (!items.length) return '-'
  return items.length === 1 ? items[0].material : `${items[0].material} 等${items.length}项`
}

const data = ref<ReturnOrder[]>([
  { code:'PRA-001',poCode:'PO-2025-0055',supplier:'宏远钢铁',items:[{material:'Q235槽钢 10#',qty:2,unit:'根'}],reason:'表面锈蚀',action:'换货',status:'供应商确认中',remark:'',sc:'confirming'},
  { code:'PRA-002',poCode:'PO-2025-0056',supplier:'宏远钢铁',items:[{material:'45#圆钢 Φ20',qty:50,unit:'kg'}],reason:'规格不符',action:'退货退款',status:'待处理',remark:'',sc:'pending'},
  { code:'PRA-003',poCode:'PO-2025-0054',supplier:'正泰电气',items:[{material:'电动滚筒 5.5kW',qty:1,unit:'台'},{material:'减速器 SA67',qty:1,unit:'台'}],reason:'运输损坏',action:'换货',status:'已退货',remark:'已退回',sc:'returned'},
])

const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.poCode.toLowerCase().includes(q)||m.supplier.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailCode=ref('');const detailSupplier=ref('');const detailPoCode=ref('');const detailReason=ref('');const detailAction=ref('');const detailItems=ref<ReturnItem[]>([])
function viewItems(row:ReturnOrder){detailCode.value=row.code;detailSupplier.value=row.supplier;detailPoCode.value=row.poCode;detailReason.value=row.reason;detailAction.value=row.action;detailItems.value=[...row.items];showDetail.value=true}

// 表单
// ---- 物品表格列配置 ----
const itemColumns = [
  { key: 'material', label: '物料名称', type: 'autocomplete' as const, required: true, placeholder: '搜索或输入物料名' },
  { key: 'qty', label: '数量', type: 'number' as const, align: 'right' as const, min: 1, total: true },
  { key: 'unit', label: '单位', type: 'select' as const, options: ['个', '件', 'kg', '根'] },
]

const numberingMode=ref('auto')
const showForm=ref(false);const editing=ref(false)
const f=reactive<{code:string;poCode:string;supplier:string;items:ReturnItem[];reason:string;action:string;status:string;remark:string}>({code:'',poCode:'',supplier:'',items:[],reason:'',action:'退货退款',status:'待处理',remark:''})
let ec=''

function openForm(item?:ReturnOrder){
  if(item){
    editing.value=true;ec=item.code
    Object.assign(f,{...item,items:item.items.map(i=>({...i}))})
  }else{
    editing.value=false;numberingMode.value='auto'
    f.code=`PRA-${String(data.value.length+1).padStart(3,'0')}`
    f.poCode='';f.supplier='';f.items=[];f.reason='';f.action='退货退款';f.status='待处理';f.remark=''
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

const showDel=ref(false);const dt=ref<ReturnOrder|null>(null)
function confirmDel(item:ReturnOrder){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>

<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}.erp-tag.confirming{background:#e3f2fd;color:#1565c0;}
.erp-tag.returned{background:#e8f5e9;color:#2e7d32;}.erp-tag.closed{background:#f5f5f5;color:#999;}

.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-input-num{text-align:right;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
