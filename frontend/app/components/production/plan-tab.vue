<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索计划编号、产品..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建计划</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">计划编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('product')" class="sortable">产品名称{{ sf==='product'?(sa?'▲':'▼'):'' }}</th>
          <th style="text-align:right;">计划数量</th><th style="text-align:right;">已完成</th><th>计划期间</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.product }}</td>
            <td class="erp-cell-num">{{ row.planQty }}</td><td class="erp-cell-num">{{ row.doneQty }}</td>
            <td class="erp-cell-spec">{{ row.period }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑计划':'新建计划'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>计划编号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>产品名称 <span class="erp-form-req">*</span></label><input v-model="f.product" placeholder="产品名称" /></div>
        <div class="erp-form-group"><label>计划数量</label><input v-model.number="f.planQty" type="number" min="1" /></div>
        <div class="erp-form-group"><label>已完成</label><input v-model.number="f.doneQty" type="number" min="0" :disabled="true" /></div>
        <div class="erp-form-group"><label>计划期间</label><input v-model="f.period" type="text" placeholder="2025年8月" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除计划 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','已发布','执行中','已完成','已关闭']
const data = ref([
  { code:'MPS-001',product:'减速器 SA67',planQty:50,doneQty:20,period:'2025年8月',status:'执行中',remark:'',sc:'running'},
  { code:'MPS-002',product:'电动滚筒 5.5kW',planQty:30,doneQty:0,period:'2025年8月',status:'已发布',remark:'',sc:'released'},
  { code:'MPS-003',product:'皮带输送机 10m',planQty:5,doneQty:5,period:'2025年7月',status:'已完成',remark:'',sc:'completed'},
  { code:'MPS-004',product:'螺旋输送机 LS400',planQty:8,doneQty:3,period:'2025年8月',status:'执行中',remark:'',sc:'running'},
  { code:'MPS-005',product:'振动电机 YZO-8-4',planQty:100,doneQty:0,period:'2025年9月',status:'草稿',remark:'',sc:'draft'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.product.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',product:'',planQty:1,doneQty:0,period:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`MPS-${String(data.value.length+1).padStart(3,'0')}`;f.product='';f.planQty=1;f.doneQty=0;f.period='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.product){alert('请填写产品名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.released{background:#e3f2fd;color:#1565c0;}
.erp-tag.running{background:#fff3e0;color:#e65100;}.erp-tag.completed{background:#e8f5e9;color:#2e7d32;}.erp-tag.closed{background:#fce4ec;color:#c62828;}
</style>
