<template>
  <div class="erp-tab-body">
    <!-- 进度栏：点击步骤筛选 -->
    <StepProgressBar v-model="progressFilter" :steps="progressSteps" />

    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索询价编号、客户..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in allStatuses" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建询价</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">询价编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('customer')" class="sortable">客户名称{{ sf==='customer'?(sa?'▲':'▼'):'' }}</th>
          <th>联系人</th><th>业务员</th><th>产品明细</th><th style="text-align:right;">总数量</th>
          <th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.customer }}</td>
            <td>{{ row.contact }}</td><td>{{ row.salesman }}</td><td class="erp-cell-spec">{{ summary(row.items) }}</td>
            <td class="erp-cell-num">{{ totalQty(row.items) }}</td>
            <td><span :class="['erp-tag', row.statusClass]">{{ row.status }}</span></td>
            <td class="erp-cell-acts">
              <template v-if="canAdvance(row.status)">
                <button class="erp-lnk" @click="nextStep(row)">下一步</button>
                <button class="erp-lnk erp-lnk-danger" @click="rejectItem(row)">拒绝</button>
              </template>
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
            </td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <!-- 询价明细查看弹窗 -->
    <FormModal :show="showDetail" :title="'询价明细 - '+detailCode" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:8px;font-size:13px;color:#888;">客户：{{ detailCustomer }} ｜ 联系人：{{ detailContact }} ｜ 业务员：{{ detailSalesman }}</div>
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

    <!-- 询价表单弹窗 -->
    <FormModal
      :show="showForm" :title="editing?'编辑询价':'新建询价'"
      size="lg"
      :showNumbering="!editing"
      v-model:numberingMode="numberingMode"
      @close="showForm=false" @save="save"
    >
      <!-- 基本信息 -->
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>询价编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>客户名称 <span class="erp-form-req">*</span></label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="erp-form-group"><label>联系人</label><input v-model="f.contact" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>业务员</label><input v-model="f.salesman" placeholder="业务员姓名" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in allStatuses" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="客户特殊要求等"></textarea></div>
      </div>

      <!-- 产品明细区 -->
      <ErpItemTable
        v-model:items="f.items"
        :columns="itemColumns"
        empty-text="暂无产品，请添加"
        add-label="＋ 添加产品"
      />
    </FormModal>

    <ConfirmDialog :show="showReject" title="确认拒绝" @confirm="doReject" @cancel="showReject=false">
      <p>确定要拒绝询价 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const progressSteps = ['待回复', '已报价', '已完成']
const allStatuses = ['待回复','已报价','已完成','已拒绝','已关闭']

// 状态流转映射
const nextStatus: Record<string, string> = {
  '待回复': '已报价',
  '已报价': '已完成',
}
const statusClassMap: Record<string, string> = {
  '待回复': 'pending',
  '已报价': 'quoted',
  '已完成': 'completed',
  '已拒绝': 'rejected',
  '已关闭': 'closed',
}

function canAdvance(status: string) {
  return status in nextStatus
}

interface InquiryItem {
  material: string
  qty: number
  unit: string
}

interface Inquiry {
  code: string
  customer: string
  contact: string
  salesman: string
  items: InquiryItem[]
  status: string
  remark: string
  statusClass: string
}

function totalQty(items: InquiryItem[]) { return items.reduce((s,i) => s + (i.qty||0), 0) }
function summary(items: InquiryItem[]) {
  if (!items.length) return '-'
  return items.length === 1 ? items[0].material : `${items[0].material} 等${items.length}项`
}

