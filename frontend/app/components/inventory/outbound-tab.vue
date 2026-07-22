<template><div class="erp-tab-body">
<div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索出库单号、物料..." class="erp-search-input"/></div><select v-model="ft" class="erp-filter-select"><option value="">全部类型</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建出库单</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">出库单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>类型</th><th>物料</th><th style="text-align:right;">数量</th><th>仓库</th><th>去向单号</th><th>日期</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td><span class="erp-tag">{{ r.type }}</span></td><td class="erp-cell-spec">{{ r.material }}</td><td class="erp-cell-num">{{ r.qty }}{{ r.unit }}</td><td>{{ r.wh }}</td><td class="erp-cell-spec">{{ r.dst }}</td><td class="erp-cell-spec">{{ r.date }}</td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建出库单" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>出库单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="erp-form-group"><label>出库类型</label><select v-model="f.type"><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
<div class="erp-form-group"><label>物料 <span class="erp-form-req">*</span></label><input v-model="f.material"/></div>
<div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" min="1"/></div>
<div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>个</option><option>件</option><option>kg</option></select></div>
<div class="erp-form-group"><label>仓库</label><input v-model="f.wh"/></div>
<div class="erp-form-group"><label>去向单号</label><input v-model="f.dst"/></div>
<div class="erp-form-group"><label>日期</label><input v-model="f.date" type="text"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>草稿</option><option>已出库</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const types = ['销售出库','生产领料','调拨出库','报废出库']
const data = ref([
  { code:'OUT-001',type:'销售出库',material:'皮带输送机 10m',qty:1,unit:'套',wh:'成品库',dst:'SHP-001',date:'2025-07-26',status:'已出库',remark:'',sc:'done'},
  { code:'OUT-002',type:'生产领料',material:'45#圆钢 Φ20',qty:120,unit:'kg',wh:'原材料库',dst:'MTL-001',date:'2025-08-01',status:'已出库',remark:'',sc:'done'},
  { code:'OUT-003',type:'生产领料',material:'M8×30六角螺栓',qty:200,unit:'个',wh:'辅料库',dst:'MTL-002',date:'2025-08-01',status:'已出库',remark:'',sc:'done'},
  { code:'OUT-004',type:'调拨出库',material:'304不锈钢板 2mm',qty:5,unit:'张',wh:'原材料主库',dst:'TRF-001',date:'2025-07-30',status:'草稿',remark:'',sc:'draft'},
  { code:'OUT-005',type:'报废出库',material:'壳体-减速器上盖',qty:2,unit:'件',wh:'半成品库',dst:'',date:'2025-07-28',status:'已出库',remark:'铸造缺陷',sc:'done'},
])
const s=ref('');const ft=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.material.includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',type:'销售出库',material:'',qty:1,unit:'个',wh:'',dst:'',date:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`OUT-${String(data.value.length+1).padStart(3,'0')}`;f.type='销售出库';f.material='';f.qty=1;f.unit='个';f.wh='';f.dst='';f.date='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.material){alert('请填写物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-search-input::placeholder{color:#bbb;}.erp-tag.done{background:#e8f5e9;color:#2e7d32;}.erp-tag.draft{background:#f5f5f5;color:#999;}</style>
