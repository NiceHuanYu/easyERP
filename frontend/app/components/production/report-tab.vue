<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索报工单号、工单号..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建报工</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">报工单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联工单</th><th>工序</th><th>操作员</th>
          <th style="text-align:right;">完工数</th><th style="text-align:right;">不良数</th><th style="text-align:right;">工时(h)</th><th>报工日期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.moCode }}</td><td>{{ row.operation }}</td>
            <td>{{ row.operator }}</td><td class="erp-cell-num">{{ row.goodQty }}</td><td class="erp-cell-num">{{ row.badQty }}</td>
            <td class="erp-cell-num">{{ row.hours }}</td><td class="erp-cell-spec">{{ row.date }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="10" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑报工':'新建报工'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>报工单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联工单</label><input v-model="f.moCode" placeholder="MO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>工序名称</label><input v-model="f.operation" placeholder="如 车削" /></div>
        <div class="erp-form-group"><label>操作员</label><input v-model="f.operator" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>完工数量</label><input v-model.number="f.goodQty" type="number" min="0" /></div>
        <div class="erp-form-group"><label>不良数量</label><input v-model.number="f.badQty" type="number" min="0" /></div>
        <div class="erp-form-group"><label>工时 (h)</label><input v-model.number="f.hours" type="number" step="0.5" min="0" /></div>
        <div class="erp-form-group"><label>报工日期</label><input v-model="f.date" type="text" placeholder="2025-08-01" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除报工记录 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','已确认','已审核']
const data = ref([
  { code:'WR-001',moCode:'MO-2025-0045',operation:'车削加工',operator:'张工',goodQty:8,badQty:1,hours:4,date:'2025-08-01',status:'已确认',remark:'',sc:'confirmed'},
  { code:'WR-002',moCode:'MO-2025-0045',operation:'钻孔',operator:'李工',goodQty:8,badQty:0,hours:2,date:'2025-08-01',status:'草稿',remark:'',sc:'draft'},
  { code:'WR-003',moCode:'MO-2025-0042',operation:'装配',operator:'赵工',goodQty:1,badQty:0,hours:8,date:'2025-07-25',status:'已审核',remark:'',sc:'approved'},
  { code:'WR-004',moCode:'MO-2025-0041',operation:'总装测试',operator:'王工',goodQty:10,badQty:0,hours:6,date:'2025-07-25',status:'已审核',remark:'',sc:'approved'},
  { code:'WR-005',moCode:'MO-2025-0045',operation:'焊接',operator:'',goodQty:0,badQty:0,hours:0,date:'',status:'草稿',remark:'待排程',sc:'draft'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.moCode.toLowerCase().includes(q)||m.operator.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',moCode:'',operation:'',operator:'',goodQty:0,badQty:0,hours:0,date:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`WR-${String(data.value.length+1).padStart(3,'0')}`;f.moCode='';f.operation='';f.operator='';f.goodQty=0;f.badQty=0;f.hours=0;f.date='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.operation){alert('请填写工序名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.confirmed{background:#fff3e0;color:#e65100;}.erp-tag.approved{background:#e8f5e9;color:#2e7d32;}
</style>
