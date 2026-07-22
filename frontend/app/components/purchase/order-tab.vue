<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索订单号、供应商..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建订单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">订单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('supplier')" class="sortable">供应商{{ sf==='supplier'?(sa?'▲':'▼'):'' }}</th>
          <th>物料</th><th style="text-align:right;">数量</th><th style="text-align:right;">金额(元)</th><th>交货日期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.supplier }}</td>
            <td class="erp-cell-spec">{{ row.material }}</td><td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td>
            <td class="erp-cell-num">{{ row.amount.toFixed(2) }}</td>
            <td class="erp-cell-spec">{{ row.delivery }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑订单':'新建订单'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>订单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>供应商 <span class="erp-form-req">*</span></label><input v-model="f.supplier" placeholder="供应商名称" /></div>
        <div class="erp-form-group"><label>物料 <span class="erp-form-req">*</span></label><input v-model="f.material" placeholder="物料名称" /></div>
        <div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" min="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>个</option><option>件</option><option>kg</option><option>张</option><option>根</option><option>桶</option></select></div>
        <div class="erp-form-group"><label>金额 (元)</label><input v-model.number="f.amount" type="number" step="0.01" placeholder="0.00" /></div>
        <div class="erp-form-group"><label>交货日期</label><input v-model="f.delivery" type="text" placeholder="2025-08-20" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除订单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待确认','已确认','已发货','已到货','已完成','已取消']
const data = ref([
  { code:'PO-2025-0056',supplier:'宏远钢铁',material:'45#圆钢 Φ20',qty:500,unit:'kg',amount:2400,delivery:'2025-08-10',status:'待确认',remark:'',sc:'pending'},
  { code:'PO-2025-0055',supplier:'宏远钢铁',material:'Q235槽钢 10#',qty:20,unit:'根',amount:704,delivery:'2025-07-25',status:'已到货',remark:'',sc:'arrived'},
  { code:'PO-2025-0054',supplier:'正泰电气',material:'电动滚筒 5.5kW',qty:3,unit:'台',amount:15600,delivery:'2025-08-05',status:'已确认',remark:'',sc:'confirmed'},
  { code:'PO-2025-0053',supplier:'东明标准件',material:'M8×30六角螺栓',qty:2000,unit:'个',amount:900,delivery:'2025-08-15',status:'草稿',remark:'',sc:'draft'},
  { code:'PO-2025-0052',supplier:'宏达橡塑',material:'O型密封圈 Φ50×3.5',qty:1000,unit:'只',amount:180,delivery:'2025-07-28',status:'已完成',remark:'',sc:'completed'},
  { code:'PO-2025-0051',supplier:'宏远钢铁',material:'铜排 TMY-40×4',qty:50,unit:'m',amount:4300,delivery:'2025-08-01',status:'已发货',remark:'物流在途',sc:'shipped'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.supplier.includes(q)||m.material.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',supplier:'',material:'',qty:1,unit:'个',amount:0,delivery:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`PO-${new Date().getFullYear()}-${String(data.value.filter(m=>m.code.startsWith('PO-')).length+1).padStart(4,'0')}`;f.supplier='';f.material='';f.qty=1;f.unit='个';f.amount=0;f.delivery='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.supplier||!f.material){alert('请填写供应商和物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.confirmed{background:#e3f2fd;color:#1565c0;}.erp-tag.shipped{background:#e0f2f1;color:#00695c;}
.erp-tag.arrived{background:#e8f5e9;color:#2e7d32;}.erp-tag.completed{background:#f3e5f5;color:#6a1b9a;}.erp-tag.cancelled{background:#fce4ec;color:#c62828;}
</style>
