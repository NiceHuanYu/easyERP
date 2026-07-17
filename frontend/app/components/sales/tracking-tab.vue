<template>
  <div class="tab-body">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box"><span class="search-icon">🔍</span><input v-model="s" type="text" placeholder="搜索订单号、客户..." class="search-input" /></div>
        <select v-model="fs" class="filter-select"><option value="">全部状态</option><option v-for="st in ss" :key="st" :value="st">{{ st }}</option></select>
      </div>
    </div>
    <div class="tracking-list">
      <div v-for="row in paged" :key="row.code" class="track-card">
        <div class="track-header">
          <div>
            <span class="track-code">{{ row.code }}</span>
            <span class="track-customer">{{ row.customer }}</span>
          </div>
          <span :class="['tag', row.sc]">{{ row.status }}</span>
        </div>
        <div class="track-progress">
          <div v-for="(step, i) in steps" :key="i"
            :class="['step', { done: i <= stepIndex(row.status), current: i === stepIndex(row.status) && row.status !== '已完成' && row.status !== '已取消' }]">
            <div class="step-dot"></div>
            <div class="step-label">{{ step }}</div>
          </div>
        </div>
        <div class="track-meta">
          <span>物料：{{ row.material }}</span>
          <span>数量：{{ row.qty }}{{ row.unit }}</span>
          <span>金额：¥{{ row.amount.toFixed(2) }}</span>
          <span>交期：{{ row.delivery }}</span>
        </div>
      </div>
      <div v-if="paged.length===0" class="empty">暂无跟踪数据</div>
    </div>
    <PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps" />
  </div>
</template>

<script setup lang="ts">
const ss = ['草稿','待审核','已审核','生产中','待发货','已发货','已完成','已取消']
const steps = ['下单','审核','生产','发货','签收','完成']
const stepMap: Record<string,number> = { '草稿':-1,'待审核':0,'已审核':1,'生产中':2,'待发货':3,'已发货':4,'已完成':5,'已取消':-1 }
function stepIndex(status:string) { return stepMap[status] ?? -1 }

const data = ref([
  { code:'SO-2025-0089',customer:'华强电子',material:'减速器 SA67',qty:5,unit:'台',amount:14250,delivery:'2025-08-15',status:'待审核',remark:'',sc:'pending'},
  { code:'SO-2025-0088',customer:'精密模具',material:'电动滚筒 5.5kW',qty:3,unit:'台',amount:20400,delivery:'2025-08-10',status:'生产中',remark:'',sc:'progress'},
  { code:'SO-2025-0087',customer:'鑫达科技',material:'皮带输送机 10m',qty:1,unit:'套',amount:32500,delivery:'2025-07-25',status:'已发货',remark:'',sc:'shipped'},
  { code:'SO-2025-0086',customer:'华强电子',material:'螺旋输送机 LS400',qty:2,unit:'台',amount:37200,delivery:'2025-07-18',status:'已完成',remark:'',sc:'completed'},
  { code:'SO-2025-0085',customer:'丰华重型',material:'振动电机 YZO-8-4',qty:10,unit:'台',amount:16500,delivery:'2025-08-20',status:'草稿',remark:'',sc:'draft'},
  { code:'SO-2025-0083',customer:'宏远机械',material:'皮带输送机 10m',qty:1,unit:'套',amount:32500,delivery:'2025-08-05',status:'已审核',remark:'',sc:'approved'},
])
const s=ref('');const fs=ref('');const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.code.toLowerCase().includes(q)||m.customer.toLowerCase().includes(q));if(fs.value)l=l.filter(m=>m.status===fs.value);return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
</script>
<style scoped>
.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}.toolbar-left{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}
.search-box{display:flex;align-items:center;background:#f5f7fa;border-radius:8px;padding:0 12px;border:1px solid #e0e0e0;}.search-box:focus-within{border-color:#1a73e8;}.search-icon{font-size:14px;margin-right:6px;}.search-input{border:none;background:transparent;padding:8px 0;font-size:13px;outline:none;width:200px;color:#333;}.search-input::placeholder{color:#bbb;}
.filter-select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:8px;background:#fafafa;font-size:13px;color:#555;outline:none;cursor:pointer;}
.tracking-list{flex:1;overflow-y:auto;display:flex;flex-direction:column;gap:14px;padding-bottom:14px;}
.track-card{background:#fff;border-radius:10px;padding:20px;box-shadow:0 1px 3px rgba(0,0,0,.06);border:1px solid #f0f0f0;}
.track-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:18px;}
.track-code{font-family:'SFMono','Consolas',monospace;font-size:15px;font-weight:600;color:#1a73e8;}
.track-customer{margin-left:12px;font-size:13px;color:#666;}
.tag{display:inline-block;padding:3px 12px;border-radius:12px;font-size:12px;font-weight:500;}
.tag.draft{background:#f5f5f5;color:#999;}.tag.pending{background:#fff3e0;color:#e65100;}.tag.approved{background:#e3f2fd;color:#1565c0;}
.tag.progress{background:#e8f5e9;color:#2e7d32;}.tag.shipped{background:#e0f2f1;color:#00695c;}
.tag.completed{background:#f3e5f5;color:#6a1b9a;}.tag.cancelled{background:#fce4ec;color:#c62828;}
.track-progress{display:flex;align-items:center;margin-bottom:16px;padding:0 4px;}
.step{display:flex;flex-direction:column;align-items:center;flex:1;position:relative;}
.step:not(:last-child)::after{content:'';position:absolute;top:10px;left:50%;width:100%;height:2px;background:#e0e0e0;z-index:0;}
.step.done:not(:last-child)::after{background:#1a73e8;}
.step-dot{width:20px;height:20px;border-radius:50%;background:#e0e0e0;z-index:1;position:relative;transition:all .3s;}
.step.done .step-dot{background:#1a73e8;box-shadow:0 0 0 3px #e3f2fd;}
.step.current .step-dot{background:#1a73e8;animation:pulse 2s infinite;}
@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(26,115,232,.4)}70%{box-shadow:0 0 0 6px rgba(26,115,232,0)}100%{box-shadow:0 0 0 0 rgba(26,115,232,0)}}
.step-label{font-size:11px;color:#999;margin-top:6px;white-space:nowrap;}
.step.done .step-label{color:#1a73e8;font-weight:500;}
.step.current .step-label{color:#1a73e8;font-weight:600;}
.track-meta{display:flex;gap:20px;font-size:12px;color:#888;flex-wrap:wrap;padding-top:12px;border-top:1px solid #f5f5f5;}
.empty{text-align:center;color:#bbb;padding:40px 0!important;font-size:14px;}
</style>
