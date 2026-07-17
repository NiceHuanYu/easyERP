<template><div class="tab-body"><div class="toolbar"><div class="toolbar-left"><div class="search-box"><span class="search-icon">🔍</span><input v-model="s" placeholder="搜索角色名称..." class="search-input"/></div></div><div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建角色</button></div></div>
<div class="table-wrap"><table class="table"><thead><tr><th @click="sort('code')" class="sortable">角色编码{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>角色名称</th><th>描述</th><th>用户数</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="code">{{ r.code }}</td><td>{{ r.name }}</td><td class="spec">{{ r.desc }}</td><td class="num">{{ r.userCount }}</td><td><span :class="['dot',r.status==='启用'?'on':'']"></span>{{ r.status }}</td><td class="acts"><button class="lnk" @click="openForm(r)">编辑</button><button class="lnk dgr" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="6" class="empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建角色" @close="showForm=false" @save="save"><div v-if="!editing" class="numbering-row"><label class="radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="grid">
<div class="fg"><label>角色编码</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div><div class="fg"><label>角色名称 <span class="req">*</span></label><input v-model="f.name"/></div>
<div class="fg"><label>描述</label><input v-model="f.desc"/></div><div class="fg"><label>状态</label><select v-model="f.status"><option>启用</option><option>停用</option></select></div>
<div class="fg full"><label>权限菜单</label><textarea v-model="f.permissions" rows="3" placeholder="如：基础资料,销售管理,采购管理"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.name }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { code:'ROLE-001',name:'管理员',desc:'系统管理员，拥有全部权限',userCount:1,status:'启用',permissions:'全部'},
  { code:'ROLE-002',name:'销售员',desc:'销售订单与客户管理',userCount:1,status:'启用',permissions:'基础资料,销售管理'},
  { code:'ROLE-003',name:'采购员',desc:'采购订单与供应商管理',userCount:1,status:'启用',permissions:'基础资料,采购管理'},
  { code:'ROLE-004',name:'生产主管',desc:'生产工单与排程管理',userCount:1,status:'启用',permissions:'基础资料,生产管理'},
  { code:'ROLE-005',name:'仓库管理员',desc:'库存管理',userCount:1,status:'启用',permissions:'基础资料,库存管理'},
  { code:'ROLE-006',name:'质量检验员',desc:'品质检验',userCount:1,status:'启用',permissions:'品质管理'},
  { code:'ROLE-007',name:'财务',desc:'财务模块',userCount:1,status:'停用',permissions:'财务管理'},
])
const s=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.name.includes(q)||m.code.toLowerCase().includes(q));l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',desc:'',permissions:'',status:'启用'});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`ROLE-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.desc='';f.permissions='';f.status='启用'}showForm.value=true}
function save(){if(!f.name){alert('请填写角色名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.dot{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:5px;background:#bbb;}.dot.on{background:#2e7d32;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;}.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;}.fg textarea{resize:vertical;font-family:inherit;}
.numbering-row{display:flex;gap:24px;margin-bottom:16px;padding:10px 14px;background:#f8faff;border-radius:8px;border:1px solid #e0eeff;}
.radio-label{display:flex;align-items:center;gap:6px;font-size:13px;color:#555;cursor:pointer;}
.radio-label input[type="radio"]{accent-color:#1a73e8;}
</style>