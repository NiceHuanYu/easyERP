<template>
  <div class="erp-tab-body">
    <!-- 内部标签：待审批 / 已审批 -->
    <div class="erp-sub-tabs">
      <button :class="['erp-tab-btn',{active:view==='pending'}]" @click="view='pending'">待审批 {{ pending.length }}</button>
      <button :class="['erp-tab-btn',{active:view==='done'}]" @click="view='done'">已审批 {{ done.length }}</button>
    </div>

    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索订单号、客户..." class="erp-search-input" /></div>
      </div>
    </div>

    <!-- 待审批列表 -->
    <template v-if="view==='pending'">
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr>
            <th>订单号</th><th>客户</th><th>产品摘要</th><th style="text-align:right;">金额(元)</th><th>交期</th><th style="text-align:center;">操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in paged" :key="row.code">
              <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.customer }}</td>
              <td class="erp-cell-spec">{{ row.summary }}</td>
              <td class="erp-cell-num">{{ row.amount.toFixed(2) }}</td>
              <td class="erp-cell-spec">{{ row.delivery }}</td>
              <td class="erp-cell-acts">
                <button class="erp-lnk" @click="showDetail(row)">详情</button>
                <button class="erp-lnk" style="color:#2e7d32;" @click="openApprove(row,true)">✓ 通过</button>
                <button class="erp-lnk erp-lnk-danger" @click="openApprove(row,false)">✕ 驳回</button>
              </td>
            </tr>
            <tr v-if="paged.length===0"><td colspan="6" class="erp-cell-empty">暂无待审批的销售订单</td></tr>
          </tbody>
        </table>
      </div>
      <PaginationBar :total="pendingFiltered.length" v-model="page" v-model:page-size="ps" />
    </template>

    <!-- 已审批列表 -->
    <template v-else>
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr>
            <th>订单号</th><th>客户</th><th>产品摘要</th><th style="text-align:right;">金额</th><th>结果</th><th>审批时间</th><th>意见</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in donePaged" :key="row.code">
              <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.customer }}</td>
              <td class="erp-cell-spec">{{ row.summary }}</td>
              <td class="erp-cell-num">{{ row.amount.toFixed(2) }}</td>
              <td><span :class="['erp-tag',row.result==='通过'?'pass':'reject']">{{ row.result }}</span></td>
              <td class="erp-cell-spec">{{ row.time }}</td>
              <td class="erp-cell-spec">{{ row.comment||'-' }}</td>
            </tr>
            <tr v-if="donePaged.length===0"><td colspan="7" class="erp-cell-empty">暂无已审批记录</td></tr>
          </tbody>
        </table>
      </div>
      <PaginationBar :total="doneFiltered.length" v-model="dPage" v-model:page-size="ps" />
    </template>

    <!-- 详情弹窗 -->
    <FormModal :show="showD" :title="'订单详情 - '+dItem?.code" @close="showD=false" @save="showD=false">
      <div v-if="dItem" class="erp-form-grid">
        <div class="erp-form-group"><label>订单号</label><input :value="dItem.code" disabled /></div>
        <div class="erp-form-group"><label>客户</label><input :value="dItem.customer" disabled /></div>
        <div class="erp-form-group full"><label>产品摘要</label><input :value="dItem.summary" disabled /></div>
        <div class="erp-form-group"><label>金额(元)</label><input :value="dItem.amount.toFixed(2)" disabled /></div>
        <div class="erp-form-group"><label>交期</label><input :value="dItem.delivery" disabled /></div>
      </div>
    </FormModal>

    <!-- 审批意见弹窗 -->
    <ApproveDialog :show="showApprove" :isApprove="isApprove" :code="aItem?.code||''" @cancel="showApprove=false" @confirm="doApprove" />
  </div>
</template>

<script setup lang="ts">
interface Order { code:string;customer:string;summary:string;amount:number;delivery:string }
interface DoneItem extends Order { result:string;time:string;comment:string }

const pending = ref<Order[]>([
  { code:'SO-2025-0089',customer:'华强电子',summary:'减速器 SA67 等2项',amount:14250,delivery:'2025-08-15' },
  { code:'SO-2025-0083',customer:'宏远机械',summary:'皮带输送机 10m',amount:32500,delivery:'2025-08-05' },
  { code:'SO-2025-0090',customer:'丰华重型',summary:'振动电机 YZO-8-4',amount:16500,delivery:'2025-08-20' },
])
const done = ref<DoneItem[]>([
  { code:'SO-2025-0086',customer:'华强电子',summary:'螺旋输送机 LS400',amount:37200,delivery:'2025-07-18',result:'通过',time:'2025-07-19 14:30',comment:'价格合理，同意' },
  { code:'SO-2025-0087',customer:'鑫达科技',summary:'皮带输送机 10m',amount:32500,delivery:'2025-07-25',result:'通过',time:'2025-07-26 09:15',comment:'' },
  { code:'SO-2025-0084',customer:'天工精密',summary:'刮板机配件-链节',amount:4250,delivery:'2025-07-30',result:'驳回',time:'2025-07-31 11:00',comment:'数量不足，请重新提交' },
])

const view=ref('pending');const s=ref('');const page=ref(1);const dPage=ref(1);const ps=ref(10)

const pendingFiltered=computed(()=>{let l=[...pending.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q));return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return pendingFiltered.value.slice(s2,s2+ps.value)})
const doneFiltered=computed(()=>{let l=[...done.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.includes(q));return l})
const donePaged=computed(()=>{const s2=(dPage.value-1)*ps.value;return doneFiltered.value.slice(s2,s2+ps.value)})
watch(s,()=>{page.value=1;dPage.value=1})

// 详情
const showD=ref(false);const dItem=ref<Order|null>(null)
function showDetail(row:Order){dItem.value=row;showD.value=true}

// 审批
const showApprove=ref(false);const isApprove=ref(true);const aItem=ref<Order|null>(null)
function openApprove(row:Order,approve:boolean){aItem.value=row;isApprove.value=approve;showApprove.value=true}
function doApprove(comment:string){
  if(!aItem.value)return
  const now=new Date().toLocaleString('zh-CN',{year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'})
  done.value.unshift({...aItem.value,result:isApprove.value?'通过':'驳回',time:now,comment})
  pending.value=pending.value.filter(m=>m.code!==aItem.value!.code)
  showApprove.value=false;aItem.value=null
}
</script>

<style scoped>
.erp-sub-tabs{display:flex;gap:4px;background:#fff;border-radius:10px;padding:6px;margin-bottom:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.erp-tab-btn{display:flex;align-items:center;gap:6px;padding:8px 16px;border:none;border-radius:8px;background:transparent;color:#666;font-size:13px;cursor:pointer;transition:all .15s;}
.erp-tab-btn:hover{background:#f5f7fa;color:#333;}
.erp-tab-btn.active{background:#1a73e8;color:#fff;font-weight:500;}
.erp-tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}
.erp-tag.pass{background:#e8f5e9;color:#2e7d32;}
.erp-tag.reject{background:#fce4ec;color:#c62828;}
</style>
