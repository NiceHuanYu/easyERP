<template>
  <div class="tab-body">
    <div class="overview-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{background:stat.bg,color:stat.color}">{{ stat.icon }}</div>
        <div class="stat-info"><span class="stat-val">{{ stat.val }}</span><span class="stat-lbl">{{ stat.label }}</span></div>
      </div>
    </div>
    <div class="toolbar"><h3 style="font-size:15px;color:#333;margin:0;">各仓库库存</h3></div>
    <div class="table-wrap"><table class="table"><thead><tr><th>仓库</th><th>类型</th><th style="text-align:right;">物料种类</th><th style="text-align:right;">总库存量</th><th style="text-align:right;">库存金额(元)</th></tr></thead>
    <tbody><tr v-for="r in warehouses" :key="r.name"><td>{{ r.name }}</td><td><span class="tag">{{ r.type }}</span></td><td class="num">{{ r.categories }}</td><td class="num">{{ r.qty }}</td><td class="num">{{ r.amount.toLocaleString() }}</td></tr></tbody></table></div>
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
.tab-body{display:flex;flex-direction:column;flex:1;gap:16px;}
.overview-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:14px;}
.stat-card{background:#fff;border-radius:10px;padding:18px;display:flex;align-items:center;gap:14px;box-shadow:0 1px 3px rgba(0,0,0,.06);}
.stat-icon{width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0;}
.stat-info{display:flex;flex-direction:column;}.stat-val{font-size:22px;font-weight:700;color:#333;}.stat-lbl{font-size:12px;color:#888;margin-top:2px;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}
.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;}.table tbody tr:hover{background:#f8faff;}
.num{text-align:right;font-family:'SFMono','Consolas',monospace;}
.tag{display:inline-block;padding:2px 10px;border-radius:10px;font-size:11px;background:#e8f0fe;color:#1a73e8;}
</style>
