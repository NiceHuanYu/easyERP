<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索对账单号、供应商..." class="search-input" /></div>
        <select v-model="fs" class="filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建对账单</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">对账单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('supplier')" class="sortable">供应商{{ sf==='supplier'?(sa?'▲':'▼'):'' }}</th>
          <th>期间</th><th style="text-align:right;">采购金额</th><th style="text-align:right;">已付金额</th><th style="text-align:right;">未付金额</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="code">{{ row.code }}</td><td>{{ row.supplier }}</td>
            <td class="spec">{{ row.period }}</td><td class="num">{{ row.total.toFixed(2) }}</td>
            <td class="num">{{ row.paid.toFixed(2) }}</td>
            <td class="num" :class="row.balance>0?'unpaid':''">{{ row.balance.toFixed(2) }}</td>
            <td><span :class="['tag', row.sc]">{{ row.status }}</span></td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑对账单':'新建对账单'" @close="showForm=false" @save="save">
      <div class="grid">
        <div class="fg"><label>对账单号</label><input v-model="f.code" :disabled="!editing" /></div>
        <div class="fg"><label>供应商 <span class="req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="fg"><label>对账期间</label><input v-model="f.period" type="text" placeholder="2025年7月" /></div>
        <div class="fg"><label>采购金额</label><input v-model.number="f.total" type="number" step="0.01" min="0" /></div>
        <div class="fg"><label>已付金额</label><input v-model.number="f.paid" type="number" step="0.01" min="0" /></div>
        <div class="fg"><label>未付金额</label><input v-model.number="f.balance" type="number" step="0.01" min="0" :disabled="true" /></div>
        <div class="fg"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
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
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;f.code=`STMT-${String(data.value.length+1).padStart(3,'0')}`;f.supplier='';f.period='';f.total=0;f.paid=0;f.balance=0;f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.supplier){alert('请填写供应商');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}.search-input::placeholder{color:#bbb;}
.filter-select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:8px;background:#fafafa;font-size:13px;color:#555;outline:none;cursor:pointer;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}
.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.num.unpaid{color:#d32f2f;font-weight:600;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}.tag.draft{background:#f5f5f5;color:#999;}.tag.confirmed{background:#e3f2fd;color:#1565c0;}
.tag.paid{background:#e8f5e9;color:#2e7d32;}.tag.closed{background:#fce4ec;color:#c62828;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;cursor:not-allowed;}.fg textarea{resize:vertical;font-family:inherit;}
</style>
