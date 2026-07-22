<template><div class="erp-tab-body">
<div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索盘点单号..." class="erp-search-input"/></div><select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option>草稿</option><option>盘点中</option><option>已完成</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建盘点</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">盘点单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>仓库</th><th style="text-align:right;">应盘数</th><th style="text-align:right;">已盘数</th><th style="text-align:right;">差异数</th><th>盘点日期</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.wh }}</td><td class="erp-cell-num">{{ r.total }}</td><td class="erp-cell-num">{{ r.done }}</td><td class="erp-cell-num" :class="r.diff!==0?'diff':''">{{ r.diff>0?'+':'' }}{{ r.diff }}</td><td class="erp-cell-spec">{{ r.date }}</td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建盘点" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>盘点单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="erp-form-group"><label>仓库 <span class="erp-form-req">*</span></label><input v-model="f.wh"/></div>
<div class="erp-form-group"><label>应盘物料数</label><input v-model.number="f.total" type="number" min="1"/></div>
<div class="erp-form-group"><label>已盘物料数</label><input v-model.number="f.done" type="number" min="0"/></div>
<div class="erp-form-group"><label>盘点日期</label><input v-model="f.date" type="text"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>草稿</option><option>盘点中</option><option>已完成</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'CT-001',wh:'原材料主库',total:86,done:86,diff:0,date:'2025-07-20',status:'已完成',remark:'账实相符',sc:'done'},
  { code:'CT-002',wh:'成品库',total:15,done:15,diff:-1,date:'2025-07-25',status:'已完成',remark:'缺少1件',sc:'done'},
  { code:'CT-003',wh:'辅料库',total:45,done:30,diff:0,date:'2025-08-01',status:'盘点中',remark:'',sc:'counting'},
  { code:'CT-004',wh:'半成品库',total:28,done:0,diff:0,date:'2025-08-10',status:'草稿',remark:'',sc:'draft'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.wh.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',wh:'',total:0,done:0,date:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`CT-${String(data.value.length+1).padStart(3,'0')}`;f.wh='';f.total=0;f.done=0;f.date='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.wh){alert('请填写仓库');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-cell-num.diff{color:#d32f2f;font-weight:600;}.erp-tag.done{background:#e8f5e9;color:#2e7d32;}.erp-tag.counting{background:#fff3e0;color:#e65100;}.erp-tag.draft{background:#f5f5f5;color:#999;}</style>