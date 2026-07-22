<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索用户名、姓名..." class="erp-search-input"/></div></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建用户</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('username')" class="sortable">用户名{{ sf==='username'?(sa?'▲':'▼'):'' }}</th><th>姓名</th><th>角色</th><th>部门</th><th>邮箱</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.username"><td class="erp-cell-code">{{ r.username }}</td><td>{{ r.name }}</td><td><span class="erp-tag">{{ r.role }}</span></td><td>{{ r.dept }}</td><td class="erp-cell-spec">{{ r.email }}</td><td><span :class="['erp-dot',r.status==='启用'?'on':'']"></span>{{ r.status }}</td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建用户" @close="showForm=false" @save="save"><div class="erp-form-grid">
<div class="erp-form-group"><label>用户名 <span class="erp-form-req">*</span></label><input v-model="f.username"/></div><div class="erp-form-group"><label>姓名 <span class="erp-form-req">*</span></label><input v-model="f.name"/></div>
<div class="erp-form-group"><label>角色</label><select v-model="f.role"><option>管理员</option><option>销售</option><option>采购</option><option>生产</option><option>仓库</option><option>品质</option><option>财务</option></select></div>
<div class="erp-form-group"><label>部门</label><input v-model="f.dept"/></div><div class="erp-form-group"><label>邮箱</label><input v-model="f.email" type="email"/></div>
<div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>启用</option><option>停用</option></select></div>
<div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2"></textarea></div></div></FormModal>
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
<style scoped>.erp-dot.on{background:#2e7d32;}
</style>