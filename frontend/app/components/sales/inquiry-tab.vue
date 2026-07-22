<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索询价编号、客户..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in statuses" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建询价</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">询价编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('customer')" class="sortable">客户名称{{ sf==='customer'?(sa?'▲':'▼'):'' }}</th>
          <th>联系人</th><th>物料名称</th><th style="text-align:right;">数量</th>
          <th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.customer }}</td>
            <td>{{ row.contact }}</td><td class="erp-cell-spec">{{ row.material }}</td>
            <td class="erp-cell-num">{{ row.qty }} {{ row.unit }}</td>
            <td><span :class="['erp-tag', row.statusClass]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑询价':'新建询价'" @close="showForm=false" @save="save">
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>询价编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>客户名称 <span class="erp-form-req">*</span></label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="erp-form-group"><label>联系人</label><input v-model="f.contact" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>物料名称 <span class="erp-form-req">*</span></label><input v-model="f.material" placeholder="如 减速器SA67" /></div>
        <div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" min="1" placeholder="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>台</option><option>套</option><option>件</option><option>个</option><option>kg</option></select></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in statuses" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="客户特殊要求等"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除询价 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const statuses = ['待回复','已报价','已关闭']
const data = ref([
  { code:'INQ-001',customer:'华强电子',contact:'李明',material:'减速器 SA67',qty:5,unit:'台',status:'待回复',remark:'',statusClass:'pending' },
  { code:'INQ-002',customer:'鑫达精密',contact:'王芳',material:'电动滚筒 5.5kW',qty:3,unit:'台',status:'待回复',remark:'加急',statusClass:'pending' },
  { code:'INQ-003',customer:'宏远机械',contact:'张伟',material:'皮带输送机 10m',qty:2,unit:'套',status:'已报价',remark:'',statusClass:'quoted' },
  { code:'INQ-004',customer:'丰华重型',contact:'赵磊',material:'螺旋输送机 LS400',qty:1,unit:'套',status:'已报价',remark:'',statusClass:'quoted' },
  { code:'INQ-005',customer:'天工精密',contact:'刘强',material:'振动电机 YZO-8-4',qty:10,unit:'台',status:'已关闭',remark:'客户取消',statusClass:'closed' },
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',customer:'',contact:'',material:'',qty:1,unit:'台',status:'待回复',remark:''});let ec=''
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`INQ-${String(data.value.length+1).padStart(3,'0')}`;f.customer='';f.contact='';f.material='';f.qty=1;f.unit='台';f.status='待回复';f.remark=''}showForm.value=true}
function save(){if(!f.customer||!f.material){alert('请填写客户和物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.quoted{background:#e8f5e9;color:#2e7d32;}
.erp-tag.closed{background:#f5f5f5;color:#999;}
</style>
