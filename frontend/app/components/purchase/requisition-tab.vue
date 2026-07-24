<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索申请单号、物料..." class="erp-search-input" /></div>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建申请</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">申请单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>物料名称</th><th style="text-align:right;">数量</th><th>需求部门</th><th>期望交期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-spec">{{ row.material }}</td><td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td>
            <td>{{ row.dept }}</td><td class="erp-cell-spec">{{ row.expectDate }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk" style="color:#2e7d32;" v-if="row.status==='草稿'" @click="submit(row)">提交</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑申请':'新建申请'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>申请单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>物料名称 <span class="erp-form-req">*</span></label><input v-model="f.material" placeholder="物料名称" /></div>
        <div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" min="1" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>个</option><option>件</option><option>kg</option><option>张</option><option>根</option><option>桶</option></select></div>
        <div class="erp-form-group"><label>需求部门 <span class="erp-form-req">*</span></label><select v-model="f.dept"><option>生产部</option><option>品质部</option><option>设备部</option><option>仓库</option></select></div>
        <div class="erp-form-group"><label>期望交期</label><input v-model="f.expectDate" type="text" placeholder="2025-08-15" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="用途说明等"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除申请 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审批','已批准','已采购','已关闭']
const data = ref([
  { code:'PR-001',material:'45#圆钢 Φ20',qty:500,unit:'kg',dept:'生产部',expectDate:'2025-08-10',status:'待审批',remark:'',sc:'pending'},
  { code:'PR-002',material:'304不锈钢板 2mm',qty:20,unit:'张',dept:'生产部',expectDate:'2025-08-05',status:'已批准',remark:'',sc:'approved'},
  { code:'PR-003',material:'M8×30六角螺栓',qty:2000,unit:'个',dept:'生产部',expectDate:'2025-08-15',status:'草稿',remark:'库存补充',sc:'draft'},
  { code:'PR-004',material:'O型密封圈 Φ50×3.5',qty:1000,unit:'只',dept:'品质部',expectDate:'2025-08-20',status:'待审批',remark:'',sc:'pending'},
  { code:'PR-005',material:'乳化切削液 18L',qty:5,unit:'桶',dept:'设备部',expectDate:'2025-08-01',status:'已采购',remark:'',sc:'ordered'},
  { code:'PR-006',material:'焊丝 ER50-6 Φ1.2',qty:10,unit:'盘',dept:'生产部',expectDate:'2025-08-12',status:'已关闭',remark:'',sc:'closed'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.material.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',material:'',qty:1,unit:'个',dept:'生产部',expectDate:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`PR-${String(data.value.length+1).padStart(3,'0')}`;f.material='';f.qty=1;f.unit='个';f.dept='生产部';f.expectDate='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.material||!f.dept){alert('请填写物料和部门');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function submit(item:any){item.status='待审批';item.sc='pending'}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.pending{background:#fff3e0;color:#e65100;}.erp-tag.approved{background:#e8f5e9;color:#2e7d32;}.erp-tag.ordered{background:#e3f2fd;color:#1565c0;}.erp-tag.closed{background:#fce4ec;color:#c62828;}
</style>
