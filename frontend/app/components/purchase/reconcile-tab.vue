<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索对账单号、供应商..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建对账单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">对账单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('supplier')" class="sortable">供应商{{ sf==='supplier'?(sa?'▲':'▼'):'' }}</th>
          <th>期间</th><th style="text-align:right;">采购金额</th><th style="text-align:right;">已付金额</th><th style="text-align:right;">未付金额</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.supplier }}</td>
            <td class="erp-cell-spec">{{ row.period }}</td><td class="erp-cell-num">{{ row.total.toFixed(2) }}</td>
            <td class="erp-cell-num">{{ row.paid.toFixed(2) }}</td>
            <td class="erp-cell-num" :class="row.balance>0?'unpaid':''">{{ row.balance.toFixed(2) }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑对账单':'新建对账单'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>对账单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>供应商 <span class="erp-form-req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="erp-form-group"><label>对账期间</label><input v-model="f.period" type="text" placeholder="2025年7月" /></div>
        <div class="erp-form-group"><label>采购金额</label><input v-model.number="f.total" type="number" step="0.01" min="0" /></div>
        <div class="erp-form-group"><label>已付金额</label><input v-model.number="f.paid" type="number" step="0.01" min="0" /></div>
        <div class="erp-form-group"><label>未付金额</label><input v-model.number="f.balance" type="number" step="0.01" min="0" :disabled="true" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除对账单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','已确认','已付款','已关闭']
const data = ref([
  { code:'STMT-001',supplier:'宏远钢铁',period:'2025年7月',total:3104,paid:2400,balance:704,status:'已确认',remark:'',sc:'confirmed'},
  { code:'STMT-002',supplier:'正泰电气',period:'2025年7月',total:15600,paid:0,balance:15600,status:'草稿',remark:'待审批',sc:'draft'},
  { code:'STMT-003',supplier:'东明标准件',period:'2025年6月',total:4500,paid:4500,balance:0,status:'已付款',remark:'',sc:'paid'},
  { code:'STMT-004',supplier:'宏达橡塑',period:'2025年7月',total:180,paid:180,balance:0,status:'已关闭',remark:'',sc:'closed'},
  { code:'STMT-005',supplier:'宏远钢铁',period:'2025年6月',total:9800,paid:9800,balance:0,status:'已付款',remark:'',sc:'paid'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.supplier.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',supplier:'',period:'',total:0,paid:0,balance:0,status:'草稿',remark:''});let ec=''
watch(()=>f.total,()=>{if(!editing.value)f.balance=f.total-f.paid})
watch(()=>f.paid,()=>{if(!editing.value)f.balance=f.total-f.paid})
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`STMT-${String(data.value.length+1).padStart(3,'0')}`;f.supplier='';f.period='';f.total=0;f.paid=0;f.balance=0;f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.supplier){alert('请填写供应商');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-cell-num.unpaid{color:#d32f2f;font-weight:600;}
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.confirmed{background:#e3f2fd;color:#1565c0;}
.erp-tag.paid{background:#e8f5e9;color:#2e7d32;}.erp-tag.closed{background:#fce4ec;color:#c62828;}
</style>
