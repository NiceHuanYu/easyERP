<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索产品、订单..." class="erp-search-input"/></div><select v-model="fs" class="erp-filter-select"><option value="">全部结果</option><option>合格</option><option>不合格</option></select></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建OQC</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">检验单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>产品</th><th>销售订单</th><th style="text-align:right;">抽样数</th><th style="text-align:right;">不合格</th><th>结果</th><th>检验员</th><th>放行</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.product }}</td><td class="erp-cell-spec">{{ r.soCode }}</td><td class="erp-cell-num">{{ r.sample }}</td><td class="erp-cell-num">{{ r.defect }}</td><td><span :class="['erp-tag',r.sc]">{{ r.result }}</span></td><td>{{ r.inspector }}</td><td>{{ r.release?'✅':'❌' }}</td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建OQC" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>检验单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="erp-form-group"><label>产品</label><input v-model="f.product"/></div>
<div class="erp-form-group"><label>销售订单</label><input v-model="f.soCode"/></div>
<div class="erp-form-group"><label>抽样数</label><input v-model.number="f.sample" type="number" min="1"/></div>
<div class="erp-form-group"><label>不合格数</label><input v-model.number="f.defect" type="number" min="0"/></div>
<div class="erp-form-group"><label>检验结果</label><select v-model="f.result"><option>合格</option><option>不合格</option></select></div>
<div class="erp-form-group"><label>检验员</label><input v-model="f.inspector"/></div>
<div class="erp-form-group"><label>是否放行</label><select v-model="f.release"><option :value="true">放行</option><option :value="false">不放行</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'OQC-001',product:'减速器 SA67',soCode:'SO-2025-0089',sample:3,defect:0,result:'合格',inspector:'王质检',release:true,remark:'',sc:'pass'},
  { code:'OQC-002',product:'皮带输送机 10m',soCode:'SO-2025-0087',sample:1,defect:0,result:'合格',inspector:'王质检',release:true,remark:'',sc:'pass'},
  { code:'OQC-003',product:'螺旋输送机 LS400',soCode:'SO-2025-0086',sample:2,defect:0,result:'合格',inspector:'王质检',release:true,remark:'',sc:'pass'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.product.includes(q));if(fs.value)l=l.filter(m=>m.result===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',product:'',soCode:'',sample:1,defect:0,result:'合格',inspector:'',release:true,remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`OQC-${String(data.value.length+1).padStart(3,'0')}`;f.product='';f.soCode='';f.sample=1;f.defect=0;f.result='合格';f.inspector='';f.release=true;f.remark=''}showForm.value=true}
function save(){if(!f.product){alert('请填写产品');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.erp-tag.pass{background:#e8f5e9;color:#2e7d32;}.erp-tag.fail{background:#fce4ec;color:#c62828;}
</style>