<template><div class="tab-body"><div class="toolbar"><div class="toolbar-left"><div class="search-box"><span class="search-icon">🔍</span><input v-model="s" placeholder="搜索用户名、姓名..." class="search-input"/></div></div><div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建用户</button></div></div>
<div class="table-wrap"><table class="table"><thead><tr><th @click="sort('username')" class="sortable">用户名{{ sf==='username'?(sa?'▲':'▼'):'' }}</th><th>姓名</th><th>角色</th><th>部门</th><th>邮箱</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.username"><td class="code">{{ r.username }}</td><td>{{ r.name }}</td><td><span class="tag">{{ r.role }}</span></td><td>{{ r.dept }}</td><td class="spec">{{ r.email }}</td><td><span :class="['dot',r.status==='启用'?'on':'']"></span>{{ r.status }}</td><td class="acts"><button class="lnk" @click="openForm(r)">编辑</button><button class="lnk dgr" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建用户" @close="showForm=false" @save="save"><div class="grid">
<div class="fg"><label>用户名 <span class="req">*</span></label><input v-model="f.username"/></div><div class="fg"><label>姓名 <span class="req">*</span></label><input v-model="f.name"/></div>
<div class="fg"><label>角色</label><select v-model="f.role"><option>管理员</option><option>销售</option><option>采购</option><option>生产</option><option>仓库</option><option>品质</option><option>财务</option></select></div>
<div class="fg"><label>部门</label><input v-model="f.dept"/></div><div class="fg"><label>邮箱</label><input v-model="f.email" type="email"/></div>
<div class="fg"><label>状态</label><select v-model="f.status"><option>启用</option><option>停用</option></select></div>
<div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
<ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false"><p>确定删除 <strong>{{ dt?.username }}</strong> 吗？</p></ConfirmDialog></div></template>
<script setup lang="ts">
const data = ref([
  { username:'admin',name:'管理员',role:'管理员',dept:'管理部',email:'admin@easyerp.com',status:'启用',remark:''},
  { username:'zhangsan',name:'张三',role:'销售',dept:'销售部',email:'zhangsan@easyerp.com',status:'启用',remark:''},
  { username:'lisi',name:'李四',role:'采购',dept:'采购部',email:'lisi@easyerp.com',status:'启用',remark:''},
  { username:'wangwu',name:'王五',role:'生产',dept:'生产部',email:'wangwu@easyerp.com',status:'启用',remark:''},
  { username:'zhaoliu',name:'赵六',role:'仓库',dept:'仓储部',email:'zhaoliu@easyerp.com',status:'停用',remark:'离职'},
])
const s=ref('');const sf=ref('username');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.username.includes(q)||m.name.includes(q));l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
const showForm=ref(false);const editing=ref(false);const f=reactive({username:'',name:'',role:'销售',dept:'',email:'',status:'启用',remark:''});let ec=''
function openForm(item?:any){if(item){editing.value=true;ec=item.username;Object.assign(f,{...item})}else{editing.value=false;f.username='';f.name='';f.role='销售';f.dept='';f.email='';f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.username||!f.name){alert('请填写用户名和姓名');return}if(editing.value){const i=data.value.findIndex(m=>m.username===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null);function del(item:any){dt.value=item;showDel.value=true};function doDel(){if(dt.value)data.value=data.value.filter(m=>m.username!==dt.value!.username);showDel.value=false;dt.value=null}
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;background:#e8f0fe;color:#1a73e8;}
.dot{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:5px;background:#bbb;}.dot.on{background:#2e7d32;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;}.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;}.fg textarea{resize:vertical;font-family:inherit;}
</style>