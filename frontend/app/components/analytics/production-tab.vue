<template><div class="erp-tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div></div></div></div>
<div class="erp-toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 本月工单达成率</h3></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>工单号</th><th>产品</th><th style="text-align:right;">计划数</th><th style="text-align:right;">完工数</th><th>达成率</th><th>状态</th></tr></thead>
<tbody><tr v-for="r in orders" :key="r.code"><td class="erp-cell-code">{{ r.code }}</td><td>{{ r.product }}</td><td class="erp-cell-num">{{ r.plan }}</td><td class="erp-cell-num">{{ r.done }}</td><td class="erp-cell-num" :class="r.rate>=90?'good':r.rate>=70?'warn':'bad'">{{ r.rate }}%</td><td><span :class="['erp-tag',r.sc]">{{ r.status }}</span></td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'🏭',label:'本月工单数',val:'12单',bg:'#e3f2fd',color:'#1565c0'},{icon:'✅',label:'完工率',val:'75%',bg:'#e8f5e9',color:'#2e7d32'},{icon:'⏱️',label:'设备稼动率',val:'82%',bg:'#fff3e0',color:'#e65100'},{icon:'⚠️',label:'不良率',val:'2.3%',bg:'#fce4ec',color:'#c62828'}]
const orders = [{code:'MO-2025-0041',product:'减速器 SA67',plan:10,done:10,rate:100,status:'已完工',sc:'completed'},{code:'MO-2025-0042',product:'螺旋输送机 LS400',plan:3,done:1,rate:33,status:'生产中',sc:'running'},{code:'MO-2025-0045',product:'减速器 SA67',plan:20,done:8,rate:40,status:'生产中',sc:'running'},{code:'MO-2025-0044',product:'电动滚筒 5.5kW',plan:10,done:0,rate:0,status:'已下达',sc:'released'}]
</script>
<style scoped>.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}
.good{color:#2e7d32;}.warn{color:#e65100;}.bad{color:#c62828;}
.completed{background:#e8f5e9;color:#2e7d32;}.running{background:#fff3e0;color:#e65100;}.released{background:#e3f2fd;color:#1565c0;}
</style>