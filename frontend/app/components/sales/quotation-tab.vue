<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索报价单号、客户..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建报价</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">报价单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('customer')" class="sortable">客户{{ sf==='customer'?(sa?'▲':'▼'):'' }}</th>
          <th>物料名称</th><th style="text-align:right;">金额(元)</th><th>版本</th><th>有效期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.customer }}</td>
            <td class="erp-cell-spec">{{ row.material }}</td><td class="erp-cell-num">{{ row.amount.toFixed(2) }}</td>
            <td>V{{ row.version }}</td><td class="erp-cell-spec">{{ row.validUntil }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑报价':'新建报价'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>报价单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>客户名称 <span class="erp-form-req">*</span></label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="erp-form-group"><label>物料名称 <span class="erp-form-req">*</span></label><input v-model="f.material" placeholder="物料/产品名称" /></div>
        <div class="erp-form-group"><label>报价金额 (元)</label><input v-model.number="f.amount" type="number" step="0.01" placeholder="0.00" /></div>
        <div class="erp-form-group"><label>版本</label><input v-model="f.version" type="text" placeholder="1.0" /></div>
        <div class="erp-form-group"><label>有效期至</label><input v-model="f.validUntil" type="text" placeholder="2025-12-31" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除报价 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','已发送','已确认','已失效']
const data = ref([
  { code:'QTN-001',customer:'华强电子',material:'减速器 SA67',amount:14250,version:1,validUntil:'2025-08-15',status:'已发送',remark:'',sc:'sent' },
  { code:'QTN-002',customer:'鑫达精密',material:'电动滚筒 5.5kW',amount:20400,version:2,validUntil:'2025-08-20',status:'已确认',remark:'客户已签字',sc:'confirmed' },
  { code:'QTN-003',customer:'宏远机械',material:'皮带输送机 10m',amount:65000,version:1,validUntil:'2025-08-10',status:'草稿',remark:'等待审核',sc:'draft' },
  { code:'QTN-004',customer:'宏远机械',material:'螺旋输送机 LS400',amount:18600,version:1,validUntil:'2025-07-30',status:'已失效',remark:'客户未回复',sc:'expired' },
  { code:'QTN-005',customer:'天工精密',material:'振动电机 YZO-8-4',amount:16500,version:1,validUntil:'2025-09-01',status:'已发送',remark:'',sc:'sent' },
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',customer:'',material:'',amount:0,version:1,validUntil:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`QTN-${String(data.value.length+1).padStart(3,'0')}`;f.customer='';f.material='';f.amount=0;f.version=1;f.validUntil='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.customer||!f.material){alert('请填写客户和物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}
.erp-tag.sent{background:#fff3e0;color:#e65100;}
.erp-tag.confirmed{background:#e8f5e9;color:#2e7d32;}
.erp-tag.expired{background:#fce4ec;color:#c62828;}
</style>
