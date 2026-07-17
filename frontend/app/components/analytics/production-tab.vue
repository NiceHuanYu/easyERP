<template><div class="tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div></div></div></div>
<div class="toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 本月工单达成率</h3></div>
<div class="table-wrap"><table class="table"><thead><tr><th>工单号</th><th>产品</th><th style="text-align:right;">计划数</th><th style="text-align:right;">完工数</th><th>达成率</th><th>状态</th></tr></thead>
<tbody><tr v-for="r in orders" :key="r.code"><td class="code">{{ r.code }}</td><td>{{ r.product }}</td><td class="num">{{ r.plan }}</td><td class="num">{{ r.done }}</td><td class="num" :class="r.rate>=90?'good':r.rate>=70?'warn':'bad'">{{ r.rate }}%</td><td><span :class="['tag',r.sc]">{{ r.status }}</span></td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'🏭',label:'本月工单数',val:'12单',bg:'#e3f2fd',color:'#1565c0'},{icon:'✅',label:'完工率',val:'75%',bg:'#e8f5e9',color:'#2e7d32'},{icon:'⏱️',label:'设备稼动率',val:'82%',bg:'#fff3e0',color:'#e65100'},{icon:'⚠️',label:'不良率',val:'2.3%',bg:'#fce4ec',color:'#c62828'}]
const orders = [{code:'MO-2025-0041',product:'减速器 SA67',plan:10,done:10,rate:100,status:'已完工',sc:'completed'},{code:'MO-2025-0042',product:'螺旋输送机 LS400',plan:3,done:1,rate:33,status:'生产中',sc:'running'},{code:'MO-2025-0045',product:'减速器 SA67',plan:20,done:8,rate:40,status:'生产中',sc:'running'},{code:'MO-2025-0044',product:'电动滚筒 5.5kW',plan:10,done:0,rate:0,status:'已下达',sc:'released'}]
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;gap:16px;}
.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.code{font-family:'SFMono','Consolas',monospace;font-size:12px;color:#1a73e8;}
.num{text-align:right;font-weight:600;}.good{color:#2e7d32;}.warn{color:#e65100;}.bad{color:#c62828;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}.completed{background:#e8f5e9;color:#2e7d32;}.running{background:#fff3e0;color:#e65100;}.released{background:#e3f2fd;color:#1565c0;}
</style>