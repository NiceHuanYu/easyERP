<template><div class="tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div><div class="kpi-trend" :class="k.up?'up':'down'">{{ k.up?'↑':'↓' }}{{ k.percent }}</div></div></div></div>
<div class="toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 产品销量排行</h3></div>
<div class="table-wrap"><table class="table"><thead><tr><th>排名</th><th>产品</th><th style="text-align:right;">销量</th><th style="text-align:right;">销售额</th><th>占比</th></tr></thead>
<tbody><tr v-for="(r,i) in topProducts" :key="r.name"><td class="num">{{ i+1 }}</td><td>{{ r.name }}</td><td class="num">{{ r.qty }}</td><td class="num">{{ r.revenue.toFixed(0) }}</td><td class="num">{{ r.ratio }}%</td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'📈',label:'本月销售额',val:'¥86.5万',bg:'#e3f2fd',color:'#1565c0',up:true,percent:'15%'},{icon:'📄',label:'本月订单数',val:'42单',bg:'#e8f5e9',color:'#2e7d32',up:true,percent:'8%'},{icon:'👥',label:'活跃客户',val:'18家',bg:'#fff3e0',color:'#e65100',up:true,percent:'5%'},{icon:'📊',label:'平均客单价',val:'¥20,595',bg:'#f3e5f5',color:'#6a1b9a',up:false,percent:'3%'}]
const topProducts = [{name:'皮带输送机 10m',qty:3,revenue:97500,ratio:18.1},{name:'螺旋输送机 LS400',qty:5,revenue:93000,ratio:17.3},{name:'减速器 SA67',qty:12,revenue:34200,ratio:6.4},{name:'电动滚筒 5.5kW',qty:5,revenue:34000,ratio:6.3},{name:'振动电机 YZO-8-4',qty:10,revenue:16500,ratio:3.1}]
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;gap:16px;}
.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}.kpi-trend{font-size:11px;}.kpi-trend.up{color:#2e7d32;}.kpi-trend.down{color:#c62828;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.num{text-align:right;font-weight:600;}
</style>