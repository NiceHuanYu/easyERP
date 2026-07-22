<template><div class="erp-tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div></div></div></div>
<div class="erp-toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 供应商交付表现</h3></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>供应商</th><th style="text-align:right;">订单数</th><th style="text-align:right;">采购额</th><th>准时率</th><th style="text-align:right;">平均交期(天)</th></tr></thead>
<tbody><tr v-for="r in supData" :key="r.name"><td>{{ r.name }}</td><td class="erp-cell-num">{{ r.orders }}</td><td class="erp-cell-num">{{ r.amount.toFixed(0) }}</td><td :class="['erp-cell-num',r.ontime>=90?'good':'warn']">{{ r.ontime }}%</td><td class="erp-cell-num">{{ r.avgDays }}</td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'📉',label:'本月采购额',val:'¥52.3万',bg:'#e3f2fd',color:'#1565c0'},{icon:'📋',label:'采购订单数',val:'28单',bg:'#e8f5e9',color:'#2e7d32'},{icon:'🚚',label:'供应商数',val:'12家',bg:'#fff3e0',color:'#e65100'},{icon:'⏱️',label:'平均交期',val:'5.2天',bg:'#f3e5f5',color:'#6a1b9a'}]
const supData = [{name:'宏远钢铁',orders:8,amount:125000,ontime:95,avgDays:3.5},{name:'正泰电气',orders:5,amount:82000,ontime:90,avgDays:5},{name:'东明标准件',orders:6,amount:28000,ontime:85,avgDays:4},{name:'宏达橡塑',orders:3,amount:12000,ontime:92,avgDays:6}]
</script>
<style scoped>.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}
.good{color:#2e7d32;}.warn{color:#e65100;}
</style>