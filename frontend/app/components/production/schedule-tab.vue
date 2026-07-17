<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索工单号、产品..." class="search-input" /></div>
        <select v-model="fs" class="filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建排程</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">排程编号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th>关联工单</th><th>产品</th><th>工序</th><th>设备</th><th>操作员</th>
          <th>计划工时(h)</th><th>开始时间</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="code">{{ row.code }}</td><td class="spec">{{ row.moCode }}</td><td>{{ row.product }}</td>
            <td>{{ row.operation }}</td><td>{{ row.machine }}</td><td>{{ row.operator }}</td>
            <td class="num">{{ row.hours }}</td><td class="spec">{{ row.startTime }}</td>
            <td><span :class="['tag', row.sc]">{{ row.status }}</span></td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="10" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑排程':'新建排程'" @close="showForm=false" @save="save">
      <div class="grid">
        <div class="fg"><label>排程编号</label><input v-model="f.code" :disabled="!editing" /></div>
        <div class="fg"><label>关联工单</label><input v-model="f.moCode" placeholder="MO-2025-xxxx" /></div>
        <div class="fg"><label>产品名称</label><input v-model="f.product" placeholder="产品名称" /></div>
        <div class="fg"><label>工序名称</label><input v-model="f.operation" placeholder="如 切割" /></div>
        <div class="fg"><label>设备编号</label><input v-model="f.machine" placeholder="如 CNC-001" /></div>
        <div class="fg"><label>操作员</label><input v-model="f.operator" placeholder="姓名" /></div>
        <div class="fg"><label>计划工时 (h)</label><input v-model.number="f.hours" type="number" step="0.5" min="0" /></div>
        <div class="fg"><label>开始时间</label><input v-model="f.startTime" type="text" placeholder="2025-08-01 08:00" /></div>
        <div class="fg"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
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
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;f.code=`SCH-${String(data.value.length+1).padStart(3,'0')}`;f.moCode='';f.product='';f.operation='';f.machine='';f.operator='';f.hours=0;f.startTime='';f.status='待排程';f.remark=''}showForm.value=true}
function save(){if(!f.operation){alert('请填写工序名称');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}.toolbar-right{flex-shrink:0;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}.search-input::placeholder{color:#bbb;}
.filter-select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:8px;background:#fafafa;font-size:13px;color:#555;outline:none;cursor:pointer;}
.btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}.btn-primary{background:#1a73e8;color:#fff;}.btn-primary:hover{background:#1557b0;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;white-space:nowrap;}
.table th.sortable{cursor:pointer;user-select:none;}.table th.sortable:hover{background:#f0f4ff;color:#1a73e8;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}.spec{color:#666;font-size:12px;}.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}.tag.pending{background:#f5f5f5;color:#999;}.tag.scheduled{background:#e3f2fd;color:#1565c0;}
.tag.running{background:#fff3e0;color:#e65100;}.tag.completed{background:#e8f5e9;color:#2e7d32;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;cursor:not-allowed;}.fg textarea{resize:vertical;font-family:inherit;}
</style>
