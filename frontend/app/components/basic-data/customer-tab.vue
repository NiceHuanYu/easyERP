<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索客户编码、名称..." class="search-input" /></div>
        <select v-model="fs" class="filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建客户</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">客户编码 {{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">客户名称 {{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>联系人</th><th>联系电话</th><th>地址</th>
          <th style="text-align:right;">信用额度(元)</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="code">{{ row.code }}</td><td class="name">{{ row.name }}</td>
            <td>{{ row.contact }}</td><td>{{ row.phone }}</td><td class="spec">{{ row.address }}</td>
            <td class="num">{{ row.credit.toLocaleString() }}</td>
            <td><span :class="['dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑客户':'新建客户'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="numbering-row">
        <label class="radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="grid">
        <div class="fg"><label>客户编码 <span class="req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="fg"><label>客户名称 <span class="req">*</span></label><input v-model="f.name" placeholder="请输入客户名称" /></div>
        <div class="fg"><label>联系人</label><input v-model="f.contact" placeholder="姓名" /></div>
        <div class="fg"><label>联系电话</label><input v-model="f.phone" placeholder="手机号/固话" /></div>
        <div class="fg full"><label>客户地址</label><input v-model="f.address" placeholder="省/市/区/详细地址" /></div>
        <div class="fg"><label>信用额度 (元)</label><input v-model.number="f.credit" type="number" min="0" placeholder="0" /></div>
        <div class="fg"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除客户 <strong>{{ dt?.code }} - {{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const data = ref([
  { code:'CUS-001',name:'华强电子科技有限公司',contact:'李明',phone:'13800138001',address:'深圳市南山区科技园',credit:500000,status:'启用',remark:'' },
  { code:'CUS-002',name:'鑫达精密制造有限公司',contact:'王芳',phone:'13900139002',address:'东莞市长安镇工业区',credit:300000,status:'启用',remark:'' },
  { code:'CUS-003',name:'宏远机械装备股份公司',contact:'张伟',phone:'13700137003',address:'广州市黄埔区开发区',credit:800000,status:'启用',remark:'VIP客户' },
  { code:'CUS-004',name:'丰华重型机械有限公司',contact:'赵磊',phone:'13600136004',address:'佛山市顺德区',credit:200000,status:'启用',remark:'' },
  { code:'CUS-005',name:'瑞丰传动设备有限公司',contact:'陈静',phone:'13500135005',address:'苏州市吴江区',credit:150000,status:'停用',remark:'暂停合作' },
  { code:'CUS-006',name:'天工精密零件加工厂',contact:'刘强',phone:'13400134006',address:'宁波市北仑区',credit:100000,status:'启用',remark:'' },
  { code:'CUS-007',name:'金鹰重工集团股份公司',contact:'孙丽',phone:'13300133007',address:'武汉市东湖高新区',credit:1200000,status:'启用',remark:'大客户' },
])
const s=ref(''); const fs=ref(''); const sf=ref('code'); const sa=ref(true); const page=ref(1); const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.name.toLowerCase().includes(q)||m.contact.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',contact:'',phone:'',address:'',credit:0,status:'启用',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`CUS-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.contact='';f.phone='';f.address='';f.credit=0;f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name){alert('请填写客户名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
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
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.name{font-weight:500;}.spec{color:#666;font-size:12px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.dot{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:5px;background:#bbb;}.dot.on{background:#2e7d32;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;cursor:not-allowed;}.fg textarea{resize:vertical;font-family:inherit;}
.numbering-row{display:flex;gap:24px;margin-bottom:16px;padding:10px 14px;background:#f8faff;border-radius:8px;border:1px solid #e0eeff;}
.radio-label{display:flex;align-items:center;gap:6px;font-size:13px;color:#555;cursor:pointer;}
.radio-label input[type="radio"]{accent-color:#1a73e8;}
</style>
