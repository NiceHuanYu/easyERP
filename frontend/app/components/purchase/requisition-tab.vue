<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索申请单号、物料..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建申请</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">申请单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>物料明细</th><th style="text-align:right;">总数量</th><th>需求部门</th><th>期望交期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td>
            <td class="erp-cell-spec">{{ summary(row.items) }}</td>
            <td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td>{{ row.dept }}</td><td class="erp-cell-spec">{{ row.expectDate }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
              <button class="erp-lnk" style="color:#2e7d32;" v-if="row.status==='草稿'" @click="submit(row)">提交</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <!-- 明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'申请明细 - '+detailCode" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">需求部门：{{ detailDept }} ｜ 期望交期：{{ detailExpectDate }}</div>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr><th>物料名称</th><th style="text-align:right;">数量</th><th>单位</th></tr></thead>
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

    <!-- 申请表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing?'编辑申请':'新建申请'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm=false" @save="save"
    >
      <!-- 基本信息 -->
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>申请单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>需求部门 <span class="erp-form-req">*</span></label><select v-model="f.dept"><option>生产部</option><option>品质部</option><option>设备部</option><option>仓库</option></select></div>
        <div class="erp-form-group"><label>期望交期</label><input v-model="f.expectDate" type="text" placeholder="2025-08-15" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="用途说明等"></textarea></div>
      </div>

      <!-- 物料明细区 -->
      <FormSection title="物料明细" hint="添加申请物料">
        <div class="erp-table-wrap">
          <table class="erp-table">
            <thead><tr>
              <th>物料名称 <span class="erp-form-req">*</span></th><th style="text-align:right;">数量</th><th>单位</th><th style="text-align:center;">操作</th>
            </tr></thead>
            <tbody>
              <tr v-for="(item,idx) in f.items" :key="idx">
                <td><input v-model="item.material" placeholder="物料名称" class="erp-tbl-input" /></td>
                <td><input v-model.number="item.qty" type="number" min="1" placeholder="1" class="erp-tbl-input erp-tbl-input-num" /></td>
                <td><select v-model="item.unit" class="erp-tbl-select"><option>个</option><option>件</option><option>kg</option><option>张</option><option>根</option><option>桶</option></select></td>
                <td class="erp-cell-acts"><button class="erp-lnk erp-lnk-danger" @click="f.items.splice(idx,1)">删除</button></td>
              </tr>
              <tr v-if="f.items.length===0"><td colspan="4" class="erp-cell-empty">暂无物料，请添加</td></tr>
            </tbody>
          </table>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;margin-top:10px;">
          <button class="erp-btn erp-btn-primary" style="padding:6px 16px;font-size:12px;" @click="addItem()">＋ 添加物料</button>
          <span style="font-size:13px;color:#555;">合计数量：<strong style="color:#1a73e8;">{{ totalQty(f.items) }}</strong></span>
        </div>
      </FormSection>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除申请 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审批','已批准','已采购','已关闭']

interface RequisitionItem {
  material: string
  qty: number
  unit: string
}

interface Requisition {
  code: string
  items: RequisitionItem[]
  dept: string
  expectDate: string
  status: string
  remark: string
  sc: string
}

function totalQty(items: RequisitionItem[]) { return items.reduce((s,i) => s + (i.qty||0), 0) }
function summary(items: RequisitionItem[]) {
  if (!items.length) return '-'
  return items.length === 1 ? items[0].material : `${items[0].material} 等${items.length}项`
}

const data = ref<Requisition[]>([
  { code:'PR-001',items:[{material:'45#圆钢 Φ20',qty:500,unit:'kg'}],dept:'生产部',expectDate:'2025-08-10',status:'待审批',remark:'',sc:'pending'},
  { code:'PR-002',items:[{material:'304不锈钢板 2mm',qty:20,unit:'张'},{material:'焊丝 ER50-6 Φ1.2',qty:10,unit:'盘'}],dept:'生产部',expectDate:'2025-08-05',status:'已批准',remark:'',sc:'approved'},
  { code:'PR-003',items:[{material:'M8×30六角螺栓',qty:2000,unit:'个'}],dept:'生产部',expectDate:'2025-08-15',status:'草稿',remark:'库存补充',sc:'draft'},
  { code:'PR-004',items:[{material:'O型密封圈 Φ50×3.5',qty:1000,unit:'只'}],dept:'品质部',expectDate:'2025-08-20',status:'待审批',remark:'',sc:'pending'},
  { code:'PR-005',items:[{material:'乳化切削液 18L',qty:5,unit:'桶'}],dept:'设备部',expectDate:'2025-08-01',status:'已采购',remark:'',sc:'ordered'},
  { code:'PR-006',items:[{material:'焊丝 ER50-6 Φ1.2',qty:10,unit:'盘'}],dept:'生产部',expectDate:'2025-08-12',status:'已关闭',remark:'',sc:'closed'},
])

const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailCode=ref('');const detailDept=ref('');const detailExpectDate=ref('');const detailItems=ref<RequisitionItem[]>([])
function viewItems(row:Requisition){detailCode.value=row.code;detailDept.value=row.dept;detailExpectDate.value=row.expectDate;detailItems.value=[...row.items];showDetail.value=true}

// 表单
const numberingMode=ref('auto')
const showForm=ref(false);const editing=ref(false)
const f=reactive<{code:string;items:RequisitionItem[];dept:string;expectDate:string;status:string;remark:string}>({code:'',items:[],dept:'生产部',expectDate:'',status:'草稿',remark:''})
let ec=''

function addItem() { f.items.push({material:'',qty:1,unit:'个'}) }

function openForm(item?:Requisition){
  if(item){
    editing.value=true;ec=item.code
    Object.assign(f,{...item,items:item.items.map(i=>({...i}))})
  }else{
    editing.value=false;numberingMode.value='auto'
    f.code=`PR-${String(data.value.length+1).padStart(3,'0')}`
    f.items=[{material:'',qty:1,unit:'个'}];f.dept='生产部';f.expectDate='';f.status='草稿';f.remark=''
  }
  showForm.value=true
}

function save(){
  if(!f.dept){alert('请选择需求部门');return}
  if(!f.items.length||f.items.some(i=>!i.material)){alert('请填写物料明细');return}
  const copy={...f,items:f.items.map(i=>({...i}))}
  if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]=copy as any}
  else data.value.push(copy as any)
  showForm.value=false
}

const showDel=ref(false);const dt=ref<Requisition|null>(null)
function confirmDel(item:Requisition){dt.value=item;showDel.value=true}
function submit(item:Requisition){item.status='待审批';item.sc='pending'}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>

<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.pending{background:#fff3e0;color:#e65100;}.erp-tag.approved{background:#e8f5e9;color:#2e7d32;}.erp-tag.ordered{background:#e3f2fd;color:#1565c0;}.erp-tag.closed{background:#fce4ec;color:#c62828;}

.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-input-num{text-align:right;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
