<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索仓库编码、名称..." class="erp-search-input" /></div>
        <select v-model="ft" class="erp-filter-select"><option value="">全部类型</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建仓库</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">仓库编码{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">仓库名称{{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>仓库类型</th><th>负责人</th><th>联系电话</th><th>仓库位置</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-name">{{ row.name }}</td>
            <td><span class="erp-tag">{{ row.type }}</span></td>
            <td>{{ row.manager }}</td><td>{{ row.phone }}</td>
            <td class="erp-cell-spec">{{ row.location }}</td>
            <td><span :class="['erp-dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑仓库':'新建仓库'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>仓库编码 <span class="erp-form-req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>仓库名称 <span class="erp-form-req">*</span></label><input v-model="f.name" placeholder="请输入仓库名称" /></div>
        <div class="erp-form-group"><label>仓库类型 <span class="erp-form-req">*</span></label><select v-model="f.type"><option value="">请选择</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div>
        <div class="erp-form-group"><label>负责人</label><input v-model="f.manager" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>联系电话</label><input v-model="f.phone" placeholder="手机号/固话" /></div>
        <div class="erp-form-group"><label>仓库位置</label><input v-model="f.location" placeholder="如：1号厂房A区" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除仓库 <strong>{{ dt?.code }} - {{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const types = ['原材料库','半成品库','成品库','辅料库','废品库','待检区']
const data = ref([
  { code:'WH-001',name:'原材料主库',type:'原材料库',manager:'刘洋',phone:'8001',location:'1号厂房A区',status:'启用',remark:'' },
  { code:'WH-002',name:'钢材专用库',type:'原材料库',manager:'刘洋',phone:'8001',location:'1号厂房B区',status:'启用',remark:'存放钢材类物料' },
  { code:'WH-003',name:'半成品库',type:'半成品库',manager:'陈丽',phone:'8002',location:'2号厂房A区',status:'启用',remark:'' },
  { code:'WH-004',name:'成品库',type:'成品库',manager:'王强',phone:'8003',location:'2号厂房B区',status:'启用',remark:'' },
  { code:'WH-005',name:'辅料库',type:'辅料库',manager:'李梅',phone:'8004',location:'3号厂房A区',status:'启用',remark:'含包材和耗材' },
  { code:'WH-006',name:'废品区',type:'废品库',manager:'赵刚',phone:'8005',location:'厂区西北角',status:'启用',remark:'' },
  { code:'WH-007',name:'来料待检区',type:'待检区',manager:'周丽华',phone:'8006',location:'原料库旁',status:'启用',remark:'IQC未完成物料' },
  { code:'WH-008',name:'外租库-龙华',type:'成品库',manager:'王强',phone:'8003',location:'龙华区观澜街道',status:'停用',remark:'租约到期' },
])
const s=ref('');const ft=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.name.toLowerCase().includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft,fs],()=>page.value=1)

const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',type:'',manager:'',phone:'',location:'',status:'启用',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`WH-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.type='';f.manager='';f.phone='';f.location='';f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name||!f.type){alert('请填写仓库名称和类型');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
</style>
