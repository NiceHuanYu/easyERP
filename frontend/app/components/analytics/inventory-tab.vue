<template><div class="tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div></div></div></div>
<div class="toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 库存ABC分类</h3></div>
<div class="table-wrap"><table class="table"><thead><tr><th>分类</th><th style="text-align:right;">物料数</th><th style="text-align:right;">库存金额</th><th>占比</th></tr></thead>
<tbody><tr v-for="r in abc" :key="r.cat"><td><span class="tag" :class="r.cat">{{ r.cat }}类</span></td><td class="num">{{ r.count }}</td><td class="num">{{ r.amount.toFixed(0) }}</td><td class="num">{{ r.ratio }}%</td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'📦',label:'库存物料总数',val:'352种',bg:'#e3f2fd',color:'#1565c0'},{icon:'💰',label:'库存总金额',val:'¥185.3万',bg:'#e8f5e9',color:'#2e7d32'},{icon:'🔄',label:'库存周转率',val:'3.2次',bg:'#fff3e0',color:'#e65100'},{icon:'⚠️',label:'呆滞物料',val:'12项',bg:'#fce4ec',color:'#c62828'}]
const abc = [{cat:'A',count:28,amount:1260000,ratio:68},{cat:'B',count:65,amount:420000,ratio:22.7},{cat:'C',count:259,amount:173000,ratio:9.3}]
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;gap:16px;}
.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}
.num{text-align:right;font-weight:600;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;}.A{background:#fce4ec;color:#c62828;}.B{background:#fff3e0;color:#e65100;}.C{background:#e8f5e9;color:#2e7d32;}
</style>