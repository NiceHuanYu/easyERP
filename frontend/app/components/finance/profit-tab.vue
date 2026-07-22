<template><div class="erp-tab-body"><div class="erp-toolbar"><h3 style="font-size:15px;color:#333;margin:0;">订单利润分析</h3></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th @click="sort('code')" class="sortable">订单号{{ sf==='code'?(sa?'▲':'▼'):'' }}</th><th>客户</th><th>产品</th><th style="text-align:right;">销售收入</th><th style="text-align:right;">销售成本</th><th style="text-align:right;">毛利</th><th>毛利率</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.customer }}</td><td class="erp-cell-spec">{{ r.product }}</td><td class="erp-cell-num">{{ r.revenue.toFixed(2) }}</td><td class="erp-cell-num">{{ r.cost.toFixed(2) }}</td><td class="erp-cell-num" :class="r.profit>=0?'profit':'loss'">{{ r.profit.toFixed(2) }}</td><td :class="r.margin>=0?'profit':'loss'">{{ (r.margin*100).toFixed(1) }}%</td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无数据</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/></div></template>
<script setup lang="ts">
const data = ref([
  { code:'SO-2025-0086',customer:'华强电子',product:'螺旋输送机 LS400',revenue:37200,cost:26800,profit:10400,margin:0.2796},
  { code:'SO-2025-0087',customer:'鑫达科技',product:'皮带输送机 10m',revenue:32500,cost:26800,profit:5700,margin:0.1754},
  { code:'SO-2025-0088',customer:'精密模具',product:'电动滚筒 5.5kW',revenue:6800,cost:5200,profit:1600,margin:0.2353},
  { code:'SO-2025-0089',customer:'华强电子',product:'减速器 SA67',revenue:2850,cost:2600,profit:250,margin:0.0877},
  { code:'SO-2025-0085',customer:'丰华重型',product:'振动电机 YZO-8-4',revenue:16500,cost:12800,profit:3700,margin:0.2242},
])
const sf=ref('code');const sa=ref(true);const page=ref(1);const ps=ref(10)
function sort(f:string){if(sf.value===f)sa.value=!sa.value;else{sf.value=f;sa.value=true}}
const filtered=computed(()=>{let l=[...data.value];l.sort((a,b)=>{const av=a[sf.value as keyof typeof a],bv=b[sf.value as keyof typeof b];if(typeof av==='number'&&typeof bv==='number')return sa.value?av-bv:bv-av;return sa.value?String(av).localeCompare(String(bv)):String(bv).localeCompare(String(av))});return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
</script>
<style scoped>.erp-toolbar{justify-content:flex-start;flex-wrap:nowrap;gap:0;}.erp-cell-num{font-weight:600;}.profit{color:#2e7d32;}.loss{color:#c62828;}</style>