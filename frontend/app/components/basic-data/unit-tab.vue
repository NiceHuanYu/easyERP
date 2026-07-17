<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索单位名称..." class="search-input" /></div>
        <select v-model="fc" class="filter-select"><option value="">全部分类</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select>
      </div>
      <div class="toolbar-right"><button class="btn btn-primary" @click="openForm()">＋ 新建单位</button></div>
    </div>
    <div class="table-wrap">
      <table class="table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">单位编码{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">单位名称{{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>单位分类</th><th>换算关系</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="code">{{ row.code }}</td><td>{{ row.name }}</td>
            <td><span class="tag">{{ row.category }}</span></td>
            <td class="spec">{{ row.conversion }}</td>
            <td><span :class="['dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="acts"><button class="lnk" @click="openForm(row)">编辑</button><button class="lnk dgr" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="6" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑计量单位':'新建计量单位'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="numbering-row">
        <label class="radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="grid">
        <div class="fg"><label>单位编码 <span class="req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="fg"><label>单位名称 <span class="req">*</span></label><input v-model="f.name" placeholder="如：千克" /></div>
        <div class="fg"><label>单位分类 <span class="req">*</span></label><select v-model="f.category"><option value="">请选择</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select></div>
        <div class="fg"><label>换算关系</label><input v-model="f.conversion" placeholder="如 1吨=1000千克" /></div>
        <div class="fg"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="fg full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除计量单位 <strong>{{ dt?.code }} - {{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const cats = ['数量','重量','长度','面积','体积','时间','其他']
const data = ref([
  { code:'UNIT-001',name:'个',category:'数量',conversion:'基本单位',status:'启用',remark:'' },
  { code:'UNIT-002',name:'件',category:'数量',conversion:'1件=1个',status:'启用',remark:'' },
  { code:'UNIT-003',name:'只',category:'数量',conversion:'1只=1个',status:'启用',remark:'' },
  { code:'UNIT-004',name:'套',category:'数量',conversion:'1套=N个(按BOM)',status:'启用',remark:'' },
  { code:'UNIT-005',name:'千克(kg)',category:'重量',conversion:'基本单位',status:'启用',remark:'' },
  { code:'UNIT-006',name:'克(g)',category:'重量',conversion:'1kg=1000g',status:'启用',remark:'' },
  { code:'UNIT-007',name:'吨(t)',category:'重量',conversion:'1t=1000kg',status:'启用',remark:'' },
  { code:'UNIT-008',name:'米(m)',category:'长度',conversion:'基本单位',status:'启用',remark:'' },
  { code:'UNIT-009',name:'厘米(cm)',category:'长度',conversion:'1m=100cm',status:'启用',remark:'' },
  { code:'UNIT-010',name:'毫米(mm)',category:'长度',conversion:'1m=1000mm',status:'启用',remark:'' },
  { code:'UNIT-011',name:'平方米(m²)',category:'面积',conversion:'基本单位',status:'启用',remark:'' },
  { code:'UNIT-012',name:'升(L)',category:'体积',conversion:'基本单位',status:'启用',remark:'' },
  { code:'UNIT-013',name:'毫升(ml)',category:'体积',conversion:'1L=1000ml',status:'启用',remark:'' },
  { code:'UNIT-014',name:'箱',category:'数量',conversion:'1箱=N个(按包装)',status:'启用',remark:'' },
  { code:'UNIT-015',name:'卷',category:'其他',conversion:'1卷=N米(按规格)',status:'启用',remark:'' },
  { code:'UNIT-016',name:'张',category:'数量',conversion:'1张=1个',status:'启用',remark:'' },
  { code:'UNIT-017',name:'片',category:'数量',conversion:'1片=1个',status:'启用',remark:'' },
  { code:'UNIT-018',name:'条',category:'数量',conversion:'1条=1个',status:'启用',remark:'' },
  { code:'UNIT-019',name:'盘',category:'其他',conversion:'1盘=N千克(按规格)',status:'启用',remark:'' },
  { code:'UNIT-020',name:'桶',category:'其他',conversion:'1桶=N升(按规格)',status:'启用',remark:'' },
  { code:'UNIT-021',name:'小时(h)',category:'时间',conversion:'基本单位',status:'启用',remark:'用于工时计量' },
])
const s=ref('');const fc=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.name.toLowerCase().includes(q)||m.code.toLowerCase().includes(q));if(fc.value)l=l.filter(m=>m.category===fc.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fc],()=>page.value=1)

const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',category:'',conversion:'',status:'启用',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`UNIT-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.category='';f.conversion='';f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name||!f.category){alert('请填写单位名称和分类');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}

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
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;background:#e8f0fe;color:#1a73e8;}
.spec{color:#666;font-size:12px;}
.dot{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:5px;background:#bbb;}.dot.on{background:#2e7d32;}
.acts{text-align:center;white-space:nowrap;}.lnk{background:none;border:none;font-size:12px;cursor:pointer;padding:4px 8px;color:#1a73e8;}.lnk:hover{text-decoration:underline;}.lnk.dgr{color:#d32f2f;}.lnk.dgr:hover{color:#b71c1c;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}.fg{display:flex;flex-direction:column;gap:4px;}.fg.full{grid-column:1/-1;}.fg label{font-size:13px;color:#555;font-weight:500;}.req{color:#d32f2f;}
.fg input,.fg select,.fg textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;transition:border-color .2s;}
.fg input:focus,.fg select:focus,.fg textarea:focus{border-color:#1a73e8;background:#fff;}.fg input:disabled{background:#f0f0f0;color:#999;cursor:not-allowed;}.fg textarea{resize:vertical;font-family:inherit;}
.numbering-row{display:flex;gap:24px;margin-bottom:16px;padding:10px 14px;background:#f8faff;border-radius:8px;border:1px solid #e0eeff;}
.radio-label{display:flex;align-items:center;gap:6px;font-size:13px;color:#555;cursor:pointer;}
.radio-label input[type="radio"]{accent-color:#1a73e8;}
</style>
