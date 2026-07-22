<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索BOM编号、产品名称..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建BOM</button></div>
    </div>

    <!-- BOM 列表表格 -->
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">BOM编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('productName')" class="sortable">产品名称{{ sf==='productName'?(sa?'▲':'▼'):'' }}</th>
          <th>产品编码</th><th>版本</th><th style="text-align:right;">用量</th><th>单位</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-name">{{ row.productName }}</td>
            <td class="erp-cell-spec">{{ row.productCode }}</td><td>V{{ row.version }}</td>
            <td class="erp-cell-num">{{ row.qty }}</td><td>{{ row.unit }}</td>
            <td><span :class="['erp-dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="viewItems(row)">明细</button>
              <button class="erp-lnk" @click="openForm(row)">编辑</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button>
            </td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <!-- BOM 明细弹窗 -->
    <FormModal :show="showDetail" :title="'BOM明细 - '+detailTitle" @close="showDetail=false" @save="showDetail=false">
      <div style="margin-bottom:12px;font-size:13px;color:#888;">以下物料组成该产品：</div>
      <table class="erp-table detail-table">
        <thead><tr><th>物料编码</th><th>物料名称</th><th>规格型号</th><th style="text-align:right;">用量</th><th>单位</th><th>备注</th></tr></thead>
        <tbody>
          <tr v-for="item in detailItems" :key="item.matCode">
            <td class="erp-cell-code">{{ item.matCode }}</td><td>{{ item.matName }}</td><td class="erp-cell-spec">{{ item.spec }}</td>
            <td class="erp-cell-num">{{ item.qty }}</td><td>{{ item.unit }}</td><td class="erp-cell-spec">{{ item.remark||'-' }}</td>
          </tr>
          <tr v-if="detailItems.length===0"><td colspan="6" class="erp-cell-empty">暂无明细</td></tr>
        </tbody>
      </table>
    </FormModal>

    <!-- BOM 表单弹窗 -->
    <FormModal :show="showForm" :title="editing?'编辑BOM':'新建BOM'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>BOM编号 <span class="erp-form-req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>产品编码 <span class="erp-form-req">*</span></label><input v-model="f.productCode" placeholder="如 PRD-001" /></div>
        <div class="erp-form-group full"><label>产品名称 <span class="erp-form-req">*</span></label><input v-model="f.productName" placeholder="请输入产品名称" /></div>
        <div class="erp-form-group"><label>生产数量</label><input v-model.number="f.qty" type="number" min="1" placeholder="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>台</option><option>套</option><option>件</option><option>个</option></select></div>
        <div class="erp-form-group"><label>版本号</label><input v-model="f.version" type="text" placeholder="1.0" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
      <div style="margin-top:16px;padding-top:14px;border-top:1px solid #f0f0f0;">
        <label style="font-size:13px;color:#555;font-weight:500;">物料明细</label>
        <div v-for="(item,idx) in f.items" :key="idx" class="item-row">
          <input v-model="item.matCode" placeholder="物料编码" style="width:110px;" />
          <input v-model="item.matName" placeholder="物料名称" style="width:140px;" />
          <input v-model="item.spec" placeholder="规格" style="width:100px;" />
          <input v-model.number="item.qty" type="number" step="0.01" placeholder="用量" style="width:70px;" />
          <select v-model="item.unit" style="width:60px;"><option v-for="u in ['个','件','kg','m','张','只','套']" :key="u">{{ u }}</option></select>
          <button class="erp-lnk erp-lnk-danger" @click="f.items.splice(idx,1)" style="padding:4px;">✕</button>
        </div>
        <button class="btn-add" @click="f.items.push({matCode:'',matName:'',spec:'',qty:1,unit:'个',remark:''})">＋ 添加物料</button>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除BOM <strong>{{ dt?.code }} - {{ dt?.productName }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const data = ref([
  { code:'BOM-001',productCode:'PRD-001',productName:'减速器 SA67',version:'1.0',qty:1,unit:'台',status:'启用',remark:'',items:[{matCode:'MAT-001',matName:'45#圆钢 Φ20',spec:'Φ20×6000mm',qty:12,unit:'kg',remark:''},{matCode:'MAT-004',matName:'M8×30六角螺栓',spec:'M8×30 8.8级',qty:24,unit:'个',remark:''},{matCode:'MAT-007',matName:'铜排 TMY-40×4',spec:'40×4mm',qty:2,unit:'m',remark:''}] },
  { code:'BOM-002',productCode:'PRD-002',productName:'电动滚筒 5.5kW',version:'2.0',qty:1,unit:'台',status:'启用',remark:'',items:[{matCode:'MAT-003',matName:'Q235槽钢 10#',spec:'10# 6m/根',qty:1,unit:'根',remark:'底座'},{matCode:'MAT-002',matName:'304不锈钢板 2mm',spec:'2×1200×2400mm',qty:0.5,unit:'张',remark:''},{matCode:'MAT-010',matName:'乳化切削液 18L',spec:'18L/桶',qty:0.2,unit:'桶',remark:'测试用量'}] },
  { code:'BOM-003',productCode:'PRD-003',productName:'皮带输送机 10m',version:'1.0',qty:1,unit:'套',status:'启用',remark:'标准10米机型',items:[{matCode:'MAT-003',matName:'Q235槽钢 10#',spec:'10# 6m/根',qty:4,unit:'根',remark:'机架'},{matCode:'MAT-005',matName:'Φ50×3无缝钢管',spec:'Φ50×3×6000mm',qty:2,unit:'根',remark:'滚筒'},{matCode:'MAT-010',matName:'乳化切削液 18L',spec:'18L/桶',qty:0.5,unit:'桶',remark:''}] },
  { code:'BOM-004',productCode:'PRD-006',productName:'刮板机配件-链节',version:'1.0',qty:1,unit:'件',status:'停用',remark:'待更新',items:[{matCode:'MAT-001',matName:'45#圆钢 Φ20',spec:'Φ20×6000mm',qty:1.5,unit:'kg',remark:''}] },
])

const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.productName.toLowerCase().includes(q)||m.productCode.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)

