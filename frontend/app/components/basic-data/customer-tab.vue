<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索客户编码、名称..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建客户</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">客户编码 {{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">客户名称 {{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>联系人</th><th>联系电话</th><th>地址</th>
          <th style="text-align:right;">信用额度(元)</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-name">{{ row.name }}</td>
            <td>{{ row.contact }}</td><td>{{ row.phone }}</td><td class="erp-cell-spec">{{ row.address }}</td>
            <td class="erp-cell-num">{{ row.credit.toLocaleString() }}</td>
            <td><span :class="['erp-dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑客户':'新建客户'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>客户编码 <span class="erp-form-req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>客户名称 <span class="erp-form-req">*</span></label><input v-model="f.name" placeholder="请输入客户名称" /></div>
        <div class="erp-form-group"><label>联系人</label><input v-model="f.contact" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>联系电话</label><input v-model="f.phone" placeholder="手机号/固话" /></div>
        <div class="erp-form-group full"><label>客户地址</label><input v-model="f.address" placeholder="省/市/区/详细地址" /></div>
        <div class="erp-form-group"><label>信用额度 (元)</label><input v-model.number="f.credit" type="number" min="0" placeholder="0" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
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
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`CUS-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.address='';f.credit=0;f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name){alert('请填写客户名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
/* === 组件特有样式 === */
.erp-cell-spec{max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
</style>
