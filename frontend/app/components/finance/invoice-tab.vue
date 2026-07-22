<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索发票号、客户..." class="erp-search-input"/></div><select v-model="ft" class="erp-filter-select"><option value="">全部方向</option><option>销售发票</option><option>采购发票</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建发票</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">发票号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>方向</th><th>客户/供应商</th><th style="text-align:right;">金额(元)</th><th>税率</th><th style="text-align:right;">税额</th><th>开票日期</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td><span class="erp-tag">{{ r.dir }}</span></td><td>{{ r.partner }}</td><td class="erp-cell-num">{{ r.amount.toFixed(2) }}</td><td>{{ r.taxRate }}</td><td class="erp-cell-num">{{ r.tax.toFixed(2) }}</td><td class="erp-cell-spec">{{ r.date }}</td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建发票" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>发票号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div><div class="erp-form-group"><label>方向</label><select v-model="f.dir"><option>销售发票</option><option>采购发票</option></select></div>
<div class="erp-form-group"><label>客户/供应商</label><input v-model="f.partner"/></div><div class="erp-form-group"><label>金额</label><input v-model.number="f.amount" type="number" step="0.01"/></div>
<div class="erp-form-group"><label>税率</label><select v-model="f.taxRate"><option>13%</option><option>9%</option><option>6%</option><option>3%</option></select></div>
<div class="erp-form-group"><label>税额</label><input v-model.number="f.tax" type="number" step="0.01"/></div>
<div class="erp-form-group"><label>开票日期</label><input v-model="f.date" type="text"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>待开票</option><option>已开票</option><option>已认证</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'INV-001',dir:'销售发票',partner:'华强电子',amount:37200,taxRate:'13%',tax:4836,date:'2025-07-26',status:'已开票',remark:'',sc:'issued'},
  { code:'INV-002',dir:'销售发票',partner:'鑫达科技',amount:32500,taxRate:'13%',tax:4225,date:'2025-07-28',status:'已开票',remark:'',sc:'issued'},
  { code:'INV-003',dir:'采购发票',partner:'宏远钢铁',amount:3104,taxRate:'13%',tax:403.52,date:'2025-07-26',status:'已认证',remark:'',sc:'verified'},
  { code:'INV-004',dir:'采购发票',partner:'正泰电气',amount:15600,taxRate:'13%',tax:2028,date:'',status:'待开票',remark:'',sc:'pending'},
])
const s=ref('');const ft=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.partner.includes(q));if(ft.value)l=l.filter(m=>m.dir===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',dir:'销售发票',partner:'',amount:0,taxRate:'13%',tax:0,date:'',status:'待开票',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`INV-${String(data.value.length+1).padStart(3,'0')}`;f.dir='销售发票';f.partner='';f.amount=0;f.taxRate='13%';f.tax=0;f.date='';f.status='待开票';f.remark=''}showForm.value=true}
function save(){if(!f.partner){alert('请填写客户/供应商');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-tag{background:#e8f0fe;color:#1a73e8;}.erp-tag.issued{background:#e3f2fd;color:#1565c0;}.erp-tag.verified{background:#e8f5e9;color:#2e7d32;}.erp-tag.pending{background:#fff3e0;color:#e65100;}</style>