// 明细查看
const showDetail=ref(false);const detailTitle=ref('');const detailItems=ref<any[]>([])
function viewItems(row:any){detailTitle.value=row.productName;detailItems.value=row.items||[];showDetail.value=true}

// 表单
const numberingMode = ref('auto')
const showForm=ref(false);const editing=ref(false)
const f=reactive({code:'',productCode:'',productName:'',version:'1.0',qty:1,unit:'台',status:'启用',remark:'',items:[] as any[]})
let ec=''
function openForm(item?:any){
  if(item){editing.value=true;ec=item.code;Object.assign(f,{...item,items:JSON.parse(JSON.stringify(item.items||[]))})}
  else{editing.value=false;numberingMode.value='auto';f.code=`BOM-${String(data.value.length+1).padStart(3,'0')}`;f.productCode='';f.productName='';f.version='1.0';f.qty=1;f.unit='台';f.status='启用';f.remark='';f.items=[]}
  showForm.value=true
}
function save(){
  if(!f.productName||!f.productCode){alert('请填写产品信息');return}
  const copy={...f,items:JSON.parse(JSON.stringify(f.items))}
  if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]=copy as any}
  else data.value.push(copy as any)
  showForm.value=false
}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>

<style scoped>
/* === 组件特有样式 === */
.btn-add{margin-top:8px;padding:6px 14px;border:1px dashed #1a73e8;border-radius:6px;background:transparent;color:#1a73e8;font-size:12px;cursor:pointer;}.btn-add:hover{background:#f0f4ff;}
.detail-table{margin-top:8px;}.detail-table th{background:#f5f7fa;}
.item-row{display:flex;align-items:center;gap:6px;margin-top:6px;}.item-row input,.item-row select{padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}.item-row input:focus,.item-row select:focus{border-color:#1a73e8;}
</style>
