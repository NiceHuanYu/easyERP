<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索物料、批号..." class="erp-search-input"/></div><select v-model="ft" class="erp-filter-select"><option value="">全部处理方式</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建记录</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>物料</th><th>来源</th><th>不合格描述</th><th>处理方式</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.material }}</td><td class="erp-cell-spec">{{ r.source }}</td><td class="erp-cell-spec">{{ r.desc }}</td><td><span class="erp-tag">{{ r.action }}</span></td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建不合格品记录" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="erp-form-group"><label>物料</label><input v-model="f.material"/></div>
<div class="erp-form-group"><label>来源</label><input v-model="f.source" placeholder="IQC/IPQC/OQC"/></div>
<div class="erp-form-group full"><label>不合格描述</label><textarea v-model="f.desc" rows="2"></textarea></div>
<div class="erp-form-group"><label>处理方式</label><select v-model="f.action"><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>待处理</option><option>处理中</option><option>已关闭</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const types = ['退货','返工','让步接收','报废']
const data = ref([
  { code:'NC-001',material:'Q235槽钢 10#',source:'IQC-001',desc:'表面严重锈蚀',action:'退货',status:'已关闭',remark:'已退回供应商',sc:'closed'},
  { code:'NC-002',material:'壳体-减速器上盖',source:'IPQC-004',desc:'铸造气孔超标',action:'报废',status:'已关闭',remark:'',sc:'closed'},
  { code:'NC-003',material:'45#圆钢 Φ20',source:'IQC-003',desc:'尺寸偏差0.5mm',action:'让步接收',status:'处理中',remark:'与生产确认可用',sc:'processing'},
])
const s=ref('');const ft=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.material.includes(q)||m.code.toLowerCase().includes(q));if(ft.value)l=l.filter(m=>m.action===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',material:'',source:'',desc:'',action:'退货',status:'待处理',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`NC-${String(data.value.length+1).padStart(3,'0')}`;f.material='';f.source='';f.desc='';f.action='退货';f.status='待处理';f.remark=''}showForm.value=true}
function save(){if(!f.material){alert('请填写物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-tag{background:#e8f0fe;color:#1a73e8;}.erp-tag.closed{background:#f5f5f5;color:#999;}.erp-tag.processing{background:#fff3e0;color:#e65100;}
</style>