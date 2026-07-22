<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索入库单号、工单号..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建入库单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">入库单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联工单</th><th>产品</th><th style="text-align:right;">入库数量</th>
          <th>仓库</th><th>入库日期</th><th>检验结果</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.moCode }}</td><td>{{ row.product }}</td>
            <td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td><td>{{ row.warehouse }}</td>
            <td class="erp-cell-spec">{{ row.date }}</td>
            <td><span :class="['erp-tag', row.qcClass]">{{ row.qcResult }}</span></td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑入库单':'新建入库单'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>入库单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联工单</label><input v-model="f.moCode" placeholder="MO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>产品名称 <span class="erp-form-req">*</span></label><input v-model="f.product" placeholder="产品名称" /></div>
        <div class="erp-form-group"><label>入库数量</label><input v-model.number="f.qty" type="number" min="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>台</option><option>套</option><option>件</option><option>个</option></select></div>
        <div class="erp-form-group"><label>仓库</label><input v-model="f.warehouse" placeholder="如 成品库" /></div>
        <div class="erp-form-group"><label>入库日期</label><input v-model="f.date" type="text" placeholder="2025-08-01" /></div>
        <div class="erp-form-group"><label>检验结果</label><select v-model="f.qcResult"><option value="合格">合格</option><option value="免检">免检</option></select></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除入库单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['待检验','已入库','已结算']
const data = ref([
  { code:'CMP-001',moCode:'MO-2025-0041',product:'减速器 SA67',qty:10,unit:'台',warehouse:'成品库',date:'2025-07-25',qcResult:'合格',status:'已入库',remark:'',sc:'done',qcClass:'pass'},
  { code:'CMP-002',moCode:'MO-2025-0045',product:'减速器 SA67',qty:5,unit:'台',warehouse:'成品库',date:'2025-08-02',qcResult:'合格',status:'待检验',remark:'',sc:'pending',qcClass:'pending'},
  { code:'CMP-003',moCode:'MO-2025-0042',product:'螺旋输送机 LS400',qty:1,unit:'台',warehouse:'成品库',date:'2025-07-28',qcResult:'合格',status:'已入库',remark:'',sc:'done',qcClass:'pass'},
  { code:'CMP-004',moCode:'MO-2025-0044',product:'电动滚筒 5.5kW',qty:5,unit:'台',warehouse:'成品库',date:'2025-08-06',qcResult:'合格',status:'已结算',remark:'工单关闭',sc:'settled',qcClass:'pass'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.moCode.toLowerCase().includes(q)||m.product.includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',moCode:'',product:'',qty:1,unit:'台',warehouse:'',date:'',qcResult:'合格',status:'待检验',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`CMP-${String(data.value.length+1).padStart(3,'0')}`;f.moCode='';f.product='';f.qty=1;f.unit='台';f.warehouse='';f.date='';f.qcResult='合格';f.status='待检验';f.remark=''}showForm.value=true}
function save(){if(!f.product){alert('请填写产品名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pass{background:#e8f5e9;color:#2e7d32;}.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.done{background:#e3f2fd;color:#1565c0;}.erp-tag.settled{background:#f3e5f5;color:#6a1b9a;}
</style>
