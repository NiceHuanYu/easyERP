<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索标准名称、物料..." class="erp-search-input"/></div><select v-model="ft" class="erp-filter-select"><option value="">全部类型</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建标准</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">标准编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>标准名称</th><th>适用类型</th><th>检验项目</th><th>抽样方案</th><th>AQL</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.name }}</td><td><span class="erp-tag">{{ r.type }}</span></td><td class="erp-cell-spec">{{ r.items }}</td><td>{{ r.sampling }}</td><td>{{ r.aql }}</td><td><span :class="['erp-dot',r.status==='启用'?'on':'']"></span>{{ r.status }}</td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建标准" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>标准编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="erp-form-group"><label>标准名称 <span class="erp-form-req">*</span></label><input v-model="f.name"/></div>
<div class="erp-form-group"><label>适用类型</label><select v-model="f.type"><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
<div class="erp-form-group"><label>检验项目</label><input v-model="f.items" placeholder="如 尺寸/外观/硬度"/></div>
<div class="erp-form-group"><label>抽样方案</label><select v-model="f.sampling"><option>GB/T 2828.1</option><option>全检</option><option>自定义</option></select></div>
<div class="erp-form-group"><label>AQL值</label><input v-model="f.aql" placeholder="如 1.0"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>启用</option><option>停用</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const types = ['IQC','IPQC','OQC']
const data = ref([
  { code:'STD-001',name:'钢材来料检验标准',type:'IQC',items:'尺寸/材质/表面',sampling:'GB/T 2828.1',aql:'1.0',status:'启用',remark:''},
  { code:'STD-002',name:'机加工过程检验标准',type:'IPQC',items:'尺寸/粗糙度/倒角',sampling:'GB/T 2828.1',aql:'0.65',status:'启用',remark:''},
  { code:'STD-003',name:'成品出货检验标准',type:'OQC',items:'外观/功能/包装',sampling:'GB/T 2828.1',aql:'0.25',status:'启用',remark:''},
  { code:'STD-004',name:'焊接工序检验标准',type:'IPQC',items:'焊缝/变形/强度',sampling:'全检',aql:'-',status:'停用',remark:'待更新'},
])
const s=ref('');const ft=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.name.includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',type:'IQC',items:'',sampling:'GB/T 2828.1',aql:'',status:'启用',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`STD-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.type='IQC';f.items='';f.sampling='GB/T 2828.1';f.aql='';f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name){alert('请填写标准名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-tag{background:#e8f0fe;color:#1a73e8;}.erp-dot.on{background:#2e7d32;}
</style>