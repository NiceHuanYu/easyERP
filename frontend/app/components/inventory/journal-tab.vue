<template><div class="erp-tab-body">
<div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索物料、单号..." class="erp-search-input"/></div>
<select v-model="ft" class="erp-filter-select"><option value="">全部类型</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>时间</th><th>物料</th><th>类型</th><th style="text-align:right;">数量变化</th><th>仓库</th><th>关联单号</th><th>操作人</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.id"><td class="erp-cell-spec">{{ r.time }}</td><td>{{ r.material }}</td><td><span :class="['erp-tag',r.typeClass]">{{ r.type }}</span></td><td :class="['erp-cell-num',r.qty>0?'in':'out']">{{ r.qty>0?'+':'' }}{{ r.qty }}</td><td>{{ r.wh }}</td><td class="erp-cell-spec">{{ r.ref }}</td><td>{{ r.op }}</td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无流水</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/></div></template>
<script setup lang="ts">
const types = ['采购入库','生产入库','销售出库','生产领料','调拨入库','调拨出库','盘点调整']
const data = ref([
  { id:1,time:'2025-08-01 09:15',material:'45#圆钢 Φ20',type:'生产领料',qty:-120,wh:'原材料库',ref:'MTL-001',op:'张工',typeClass:'out'},
  { id:2,time:'2025-08-01 09:20',material:'M8×30六角螺栓',type:'生产领料',qty:-200,wh:'辅料库',ref:'MTL-002',op:'张工',typeClass:'out'},
  { id:3,time:'2025-08-01 10:00',material:'减速器 SA67',type:'生产入库',qty:5,wh:'成品库',ref:'CMP-002',op:'王工',typeClass:'in'},
  { id:4,time:'2025-07-30 14:00',material:'304不锈钢板 2mm',type:'调拨入库',qty:5,wh:'钢材专用库',ref:'TRF-001',op:'刘洋',typeClass:'in'},
  { id:5,time:'2025-07-30 14:00',material:'304不锈钢板 2mm',type:'调拨出库',qty:-5,wh:'原材料主库',ref:'TRF-001',op:'刘洋',typeClass:'out'},
  { id:6,time:'2025-07-28 11:30',material:'壳体-减速器上盖',type:'销售出库',qty:-2,wh:'成品库',ref:'SHP-001',op:'王强',typeClass:'out'},
  { id:7,time:'2025-07-25 16:00',material:'减速器 SA67',type:'生产入库',qty:10,wh:'成品库',ref:'CMP-001',op:'王工',typeClass:'in'},
  { id:8,time:'2025-07-25 16:30',material:'Q235槽钢 10#',type:'盘点调整',qty:-1,wh:'原材料库',ref:'CT-002',op:'刘洋',typeClass:'adj'},
  { id:9,time:'2025-07-25 09:00',material:'Q235槽钢 10#',type:'采购入库',qty:20,wh:'原材料库',ref:'ARV-001',op:'周明',typeClass:'in'},
  { id:10,time:'2025-07-24 08:00',material:'45#圆钢 Φ20',type:'采购入库',qty:500,wh:'原材料库',ref:'PO-2025-0055',op:'周明',typeClass:'in'},
])
const s=ref('');const ft=ref('');const page=ref(1);const ps=ref(10)
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.material.includes(q)||m.ref.toLowerCase().includes(q));if(ft.value)l=l.filter(m=>m.type===ft.value);return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,ft],()=>page.value=1)
</script>
<style scoped>.erp-cell-num.in{color:#2e7d32;}.erp-cell-num.out{color:#c62828;}.erp-tag.in{background:#e8f5e9;color:#2e7d32;}.erp-tag.out{background:#fce4ec;color:#c62828;}.erp-tag.adj{background:#fff3e0;color:#e65100;}</style>