const data = ref<Inquiry[]>([
  { code:'INQ-001',customer:'华强电子',contact:'李明',salesman:'陈刚',items:[{material:'减速器 SA67',qty:5,unit:'台'}],status:'待回复',remark:'',statusClass:'pending' },
  { code:'INQ-002',customer:'鑫达精密',contact:'王芳',salesman:'陈刚',items:[{material:'电动滚筒 5.5kW',qty:3,unit:'台'}],status:'待回复',remark:'加急',statusClass:'pending' },
  { code:'INQ-003',customer:'宏远机械',contact:'张伟',salesman:'刘洋',items:[{material:'皮带输送机 10m',qty:2,unit:'套'}],status:'已报价',remark:'',statusClass:'quoted' },
  { code:'INQ-004',customer:'丰华重型',contact:'赵磊',salesman:'刘洋',items:[{material:'螺旋输送机 LS400',qty:1,unit:'套'}],status:'已报价',remark:'',statusClass:'quoted' },
  { code:'INQ-005',customer:'天工精密',contact:'刘强',salesman:'陈刚',items:[{material:'振动电机 YZO-8-4',qty:10,unit:'台'}],status:'已完成',remark:'已转订单',statusClass:'completed' },
  { code:'INQ-006',customer:'利达工业',contact:'周亮',salesman:'刘洋',items:[{material:'刮板输送机 SGZ630',qty:1,unit:'套'}],status:'已拒绝',remark:'价格不合适',statusClass:'rejected' },
  { code:'INQ-007',customer:'荣盛五金',contact:'孙健',salesman:'陈刚',items:[{material:'电机配件 Y2-132',qty:20,unit:'件'}],status:'已关闭',remark:'客户取消',statusClass:'closed' },
])

// 进度栏筛选
const progressFilter = ref<string | null>(null)

// 搜索、排序、分页
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{
  let l=[...data.value]
  const q=s.value.trim().toLowerCase()
  if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q))
  // 进度栏筛选优先，否则用下拉筛选
  if(progressFilter.value) l=l.filter(m=>m.status===progressFilter.value)
  else if(fs.value) l=l.filter(m=>m.status===fs.value)
  l.sort((a,b)=>{
    const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b]
    if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av
    return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))
  })
  return l
})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs,progressFilter],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailCode=ref('');const detailCustomer=ref('');const detailContact=ref('');const detailSalesman=ref('');const detailItems=ref<InquiryItem[]>([])
function viewItems(row:Inquiry){detailCode.value=row.code;detailCustomer.value=row.customer;detailContact.value=row.contact;detailSalesman.value=row.salesman;detailItems.value=[...row.items];showDetail.value=true}

// ---- 物品表格列配置 ----
const itemColumns = [
  { key: 'material', label: '物料/产品', type: 'autocomplete' as const, required: true, placeholder: '搜索或输入产品名' },
  { key: 'qty', label: '数量', type: 'number' as const, align: 'right' as const, min: 1, total: true },
  { key: 'unit', label: '单位', type: 'select' as const, options: ['台', '套', '件', '个', 'kg'] },
]

// 表单
const showForm=ref(false);const editing=ref(false);const numberingMode=ref('auto')
const f=reactive<{code:string;customer:string;contact:string;salesman:string;items:InquiryItem[];status:string;remark:string}>({code:'',customer:'',contact:'',salesman:'',items:[],status:'待回复',remark:''})
let ec=''

function openForm(item?:Inquiry){
  if(item){
    editing.value=true;ec=item.code
    Object.assign(f,{...item,items:item.items.map(i=>({...i}))})
  }else{
    editing.value=false;numberingMode.value='auto'
    f.code=`INQ-${String(data.value.length+1).padStart(3,'0')}`
    f.customer='';f.contact='';f.salesman='';f.items=[];f.status='待回复';f.remark=''
  }
  showForm.value=true
}

function save(){
  if(!f.customer){alert('请填写客户');return}
  if(!f.items.length||f.items.some(i=>!i.material)){alert('请填写询价产品明细');return}
  const cls=statusClassMap[f.status]||''
  const copy={...f,items:f.items.map(i=>({...i})),statusClass:cls}
  if(editing.value){
    const i=data.value.findIndex(m=>m.code===ec)
    if(i!==-1)data.value[i]=copy as any
  }else{
    data.value.push(copy as any)
  }
  showForm.value=false
}

// 状态流转
function nextStep(item: any) {
  const ns = nextStatus[item.status]
  if (ns) {
    item.status = ns
    item.statusClass = statusClassMap[ns]
  }
}

const showReject=ref(false);const dt=ref<any>(null)
function rejectItem(item:any){dt.value=item;showReject.value=true}
function doReject(){
  if(dt.value){
    dt.value.status='已拒绝'
    dt.value.statusClass='rejected'
  }
  showReject.value=false;dt.value=null
}
</script>

<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.quoted{background:#e8f5e9;color:#2e7d32;}
.erp-tag.completed{background:#e3f2fd;color:#1565c0;}
.erp-tag.rejected{background:#fce4ec;color:#c62828;}
.erp-tag.closed{background:#f5f5f5;color:#999;}

/* 表格内输入框 */
.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-input-num{text-align:right;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
