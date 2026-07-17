<template><div class="tab-body">
<div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div><div class="kpi-trend" :class="k.up?'up':'down'">{{ k.up?'↑':'↓' }} {{ k.percent }}</div></div></div></div>
<div class="toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 近期经营数据</h3></div>
<div class="table-wrap"><table class="table"><thead><tr><th>指标</th><th>本月</th><th>上月</th><th>环比</th><th>年度累计</th></tr></thead>
<tbody><tr v-for="r in data" :key="r.name"><td>{{ r.name }}</td><td class="num">{{ r.month }}</td><td class="num">{{ r.last }}</td><td :class="['num',r.change>=0?'up':'down']">{{ r.change>=0?'+':'' }}{{ r.change }}%</td><td class="num">{{ r.year }}</td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [
  { icon:'💰',label:'本月销售额',val:'¥86.5万',bg:'#e3f2fd',color:'#1565c0',up:true,percent:'15%'},
  { icon:'📦',label:'订单数',val:'42单',bg:'#e8f5e9',color:'#2e7d32',up:true,percent:'8%'},
  { icon:'📤',label:'订单交付率',val:'92.5%',bg:'#fff3e0',color:'#e65100',up:true,percent:'3.2%'},
  { icon:'⚠️',label:'逾期订单',val:'3单',bg:'#fce4ec',color:'#c62828',up:false,percent:'12%'},
]
const data = [
  { name:'销售额(万元)',month:'86.5',last:'75.2',change:15,year:'528'},
  { name:'采购额(万元)',month:'52.3',last:'56.8',change:-8,year:'385'},
  { name:'生产产值(万元)',month:'71.2',last:'64.5',change:10,year:'452'},
  { name:'库存周转率',month:'3.2',last:'3.0',change:6.7,year:'-',yearNum:0},
  { name:'订单准时交付率',month:'92.5%',last:'89.3%',change:3.6,year:'-',yearNum:0},
  { name:'产品合格率',month:'98.2%',last:'97.5%',change:0.7,year:'-',yearNum:0},
]
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;gap:16px;}
.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:14px;}
.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}.kpi-trend{font-size:11px;margin-top:2px;}.kpi-trend.up{color:#2e7d32;}.kpi-trend.down{color:#c62828;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.num{text-align:right;font-weight:600;}.up{color:#2e7d32;}.down{color:#c62828;}
</style>