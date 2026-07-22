<template>
  <div class="erp-tab-body">
    <div class="overview-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{background:stat.bg,color:stat.color}">{{ stat.icon }}</div>
        <div class="stat-info"><span class="stat-val">{{ stat.val }}</span><span class="stat-lbl">{{ stat.label }}</span></div>
      </div>
    </div>
    <div class="erp-toolbar"><h3 style="font-size:15px;color:#333;margin:0;">各仓库库存</h3></div>
    <div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>仓库</th><th>类型</th><th style="text-align:right;">物料种类</th><th style="text-align:right;">总库存量</th><th style="text-align:right;">库存金额(元)</th></tr></thead>
    <tbody><tr v-for="r in warehouses" :key="r.name"><td>{{ r.name }}</td><td><span class="erp-tag">{{ r.type }}</span></td><td class="erp-cell-num">{{ r.categories }}</td><td class="erp-cell-num">{{ r.qty }}</td><td class="erp-cell-num">{{ r.amount.toLocaleString() }}</td></tr></tbody></table></div>
  </div>
</template>
<script setup lang="ts">
const stats = [
  { icon:'📦',label:'物料总数',val:'352种',bg:'#e3f2fd',color:'#1565c0' },
  { icon:'📥',label:'本月入库',val:'1,280件',bg:'#e8f5e9',color:'#2e7d32' },
  { icon:'📤',label:'本月出库',val:'960件',bg:'#fff3e0',color:'#e65100' },
  { icon:'⚠️',label:'低于安全库存',val:'5项',bg:'#fce4ec',color:'#c62828' },
]
const warehouses = [
  { name:'原材料主库',type:'原材料库',categories:86,qty:12500,amount:186000 },
  { name:'钢材专用库',type:'原材料库',categories:32,qty:45000,amount:328000 },
  { name:'半成品库',type:'半成品库',categories:28,qty:3200,amount:425000 },
  { name:'成品库',type:'成品库',categories:15,qty:680,amount:856000 },
  { name:'辅料库',type:'辅料库',categories:45,qty:8600,amount:58000 },
]
</script>
<style scoped>
.erp-tab-body{gap:16px;}
.overview-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}
.stat-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.stat-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.stat-info{display:flex;flex-direction:column;}.stat-val{font-size:22px;font-weight:700;color:#333;}.stat-lbl{font-size:12px;color:#888;margin-top:2px;}
</style>
