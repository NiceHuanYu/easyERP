<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索退货单号、订单号..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建退货单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">退货单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联订单</th><th>客户</th><th>物料</th><th style="text-align:right;">数量</th>
          <th>退货原因</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.soCode }}</td>
            <td>{{ row.customer }}</td><td class="erp-cell-spec">{{ row.material }}</td><td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td>
            <td class="erp-cell-spec">{{ row.reason }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑退货单':'新建退货单'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>退货单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>关联订单</label><input v-model="f.soCode" placeholder="SO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>客户</label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="erp-form-group"><label>物料名称</label><input v-model="f.material" placeholder="产品名称" /></div>
        <div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" min="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>台</option><option>套</option><option>件</option><option>个</option></select></div>
        <div class="erp-form-group"><label>退货原因</label><input v-model="f.reason" placeholder="如 质量不合格" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除退货单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['待检验','已入库','已退款','已关闭']
const data = ref([
  { code:'RMA-001',soCode:'SO-2025-0085',customer:'丰华重型',material:'振动电机 YZO-8-4',qty:2,unit:'台',reason:'异响严重',status:'待检验',remark:'',sc:'pending'},
  { code:'RMA-002',soCode:'SO-2025-0084',customer:'天工精密',material:'刮板机配件-链节',qty:5,unit:'件',reason:'尺寸不符',status:'已入库',remark:'质检已通过',sc:'received'},
  { code:'RMA-003',soCode:'SO-2025-0086',customer:'华强电子',material:'螺旋输送机 LS400',qty:1,unit:'台',reason:'订单取消',status:'已退款',remark:'款项已退',sc:'refunded'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q)||m.soCode.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',soCode:'',customer:'',material:'',qty:1,unit:'台',reason:'',status:'待检验',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`RMA-${String(data.value.length+1).padStart(3,'0')}`;f.soCode='';f.customer='';f.material='';f.qty=1;f.unit='台';f.reason='';f.status='待检验';f.remark=''}showForm.value=true}
function save(){if(!f.customer){alert('请填写客户');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pending{background:#fff3e0;color:#e65100;}
.erp-tag.received{background:#e8f5e9;color:#2e7d32;}
.erp-tag.refunded{background:#e3f2fd;color:#1565c0;}
.erp-tag.closed{background:#f5f5f5;color:#999;}
</style>
