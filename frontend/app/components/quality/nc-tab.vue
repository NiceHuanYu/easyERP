<template><div class="tab-body"><div class="toolbar"><div class="toolbar-left"><div class="search-box"><span class="search-icon">🔍</span><input v-model="s" placeholder="搜索物料、批号..." class="search-input"/></div><select v-model="ft" class="filter-select"><option value="">全部处理方式</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div><div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建记录</button></div></div>
<div class="table-wrap"><table class="table"><thead><tr><th @click="sort('code')" class="sortable">编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>物料</th><th>来源</th><th>不合格描述</th><th>处理方式</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="code">{{ r.code }}</td><td>{{ r.material }}</td><td class="spec">{{ r.source }}</td><td class="spec">{{ r.desc }}</td><td><span class="tag">{{ r.action }}</span></td><td><span :class="['tag',r.sc]">{{ r.status }}</span></td><td class="acts"><button class="lnk" @click="openForm(r)">编辑</button><button class="lnk dgr" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建不合格品记录" @close="showForm=false" @save="save"><div v-if="!editing" class="numbering-row"><label class="radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="grid">
<div class="fg"><label>编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div>
<div class="fg"><label>物料</label><input v-model="f.material"/></div>
<div class="fg"><label>来源</label><input v-model="f.source" placeholder="IQC/IPQC/OQC"/></div>
<div class="fg full"><label>不合格描述</label><textarea v-model="f.desc" rows="2"></textarea></div>
<div class="fg"><label>处理方式</label><select v-model="f.action"><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
<div class="fg"><label>状态</label><select v-model="f.status"><option>待处理</option><option>处理中</option><option>已关闭</option></select></div>
<div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
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
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}
.filter-select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:8px;background:#fafafa;font-size:13px;color:#555;outline:none;cursor:pointer;}.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;background:#e8f0fe;color:#1a73e8;}.tag.closed{background:#f5f5f5;color:#999;}.tag.processing{background:#fff3e0;color:#e65100;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;}.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;}.fg textarea{resize:vertical;font-family:inherit;}
.numbering-row{display:flex;gap:24px;margin-bottom:16px;padding:10px 14px;background:#f8faff;border-radius:8px;border:1px solid #e0eeff;}
.radio-label{display:flex;align-items:center;gap:6px;font-size:13px;color:#555;cursor:pointer;}
.radio-label input[type="radio"]{accent-color:#1a73e8;}
</style>