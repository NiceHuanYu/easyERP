<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索联系人、单位名称..." class="search-input" /></div>
        <select v-model="ft" class="filter-select"><option value="">全部类型</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建联系人</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('name')" class="sortable">联系人 {{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('phone')" class="sortable">联系电话 {{ sf==='phone'?(sa?'▲':'▼'):'' }}</th>
          <th>所属类型</th><th @click="sort('org')" class="sortable">所属单位 {{ sf==='org'?(sa?'▲':'▼'):'' }}</th><th>备注</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.id">
            <td class="name">{{ row.name }}</td><td>{{ row.phone }}</td>
            <td><span class="tag">{{ row.type }}</span></td><td>{{ row.org }}</td><td class="spec">{{ row.remark||'-' }}</td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="6" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑联系人':'新建联系人'" @close="showForm=false" @save="save">
      <div class="grid">
        <div class="fg"><label>联系人 <span class="req">*</span></label><input v-model="f.name" placeholder="姓名" /></div>
        <div class="fg"><label>联系电话</label><input v-model="f.phone" placeholder="手机号/固话" /></div>
        <div class="fg"><label>所属类型 <span class="req">*</span></label><select v-model="f.type"><option value="">请选择</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
        <div class="fg"><label>所属单位 <span class="req">*</span></label><input v-model="f.org" placeholder="单位名称" /></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除联系人 <strong>{{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const types = ['客户','供应商','仓库']
const data = ref([
  { id:1, name:'李明', phone:'13800138001', type:'客户', org:'华强电子科技有限公司', remark:'' },
  { id:2, name:'王芳', phone:'13900139002', type:'客户', org:'鑫达精密制造有限公司', remark:'' },
  { id:3, name:'张伟', phone:'13700137003', type:'客户', org:'宏远机械装备股份公司', remark:'' },
  { id:4, name:'赵磊', phone:'13600136004', type:'客户', org:'丰华重型机械有限公司', remark:'' },
  { id:5, name:'周明', phone:'0755-82888801', type:'供应商', org:'宏远钢铁集团有限公司', remark:'采购经理' },
  { id:6, name:'吴刚', phone:'0577-62888802', type:'供应商', org:'正泰电气股份有限公司', remark:'' },
  { id:7, name:'郑涛', phone:'0310-69888803', type:'供应商', org:'东明标准件有限公司', remark:'' },
  { id:8, name:'刘洋', phone:'8001', type:'仓库', org:'原材料主库', remark:'仓库主管' },
  { id:9, name:'陈丽', phone:'8002', type:'仓库', org:'半成品库', remark:'' },
  { id:10,name:'王强', phone:'8003', type:'仓库', org:'成品库', remark:'' },
  { id:11,name:'李梅', phone:'8004', type:'仓库', org:'辅料库', remark:'' },
])
let nextId = 12
const s=ref('');const ft=ref('');const sf=ref('name');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.name.includes(q)||m.org.includes(q)||m.phone.includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)

const showForm=ref(false);const editing=ref(false);const f=reactive({name:'',phone:'',type:'',org:'',remark:''})
let ei=-1
function openForm(item?:any){if(item){editing.value=true;ei=item.id;Object.assign(f,{...item})}else{editing.value=false;f.name='';f.phone='';f.type='';f.org='';f.remark=''}showForm.value=true}
function save(){if(!f.name||!f.type||!f.org){alert('请填写联系人、所属类型和所属单位');return}if(editing.value){const i=data.value.findIndex(m=>m.id===ei);if(i!==-1)data.value[i]={...f,id:ei}as any}else data.value.push({...f,id:nextId++}as any);showForm.value=false}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.id!==dt.value!.id);showDel.value=false;dt.value=null}
</script>
<style scoped>
.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}.search-input::placeholder{color:#bbb;}
.filter-select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:8px;background:#fafafa;font-size:13px;color:#555;outline:none;cursor:pointer;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}
.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.name{font-weight:500;}.spec{color:#666;font-size:12px;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;background:#e8f0fe;color:#1a73e8;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg textarea{resize:vertical;font-family:inherit;}
</style>
