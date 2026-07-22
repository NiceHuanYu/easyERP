<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索收款/付款单..." class="erp-search-input"/></div><select v-model="ft" class="erp-filter-select"><option value="">全部方向</option><option>收款</option><option>付款</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建收付款</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>方向</th><th>客户/供应商</th><th style="text-align:right;">金额(元)</th><th>方式</th><th>账户</th><th>日期</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td><span :class="['erp-tag',r.dir==='收款'?'in':'out']">{{ r.dir }}</span></td><td>{{ r.partner }}</td><td class="erp-cell-num">{{ r.amount.toFixed(2) }}</td><td>{{ r.method }}</td><td>{{ r.account }}</td><td class="erp-cell-spec">{{ r.date }}</td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建收付款" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid"><div class="erp-form-group"><label>单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div><div class="erp-form-group"><label>方向</label><select v-model="f.dir"><option>收款</option><option>付款</option></select></div>
<div class="erp-form-group"><label>客户/供应商</label><input v-model="f.partner"/></div><div class="erp-form-group"><label>金额</label><input v-model.number="f.amount" type="number" step="0.01"/></div>
<div class="erp-form-group"><label>方式</label><select v-model="f.method"><option>银行转账</option><option>现金</option><option>承兑汇票</option><option>支票</option></select></div>
<div class="erp-form-group"><label>账户</label><input v-model="f.account" placeholder="银行/现金账户"/></div><div class="erp-form-group"><label>日期</label><input v-model="f.date" type="text"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>已完成</option><option>待确认</option></select></div><div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'PMT-001',dir:'收款',partner:'华强电子',amount:37200,method:'银行转账',account:'工行-基本户',date:'2025-07-28',status:'已完成',remark:'',sc:'done'},
  { code:'PMT-002',dir:'收款',partner:'精密模具',amount:10000,method:'银行转账',account:'工行-基本户',date:'2025-08-01',status:'已完成',remark:'',sc:'done'},
  { code:'PMT-003',dir:'付款',partner:'宏远钢铁',amount:3104,method:'银行转账',account:'工行-基本户',date:'2025-07-26',status:'已完成',remark:'',sc:'done'},
  { code:'PMT-004',dir:'付款',partner:'东明标准件',amount:900,method:'现金',account:'备用金',date:'2025-07-20',status:'已完成',remark:'',sc:'done'},
  { code:'PMT-005',dir:'收款',partner:'华强电子',amount:14250,method:'银行转账',account:'工行-基本户',date:'',status:'待确认',remark:'',sc:'pending'},
])
const s=ref('');const ft=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.partner.includes(q));if(ft.value)l=l.filter(m=>m.dir===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',dir:'收款',partner:'',amount:0,method:'银行转账',account:'',date:'',status:'已完成',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`PMT-${String(data.value.length+1).padStart(3,'0')}`;f.dir='收款';f.partner='';f.amount=0;f.method='银行转账';f.account='';f.date='';f.status='已完成';f.remark=''}showForm.value=true}
function save(){if(!f.partner){alert('请填写客户/供应商');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-tag.in{background:#e8f5e9;color:#2e7d32;}.erp-tag.out{background:#fce4ec;color:#c62828;}.erp-tag.done{background:#e8f5e9;color:#2e7d32;}.erp-tag.pending{background:#fff3e0;color:#e65100;}</style>