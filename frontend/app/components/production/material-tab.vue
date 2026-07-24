<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索单号、工单号..." class="erp-search-input" /></div>
        <select v-model="ft" class="erp-filter-select"><option value="">全部类型</option><option value="领料">领料</option><option value="退料">退料</option></select>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建领/退料单</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>类型</th><th>关联工单</th><th>物料</th><th style="text-align:right;">数量</th>
          <th>仓库</th><th>申请人</th><th>日期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td>
            <td><span :class="['erp-tag', row.type==='领料'?'pick':'return']">{{ row.type }}</span></td>
            <td class="erp-cell-spec">{{ row.moCode }}</td><td class="erp-cell-spec">{{ row.material }}</td>
            <td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td><td>{{ row.warehouse }}</td>
            <td>{{ row.applicant }}</td><td class="erp-cell-spec">{{ row.date }}</td>
            <td><span :class="['erp-tag', row.sc]">{{ row.status }}</span></td>
            <td class="erp-cell-acts"><button v-if="row.status==='草稿'" class="erp-lnk" style="color:#2e7d32;" @click="submit(row)">提交</button><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="10" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑单据':'新建单据'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>单号</label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>类型</label><select v-model="f.type"><option value="领料">领料</option><option value="退料">退料</option></select></div>
        <div class="erp-form-group"><label>关联工单</label><input v-model="f.moCode" placeholder="MO-2025-xxxx" /></div>
        <div class="erp-form-group"><label>物料</label><input v-model="f.material" placeholder="物料名称" /></div>
        <div class="erp-form-group"><label>数量</label><input v-model.number="f.qty" type="number" step="0.01" min="0.01" /></div>
        <div class="erp-form-group"><label>单位</label><select v-model="f.unit"><option>个</option><option>件</option><option>kg</option><option>张</option><option>根</option></select></div>
        <div class="erp-form-group"><label>仓库</label><input v-model="f.warehouse" placeholder="如 原材料库" /></div>
        <div class="erp-form-group"><label>申请人</label><input v-model="f.applicant" placeholder="姓名" /></div>
        <div class="erp-form-group"><label>日期</label><input v-model="f.date" type="text" placeholder="2025-08-01" /></div>
        <div class="erp-form-group" v-if="editing"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除单据 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审核','已审核','已领用','已退回']
const data = ref([
  { code:'MTL-001',type:'领料',moCode:'MO-2025-0045',material:'45#圆钢 Φ20',qty:120,unit:'kg',warehouse:'原材料库',applicant:'张工',date:'2025-08-01',status:'已领用',remark:'',sc:'done'},
  { code:'MTL-002',type:'领料',moCode:'MO-2025-0045',material:'M8×30六角螺栓',qty:200,unit:'个',warehouse:'辅料库',applicant:'张工',date:'2025-08-01',status:'已审核',remark:'',sc:'approved'},
  { code:'MTL-003',type:'退料',moCode:'MO-2025-0042',material:'Q235槽钢 10#',qty:2,unit:'根',warehouse:'原材料库',applicant:'赵工',date:'2025-08-02',status:'已退回',remark:'余料退回',sc:'returned'},
  { code:'MTL-004',type:'领料',moCode:'MO-2025-0044',material:'304不锈钢板 2mm',qty:5,unit:'张',warehouse:'原材料库',applicant:'李工',date:'2025-08-05',status:'草稿',remark:'',sc:'draft'},
  { code:'MTL-005',type:'退料',moCode:'MO-2025-0041',material:'铜排 TMY-40×4',qty:3,unit:'m',warehouse:'原材料库',applicant:'王工',date:'2025-07-25',status:'已退回',remark:'',sc:'returned'},
])
const s=ref('');const ft=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.moCode.toLowerCase().includes(q)||m.material.includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',type:'领料',moCode:'',material:'',qty:1,unit:'个',warehouse:'',applicant:'',date:'',status:'草稿',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`MTL-${String(data.value.length+1).padStart(3,'0')}`;f.type='领料';f.moCode='';f.material='';f.qty=1;f.unit='个';f.warehouse='';f.applicant='';f.date='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.material){alert('请填写物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
function submit(row:any){const i=data.value.findIndex(m=>m.code===row.code);if(i!==-1){data.value[i].status='待审核';data.value[i].sc='pending-review'}}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.erp-tag.pick{background:#e3f2fd;color:#1565c0;}.erp-tag.return{background:#fff3e0;color:#e65100;}
.erp-tag.draft{background:#f5f5f5;color:#999;}.erp-tag.pending-review{background:#fff3e0;color:#e65100;}.erp-tag.approved{background:#e8f5e9;color:#2e7d32;}.erp-tag.done{background:#e8f5e9;color:#2e7d32;}.erp-tag.returned{background:#fce4ec;color:#c62828;}
</style>
