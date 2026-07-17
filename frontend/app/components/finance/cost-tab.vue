<template><div class="tab-body"><div class="toolbar"><h3 style="font-size:15px;color:#333;margin:0;">产品成本核算</h3>
<button class="btn btn-primary" @click="openForm()">＋ 新建核算</button></div>
<div class="table-wrap"><table class="table"><thead><tr><th @click="sort('code')" class="sortable">核算编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>产品</th><th>关联工单</th><th style="text-align:right;">材料成本</th><th style="text-align:right;">人工成本</th><th style="text-align:right;">制造费用</th><th style="text-align:right;">总成本</th><th style="text-align:right;">单位成本</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="code">{{ r.code }}</td><td>{{ r.product }}</td><td class="spec">{{ r.moCode }}</td><td class="num">{{ r.material.toFixed(2) }}</td><td class="num">{{ r.labor.toFixed(2) }}</td><td class="num">{{ r.overhead.toFixed(2) }}</td><td class="num">{{ r.total.toFixed(2) }}</td><td class="num">{{ r.unitCost.toFixed(2) }}</td><td class="acts"><button class="lnk" @click="openForm(r)">编辑</button><button class="lnk dgr" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="9" class="empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建核算" @close="showForm=false" @save="save"><div v-if="!editing" class="numbering-row"><label class="radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="grid">
<div class="fg"><label>核算编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div><div class="fg"><label>产品</label><input v-model="f.product"/></div>
<div class="fg"><label>关联工单</label><input v-model="f.moCode"/></div><div class="fg"><label>材料成本</label><input v-model.number="f.material" type="number" step="0.01"/></div>
<div class="fg"><label>人工成本</label><input v-model.number="f.labor" type="number" step="0.01"/></div><div class="fg"><label>制造费用</label><input v-model.number="f.overhead" type="number" step="0.01"/></div>
<div class="fg"><label>完工数量</label><input v-model.number="f.qty" type="number" min="1"/></div>
<div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.code }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'COST-001',product:'减速器 SA67',moCode:'MO-2025-0041',material:18500,labor:4200,overhead:3300,total:26000,qty:10,unitCost:2600,remark:''},
  { code:'COST-002',product:'螺旋输送机 LS400',moCode:'MO-2025-0042',material:8900,labor:2100,overhead:1500,total:12500,qty:3,unitCost:4166.67,remark:''},
  { code:'COST-003',product:'减速器 SA67',moCode:'MO-2025-0045',material:9200,labor:2400,overhead:1800,total:13400,qty:5,unitCost:2680,remark:'进行中'},
])
const s=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.product.includes(q)||m.code.toLowerCase().includes(q));l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',product:'',moCode:'',material:0,labor:0,overhead:0,total:0,qty:1,unitCost:0,remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`COST-${String(data.value.length+1).padStart(3,'0')}`;f.product='';f.moCode='';f.material=0;f.labor=0;f.overhead=0;f.qty=1;f.remark=''}showForm.value=true}
function save(){if(!f.product){alert('请填写产品');return}const t=f.material+f.labor+f.overhead;f.total=t;f.unitCost=f.qty>0?t/f.qty:0;if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;}.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;}.fg textarea{resize:vertical;font-family:inherit;}
.numbering-row{display:flex;gap:24px;margin-bottom:16px;padding:10px 14px;background:#f8faff;border-radius:8px;border:1px solid #e0eeff;}
.radio-label{display:flex;align-items:center;gap:6px;font-size:13px;color:#555;cursor:pointer;}
.radio-label input[type="radio"]{accent-color:#1a73e8;}
</style>