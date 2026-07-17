<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索订单号、客户..." class="search-input" /></div>
        <select v-model="fs" class="filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建订单</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">订单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('customer')" class="sortable">客户{{ sf==='customer'?(sa?'▲':'▼'):'' }}</th>
          <th>物料名称</th><th style="text-align:right;">数量</th><th style="text-align:right;">金额(元)</th>
          <th>交期</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="code">{{ row.code }}</td><td>{{ row.customer }}</td>
            <td class="spec">{{ row.material }}</td><td class="num">{{ row.qty }}{{ row.unit }}</td>
            <td class="num">{{ row.amount.toFixed(2) }}</td>
            <td class="spec">{{ row.delivery }}</td>
            <td><span :class="['tag', row.sc]">{{ row.status }}</span></td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑订单':'新建订单'" @close="showForm=false" @save="save">
      <div class="grid">
        <div class="fg"><label>订单号</label><input v-model="f.code" :disabled="!editing" /></div>
        <div class="fg"><label>客户 <span class="req">*</span></label><input v-model="f.customer" placeholder="客户名称" /></div>
        <div class="fg"><label>物料/产品 <span class="req">*</span></label><input v-model="f.material" placeholder="产品名称" /></div>
        <div class="fg"><label>数量</label><input v-model.number="f.qty" type="number" min="1" placeholder="1" /></div>
        <div class="fg"><label>单位</label><select v-model="f.unit"><option>台</option><option>套</option><option>件</option><option>个</option></select></div>
        <div class="fg"><label>金额 (元)</label><input v-model.number="f.amount" type="number" step="0.01" placeholder="0.00" /></div>
        <div class="fg"><label>交货日期</label><input v-model="f.delivery" type="text" placeholder="2025-08-15" /></div>
        <div class="fg"><label>状态</label><select v-model="f.status"><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除订单 <strong>{{ dt?.code }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审核','已审核','生产中','待发货','已发货','已完成','已取消']
const data = ref([
  { code:'SO-2025-0089',customer:'华强电子',material:'减速器 SA67',qty:5,unit:'台',amount:14250,delivery:'2025-08-15',status:'待审核',remark:'',sc:'pending'},
  { code:'SO-2025-0088',customer:'精密模具',material:'电动滚筒 5.5kW',qty:3,unit:'台',amount:20400,delivery:'2025-08-10',status:'生产中',remark:'',sc:'progress'},
  { code:'SO-2025-0087',customer:'鑫达科技',material:'皮带输送机 10m',qty:1,unit:'套',amount:32500,delivery:'2025-07-25',status:'已发货',remark:'物流已发出',sc:'shipped'},
  { code:'SO-2025-0086',customer:'华强电子',material:'螺旋输送机 LS400',qty:2,unit:'台',amount:37200,delivery:'2025-07-18',status:'已完成',remark:'已签收',sc:'completed'},
  { code:'SO-2025-0085',customer:'丰华重型',material:'振动电机 YZO-8-4',qty:10,unit:'台',amount:16500,delivery:'2025-08-20',status:'草稿',remark:'等待确认',sc:'draft'},
  { code:'SO-2025-0084',customer:'天工精密',material:'刮板机配件-链节',qty:50,unit:'件',amount:4250,delivery:'2025-07-30',status:'已取消',remark:'客户取消',sc:'cancelled'},
  { code:'SO-2025-0083',customer:'宏远机械',material:'皮带输送机 10m',qty:1,unit:'套',amount:32500,delivery:'2025-08-05',status:'已审核',remark:'加急处理',sc:'approved'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',customer:'',material:'',qty:1,unit:'台',amount:0,delivery:'',status:'草稿',remark:''});let ec=''
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;f.code=`SO-${new Date().getFullYear()}-${String(data.value.filter(m=>m.code.startsWith('SO-')).length+1).padStart(4,'0')}`;f.customer='';f.material='';f.qty=1;f.unit='台';f.amount=0;f.delivery='';f.status='草稿';f.remark=''}showForm.value=true}
function save(){if(!f.customer||!f.material){alert('请填写客户和物料');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}
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
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}
.tag.draft{background:#f5f5f5;color:#999;}.tag.pending{background:#fff3e0;color:#e65100;}.tag.approved{background:#e3f2fd;color:#1565c0;}
.tag.progress{background:#e8f5e9;color:#2e7d32;}.tag.shipped{background:#e0f2f1;color:#00695c;}
.tag.completed{background:#f3e5f5;color:#6a1b9a;}.tag.cancelled{background:#fce4ec;color:#c62828;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;cursor:not-allowed;}.fg textarea{resize:vertical;font-family:inherit;}
</style>
