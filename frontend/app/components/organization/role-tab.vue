<template><div class="erp-tab-body"><div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索角色名称..." class="erp-search-input"/></div></div><div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建角色</button></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">角色编码{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>角色名称</th><th>描述</th><th>用户数</th><th>状态</th><th style="text-align:center;">操作</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.name }}</td><td class="erp-cell-spec">{{ r.desc }}</td><td class="erp-cell-num">{{ r.userCount }}</td><td><span :class="['erp-dot',r.status==='启用'?'on':'']"></span>{{ r.status }}</td><td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(r)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="del(r)">删除</button></td></tr>
<tr v-if="paged.length===0"><td colspan="6" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/>
<FormModal :show="showForm" title="新建角色" @close="showForm=false" @save="save"><div v-if="!editing" class="erp-numbering-row"><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label><label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label></div><div class="erp-form-grid">
<div class="erp-form-group"><label>角色编码</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'"/></div><div class="erp-form-group"><label>角色名称 <span class="erp-form-req">*</span></label><input v-model="f.name"/></div>
<div class="erp-form-group"><label>描述</label><input v-model="f.desc"/></div><div class="erp-form-group"><label>状态</label><select v-model="f.status"><option>启用</option><option>停用</option></select></div>
<div class="erp-form-group full"><label>权限菜单</label><textarea v-model="f.permissions" rows="3" placeholder="如：基础资料,销售管理,采购管理"></textarea></div></div></FormModal>
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
<style scoped>.erp-dot.on{background:#2e7d32;}
</style>