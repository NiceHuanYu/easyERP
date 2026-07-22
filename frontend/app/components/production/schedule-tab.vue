<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索工单号、产品..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建排程</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">排程编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联工单</th><th>产品</th><th>工序</th><th>设备</th><th>操作员</th>
          <th>计划工时(h)</th><th>开始时间</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.moCode }}</td><td>{{ row.product }}</td>
            <td>{{ row.operation }}</td><td>{{ row.machine }}</td><td>{{ row.operator }}</td>
            <td class="erp-cell-num">{{ row.hours }}</td><td class="erp-cell-spec">{{ row.startTime }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="10" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑排程':'新建排程'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>排程编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联工单</label><input v-model="f.moCode" placeholder="MO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>产品名称</label><input v-model="f.product" placeholder="产品名称" /></div>
        <div class="erp-form-group"><label>工序名称</label><input v-model="f.operation" placeholder="如 切割" /></div>
        <div class="erp-form-group"><label>设备编号</label><input v-model="f.machine" placeholder="如 CNC-001" /></div>
        <div class="erp-form-group"><label>操作员</label><input v-model="f.operator" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>计划工时 (h)</label><input v-model.number="f.hours" type="number" step="0.5" min="0" /></div>
        <div class="erp-form-group"><label>开始时间</label><input v-model="f.startTime" type="text" placeholder="2025-08-01 08:00" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除排程 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['待排程','已排程','执行中','已完成']
const data = ref([
  { code:'SCH-001',moCode:'MO-2025-0045',product:'减速器 SA67',operation:'车削加工',machine:'车床C6140',operator:'张工',hours:4,startTime:'2025-08-01 08:00',status:'执行中',remark:'',sc:'running'},
  { code:'SCH-002',moCode:'MO-2025-0045',product:'减速器 SA67',operation:'钻孔',machine:'钻床Z3050',operator:'李工',hours:2,startTime:'2025-08-01 13:00',status:'已排程',remark:'',sc:'scheduled'},
  { code:'SCH-003',moCode:'MO-2025-0044',product:'电动滚筒 5.5kW',operation:'焊接',machine:'焊机MIG-500',operator:'王工',hours:6,startTime:'2025-08-05 08:00',status:'已排程',remark:'',sc:'scheduled'},
  { code:'SCH-004',moCode:'MO-2025-0042',product:'螺旋输送机 LS400',operation:'装配',machine:'装配线A',operator:'赵工',hours:8,startTime:'2025-07-25 08:00',status:'已完成',remark:'',sc:'completed'},
  { code:'SCH-005',moCode:'MO-2025-0043',product:'皮带输送机 10m',operation:'切割下料',machine:'激光切割机',operator:'',hours:3,startTime:'',status:'待排程',remark:'等待排程',sc:'pending'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.moCode.toLowerCase().includes(q)||m.product.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',moCode:'',product:'',operation:'',machine:'',operator:'',hours:0,startTime:'',status:'待排程',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`SCH-${String(data.value.length+1).padStart(3,'0')}`;f.moCode='';f.product='';f.operation='';f.machine='';f.operator='';f.hours=0;f.startTime='';f.status='待排程';f.remark=''}showForm.value=true}
function save(){if(!f.operation){alert('请填写工序名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pending{background:#f5f5f5;color:#999;}.erp-tag.scheduled{background:#e3f2fd;color:#1565c0;}
.erp-tag.running{background:#fff3e0;color:#e65100;}.erp-tag.completed{background:#e8f5e9;color:#2e7d32;}
</style>
