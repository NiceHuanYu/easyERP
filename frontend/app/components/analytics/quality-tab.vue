<template><div class="erp-tab-body"><div class="kpi-grid"><div v-for="k in kpis" :key="k.label" class="kpi-card"><div class="kpi-icon" :style="{background:k.bg,color:k.color}">{{ k.icon }}</div><div><div class="kpi-val">{{ k.val }}</div><div class="kpi-lbl">{{ k.label }}</div></div></div></div>
<div class="erp-toolbar" style="margin-top:16px;"><h3 style="font-size:15px;color:#333;margin:0;">📋 各环节合格率</h3></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>检验环节</th><th style="text-align:right;">检验批数</th><th style="text-align:right;">合格批数</th><th>合格率</th></tr></thead>
<tbody><tr v-for="r in rates" :key="r.stage"><td>{{ r.stage }}</td><td class="erp-cell-num">{{ r.total }}</td><td class="erp-cell-num">{{ r.pass }}</td><td :class="['erp-cell-num',r.rate>=95?'good':r.rate>=85?'warn':'bad']">{{ r.rate }}%</td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const kpis = [{icon:'🔍',label:'来料合格率',val:'96.5%',bg:'#e3f2fd',color:'#1565c0'},{icon:'⚙️',label:'过程合格率',val:'98.2%',bg:'#e8f5e9',color:'#2e7d32'},{icon:'📦',label:'出货合格率',val:'99.5%',bg:'#fff3e0',color:'#e65100'},{icon:'⚠️',label:'不合格品',val:'3项',bg:'#fce4ec',color:'#c62828'}]
const rates = [{stage:'IQC来料检验',total:48,pass:46,rate:95.8},{stage:'IPQC过程检验',total:156,pass:153,rate:98.1},{stage:'OQC出货检验',total:32,pass:32,rate:100}]
</script>
<style scoped>.kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}.kpi-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.kpi-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.kpi-val{font-size:20px;font-weight:700;color:#333;}.kpi-lbl{font-size:12px;color:#888;}
.good{color:#2e7d32;}.warn{color:#e65100;}.bad{color:#c62828;}
</style>