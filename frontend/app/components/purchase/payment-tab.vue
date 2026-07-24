<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索付款单号、供应商..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建付款单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">付款单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('supplier')" class="sortable">供应商{{ sf==='supplier'?(sa?'▲':'▼'):'' }}</th>
          <th>关联对账单</th><th style="text-align:right;">付款金额(元)</th><th>付款方式</th><th>付款日期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.supplier }}</td>
            <td class="erp-cell-spec">{{ row.stmtCode }}</td><td class="erp-cell-num">{{ row.amount.toFixed(2) }}</td>
            <td>{{ row.method }}</td><td class="erp-cell-spec">{{ row.payDate }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button v-if="row.status==='草稿'" class="erp-lnk" style="color:#2e7d32;" @click="submit(row)">提交</button><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑付款单':'新建付款单'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>付款单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>供应商 <span class="erp-form-req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="erp-form-group"><label>关联对账单</label><input v-model="f.stmtCode" placeholder="STMT-xxx" /></div>
        <div class="erp-form-group"><label>付款金额 (元)</label><input v-model.number="f.amount" type="number" step="0.01" min="0" /></div>
        <div class="erp-form-group"><label>付款方式</label><select v-model="f.method"><option>银行转账</option><option>银行承兑</option><option>现金</option><option>支票</option></select></div>
        <div class="erp-form-group"><label>付款日期</label><input v-model="f.payDate" type="text" placeholder="2025-07-30" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除付款单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审批','已批准','已付款','已退回']
const data = ref([
  { code:'PAY-001',supplier:'宏远钢铁',stmtCode:'STMT-001',amount:704,method:'银行转账',payDate:'2025-08-05',status:'待审批',remark:'',sc:'pending'},
  { code:'PAY-002',supplier:'宏远钢铁',stmtCode:'STMT-005',amount:9800,method:'银行承兑',payDate:'2025-07-20',status:'已付款',remark:'6月货款',sc:'paid'},
  { code:'PAY-003',supplier:'东明标准件',stmtCode:'STMT-003',amount:4500,method:'银行转账',payDate:'2025-07-15',status:'已付款',remark:'',sc:'paid'},
  { code:'PAY-004',supplier:'宏达橡塑',stmtCode:'STMT-004',amount:180,method:'现金',payDate:'2025-07-10',status:'已付款',remark:'',sc:'paid'},
  { code:'PAY-005',supplier:'正泰电气',stmtCode:'STMT-002',amount:15600,method:'银行转账',payDate:'2025-08-10',status:'待审批',remark:'',sc:'pending'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.supplier.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',supplier:'',stmtCode:'',amount:0,method:'银行转账',payDate:'',status:'待审批',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`PAY-${String(data.value.length+1).padStart(3,'0')}`;f.supplier='';f.stmtCode='';f.amount=0;f.method='银行转账';f.payDate='';f.status='待审批';f.remark=''}showForm.value=true}
function save(){if(!f.supplier||f.amount<=0){alert('请填写供应商和付款金额');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
function submit(row:any){const i=data.value.findIndex(m=>m.code===row.code);if(i!==-1){data.value[i].status='待审批';data.value[i].sc='pending'}}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}.erp-tag.approved{background:#e3f2fd;color:#1565c0;}
.erp-tag.paid{background:#e8f5e9;color:#2e7d32;}.erp-tag.refunded{background:#fce4ec;color:#c62828;}.erp-tag.draft{background:#f5f5f5;color:#999;}
</style>
