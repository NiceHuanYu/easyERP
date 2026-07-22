<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索供应商编码、名称..." class="erp-search-input" /></div>
        <select v-model="fc" class="erp-filter-select"><option value="">全部类别</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select>
        <select v-model="fs" class="erp-filter-select"><option value="">全部状态</option><option value="启用">启用</option><option value="停用">停用</option></select>
      </div>
      <div class="erp-toolbar-right"><button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建供应商</button></div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th @click="sort('code')" class="sortable">供应商编码{{ sf==='code'?(sa?'▲':'▼'):'' }}</th>
          <th @click="sort('name')" class="sortable">供应商名称{{ sf==='name'?(sa?'▲':'▼'):'' }}</th>
          <th>供货类别</th><th>状态</th><th style="text-align:center;">操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in paged" :key="row.code">
            <td class="erp-cell-code">{{ row.code }}</td><td class="erp-cell-name">{{ row.name }}</td>
            <td><span class="erp-tag">{{ row.category }}</span></td>
            <td><span :class="['erp-dot', row.status==='启用'?'on':'']"></span>{{ row.status }}</td>
            <td class="erp-cell-acts"><button class="erp-lnk" @click="openForm(row)">编辑</button><button class="erp-lnk erp-lnk-danger" @click="confirmDel(row)">删除</button></td>
          </tr>
          <tr v-if="paged.length===0"><td colspan="5" class="erp-cell-empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />

    <FormModal :show="showForm" :title="editing?'编辑供应商':'新建供应商'" @close="showForm=false" @save="save">
      <div v-if="!editing" class="erp-numbering-row">
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="auto" /><span>自动编号</span></label>
        <label class="erp-radio-label"><input type="radio" v-model="numberingMode" value="manual" /><span>手动编号</span></label>
      </div>
      <div class="erp-form-grid">
        <div class="erp-form-group"><label>供应商编码 <span class="erp-form-req">*</span></label><input v-model="f.code" :disabled="!editing && numberingMode === 'auto'" /></div>
        <div class="erp-form-group"><label>供应商名称 <span class="erp-form-req">*</span></label><input v-model="f.name" placeholder="请输入供应商名称" /></div>
        <div class="erp-form-group"><label>供货类别 <span class="erp-form-req">*</span></label><select v-model="f.category"><option value="">请选择</option><option v-for="c in cats" :key="c" :value="c">{{ c }}</option></select></div>
        <div class="erp-form-group full"><label>供应商地址</label><input v-model="f.address" placeholder="省/市/区/详细地址" /></div>
        <div class="erp-form-group"><label>状态</label><select v-model="f.status"><option value="启用">启用</option><option value="停用">停用</option></select></div>
        <div class="erp-form-group full"><label>备注</label><textarea v-model="f.remark" rows="2" placeholder="可选"></textarea></div>
      </div>
    </FormModal>

    <ConfirmDialog :show="showDel" title="确认删除" @confirm="doDel" @cancel="showDel=false">
      <p>确定要删除供应商 <strong>{{ dt?.code }} - {{ dt?.name }}</strong> 吗？</p>
    </ConfirmDialog>
  </div>
</template>

<script setup lang="ts">
const cats = ['钢材','电气','五金','橡塑','包装','化工','其他']
const data = ref([
  { code:'SUP-001',name:'宏远钢铁集团有限公司',category:'钢材',address:'深圳市宝安区',status:'启用',remark:'' },
  { code:'SUP-002',name:'正泰电气股份有限公司',category:'电气',address:'温州市乐清市',status:'启用',remark:'' },
  { code:'SUP-003',name:'东明标准件有限公司',category:'五金',address:'邯郸市永年区',status:'启用',remark:'' },
  { code:'SUP-004',name:'宏达橡塑制品厂',category:'橡塑',address:'常州市武进区',status:'启用',remark:'' },
  { code:'SUP-006',name:'宏远钢铁集团二分厂',category:'钢材',address:'东莞市塘厦镇',status:'停用',remark:'暂停供货' },
  { code:'SUP-007',name:'宏远钢铁集团三分厂',category:'钢材',address:'广州市增城区',status:'启用',remark:'' },
])
const s=ref('');const fc=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.name.toLowerCase().includes(q));if(fc.value)l=l.filter(m=>m.category===fc.value);if(fs.value)l=l.filter(m=>m.status===fs.value);l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fc,fs],()=>page.value=1)

const showForm=ref(false);const editing=ref(false);const f=reactive({code:'',name:'',category:'',address:'',status:'启用',remark:''});let ec=''
const numberingMode = ref('auto')
function openForm(item?:any){if(item){editing.value=true;ec=item.code;Object.assign(f,{...item})}else{editing.value=false;numberingMode.value='auto';f.code=`SUP-${String(data.value.length+1).padStart(3,'0')}`;f.name='';f.category='';f.address='';f.status='启用';f.remark=''}showForm.value=true}
function save(){if(!f.name||!f.category){alert('请填写供应商名称和供货类别');return}if(editing.value){const i=data.value.findIndex(m=>m.code===ec);if(i!==-1)data.value[i]={...f}as any}else data.value.push({...f}as any);showForm.value=false}

const showDel=ref(false);const dt=ref<any>(null)
function confirmDel(item:any){dt.value=item;showDel.value=true}
function doDel(){if(dt.value)data.value=data.value.filter(m=>m.code!==dt.value!.code);showDel.value=false;dt.value=null}
</script>
<style scoped>
/* 组件特有样式暂无，全部使用全局 erp-* 类 */
</style>
