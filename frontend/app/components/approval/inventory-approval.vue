<template>
  <div class="erp-tab-body">
    <!-- 内部标签：待审批 / 已审批 -->
    <div class="erp-sub-tabs">
      <button :class="['erp-tab-btn',{active:view==='pending'}]" @click="view='pending'">待审批 {{ pending.length }}</button>
      <button :class="['erp-tab-btn',{active:view==='done'}]" @click="view='done'">已审批 {{ done.length }}</button>
    </div>

    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索调拨单号、物料..." class="erp-search-input" /></div>
      </div>
    </div>

    <!-- 待审批列表 -->
    <template v-if="view==='pending'">
      <div class="erp-table-wrap">
        <table class="erp-table">
          <thead><tr>
            <th>调拨单号</th><th>物料</th><th style="text-align:right;">数量</th><th>单位</th><th>调出仓库</th><th>调入仓库</th><th style="text-align:center;">操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in paged" :key="row.code">
              <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.material }}</td>
              <td class="erp-cell-num">{{ row.qty }}</td><td>{{ row.unit }}</td>
              <td>{{ row.from }}</td><td>{{ row.to }}</td>
              <td class="erp-cell-acts">
                <button class="erp-lnk" @click="showDetail(row)">详情</button>
                <button class="erp-lnk" style="color:#2e7d32;" @click="openApprove(row,true)">✓ 通过</button>
                <button class="erp-lnk erp-lnk-danger" @click="openApprove(row,false)">✕ 驳回</button>
              </td>
            </tr>
            <tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无待审批的调拨单</td></tr>
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
            <th>调拨单号</th><th>物料</th><th style="text-align:right;">数量</th><th>调出仓库</th><th>调入仓库</th><th>结果</th><th>审批时间</th><th>意见</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in donePaged" :key="row.code">
              <td class="erp-cell-code">{{ row.code }}</td><td>{{ row.material }}</td>
              <td class="erp-cell-num">{{ row.qty }}{{ row.unit }}</td>
              <td>{{ row.from }}</td><td>{{ row.to }}</td>
              <td><span :class="['erp-tag',row.result==='通过'?'pass':'reject']">{{ row.result }}</span></td>
              <td class="erp-cell-spec">{{ row.time }}</td>
              <td class="erp-cell-spec">{{ row.comment||'-' }}</td>
            </tr>
            <tr v-if="donePaged.length===0"><td colspan="8" class="erp-cell-empty">暂无已审批记录</td></tr>
          </tbody>
        </table>
      </div>
      <PaginationBar :total="doneFiltered.length" v-model="dPage" v-model:page-size="ps" />
    </template>

    <!-- 详情弹窗 -->
    <FormModal :show="showD" :title="'调拨详情 - '+dItem?.code" @close="showD=false" @save="showD=false">
      <div v-if="dItem" class="erp-form-grid">
        <div class="erp-form-group"><label>调拨单号</label><input :value="dItem.code" disabled /></div>
        <div class="erp-form-group"><label>物料</label><input :value="dItem.material" disabled /></div>
        <div class="erp-form-group"><label>数量</label><input :value="dItem.qty" disabled /></div>
        <div class="erp-form-group"><label>单位</label><input :value="dItem.unit" disabled /></div>
        <div class="erp-form-group"><label>调出仓库</label><input :value="dItem.from" disabled /></div>
        <div class="erp-form-group"><label>调入仓库</label><input :value="dItem.to" disabled /></div>
      </div>
    </FormModal>

    <!-- 审批意见弹窗 -->
    <ApproveDialog :show="showApprove" :isApprove="isApprove" :code="aItem?.code||''" @cancel="showApprove=false" @confirm="doApprove" />
  </div>
</template>

<script setup lang="ts">
interface Item { code:string;material:string;qty:number;unit:string;from:string;to:string }
interface DoneItem extends Item { result:string;time:string;comment:string }

const pending = ref<Item[]>([
  { code:'TR-001',material:'45#圆钢 Φ20',qty:200,unit:'kg',from:'原材料库',to:'生产车间库' },
  { code:'TR-002',material:'M8×30六角螺栓',qty:1000,unit:'个',from:'原材料库',to:'装配车间库' },
])
const done = ref<DoneItem[]>([
  { code:'TR-0003',material:'轴承 6205',qty:50,unit:'个',from:'原材料库',to:'维修车间库',result:'通过',time:'2025-07-20 10:15',comment:'合理调拨，同意' },
  { code:'TR-0004',material:'焊条 J422 Φ3.2',qty:300,unit:'kg',from:'原材料库',to:'生产车间库',result:'驳回',time:'2025-07-21 16:00',comment:'库存不足，暂缓调拨' },
])

const view=ref('pending');const s=ref('');const page=ref(1);const dPage=ref(1);const ps=ref(10)

const pendingFiltered=computed(()=>{let l=[...pending.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.material.includes(q));return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return pendingFiltered.value.slice(s2,s2+ps.value)})
const doneFiltered=computed(()=>{let l=[...done.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.material.includes(q));return l})
const donePaged=computed(()=>{const s2=(dPage.value-1)*ps.value;return doneFiltered.value.slice(s2,s2+ps.value)})
watch(s,()=>{page.value=1;dPage.value=1})

// 详情
const showD=ref(false);const dItem=ref<Item|null>(null)
function showDetail(row:Item){dItem.value=row;showD.value=true}

// 审批
const showApprove=ref(false);const isApprove=ref(true);const aItem=ref<Item|null>(null)
function openApprove(row:Item,approve:boolean){aItem.value=row;isApprove.value=approve;showApprove.value=true}
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
