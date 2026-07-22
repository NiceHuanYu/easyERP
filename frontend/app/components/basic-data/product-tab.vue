<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="search" type="text" placeholder="搜索编码、名称..." class="erp-search-input" /></div>
        <select v-model="fc" class="erp-filter-select"><option value="">全部类别</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建产品</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">产品编码 {{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">产品名称 {{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>类别</th><th>规格型号</th><th>单位</th>
          <th @click="sort('salePrice')" class="sortable" style="text-align:right;">销售价(元){{ sf==='salePrice'?(sa?'▲':'▼'):'' }}</th>
          <th style="text-align:right;">成本价(元)</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-name">{{ row.name }}</td>
            <td><span class="erp-tag">{{ row.category }}</span></td><td class="erp-cell-spec">{{ row.spec }}</td><td>{{ row.unit }}</td>
            <td class="erp-cell-num">{{ row.salePrice.toFixed(2) }}</td><td class="erp-cell-num">{{ row.costPrice.toFixed(2) }}</td>
            <td><span :class="['erp-dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="9" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="pageSize" />

    <FormModal :show="showForm" :title="editing?'编辑产品':'新建产品'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>产品编码 <span class="erp-form-req">*</span></label><input v-model="f.code" type="text" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>产品名称 <span class="erp-form-req">*</span></label><input v-model="f.name" placeholder="请输入产品名称" /></div>
        <div class="erp-form-group"><label>产品类别 <span class="erp-form-req">*</span></label><select v-model="f.category"><option value="">请选择</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select></div>
        <div class="erp-form-group"><label>规格型号</label><input v-model="f.spec" placeholder="如 500W 220V" /></div>
        <div class="erp-form-group"><label>计量单位 <span class="erp-form-req">*</span></label><select v-model="f.unit"><option value="">请选择</option><option v-for="u in units" :key="u" :value="u">{{ u }}</option></select></div>
        <div class="erp-form-group"><label>销售价 (元)</label><input v-model.number="f.salePrice" type="number" step="0.01" min="0" placeholder="0.00" /></div>
        <div class="erp-form-group"><label>成本价 (元)</label><input v-model.number="f.costPrice" type="number" step="0.01" min="0" placeholder="0.00" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除产品 <strong>{{ dt?.code }} - {{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const cats = ['成品','半成品','服务']
const units = ['台','套','件','个','只','箱','条']
const data = ref([
  { code:'PRD-001',name:'减速器 SA67',category:'成品',spec:'SA67 i=23.45',unit:'台',salePrice:2850,costPrice:2100,status:'启用',remark:'' },
  { code:'PRD-002',name:'电动滚筒 5.5kW',category:'成品',spec:'5.5kW Φ500×650',unit:'台',salePrice:6800,costPrice:5200,status:'启用',remark:'' },
  { code:'PRD-003',name:'皮带输送机 10m',category:'成品',spec:'B800×10000mm',unit:'套',salePrice:32500,costPrice:26800,status:'启用',remark:'' },
  { code:'PRD-004',name:'螺旋输送机 LS400',category:'成品',spec:'LS400×8000mm',unit:'套',salePrice:18600,costPrice:15200,status:'启用',remark:'' },
  { code:'PRD-005',name:'斗式提升机 NE30',category:'成品',spec:'NE30×15m',unit:'套',salePrice:42000,costPrice:35600,status:'停用',remark:'改型中' },
  { code:'PRD-006',name:'刮板机配件-链节',category:'半成品',spec:'MS25 热处理',unit:'件',salePrice:85,costPrice:52,status:'启用',remark:'' },
  { code:'PRD-007',name:'振动电机 YZO-8-4',category:'成品',spec:'YZO-8-4 0.75kW',unit:'台',salePrice:1650,costPrice:1280,status:'启用',remark:'' },
  { code:'PRD-008',name:'设备安装调试',category:'服务',spec:'按项计价',unit:'项',salePrice:5000,costPrice:3800,status:'启用',remark:'含培训' },
])

const search = ref(''); const fc = ref(''); const fs = ref('')
const sf = ref('code'); const sa = ref(true)
const page = ref(1); const pageSize = ref(10)
function sort(f: string) { if (sf.value===f) sa.value=!sa.value; else { sf.value=f; sa.value=true } }

const filtered = computed(() => {
  let l = [...data.value]; const q = search.value.trim().toLowerCase()
  if (q) l = l.filter(m => m.code.toLowerCase().includes(q)||m.name.toLowerCase().includes(q))
  if (fc.value) l = l.filter(m => m.category===fc.value)
  if (fs.value) l = l.filter(m => m.status===fs.value)
  l.sort((a,b) => { const av=a[sf.value as keyof typeof a], bv=b[sf.value as keyof typeof b]; if (typeof av==='number'&&typeof bv==='number') return sa.value?av-bv:bv-av; return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av)) })
  return l
})
const paged = computed(() => { const s=(page.value-1)*pageSize.value; return filtered.value.slice(s,s+pageSize.value) })
watch([search,fc,fs],()=>page.value=1)

const showForm = ref(false); const editing = ref(false)
const f = reactive({ code:'', name:'', category:'', spec:'', unit:'', salePrice:0, costPrice:0, status:'启用', remark:'' })
let ec = ''
const numberingMode = ref('auto')
function openForm(item?:any) {
  if (item) { editing.value=true; ec=item.code; Object.assign(f,{...item}) }
  else { editing.value=false; numberingMode.value='auto'; f.code=`PRD-${String(data.value.length+1).padStart(3,'0')}`; f.name=''; f.category=''; f.spec=''; f.unit=''; f.salePrice=0; f.costPrice=0; f.status='启用'; f.remark='' }
  showForm.value=true
}
function save() {
  if (!f.name||!f.category||!f.unit) { alert('请填写产品名称、类别和计量单位'); return }
  if (editing.value) { const i=data.value.findIndex(m=>m.code===ec); if(i!==-1) data.value[i]={...f} as any }
  else data.value.push({...f} as any)
  showForm.value=false
}

const showDel = ref(false); const dt = ref<any>(null)
function confirmDel(item:any) { dt.value=item; showDel.value=true }
function doDel() { if(dt.value) data.value=data.value.filter(m=>m.code!==dt.value!.code); showDel.value=false; dt.value=null }
</script>

<style scoped>
</style>
