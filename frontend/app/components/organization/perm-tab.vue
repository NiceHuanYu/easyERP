<template><div class="tab-body"><div class="toolbar"><h3 style="font-size:15px;color:#333;margin:0;">模块权限配置</h3></div>
<div class="table-wrap"><table class="table"><thead><tr><th>模块</th><th v-for="r in roles" :key="r" style="text-align:center;">{{ r }}</th></tr></thead>
<tbody><tr v-for="m in modules" :key="m.name"><td>{{ m.name }}</td><td v-for="r in roles" :key="r" style="text-align:center;"><span :class="m[r]==='✅'?'granted':''">{{ m[r] }}</span></td></tr></tbody></table></div></div></template>
<script setup lang="ts">
const roles = ['管理员','销售员','采购员','生产主管','仓库管理员','质检员','财务']
const modules = [
  { name:'基础资料',管理员:'✅',销售员:'✅',采购员:'✅',生产主管:'✅',仓库管理员:'✅',质检员:'✅',财务:'✅'},
  { name:'销售管理',管理员:'✅',销售员:'✅',采购员:'❌',生产主管:'❌',仓库管理员:'❌',质检员:'❌',财务:'✅'},
  { name:'采购管理',管理员:'✅',销售员:'❌',采购员:'✅',生产主管:'❌',仓库管理员:'❌',质检员:'❌',财务:'✅'},
  { name:'生产管理',管理员:'✅',销售员:'❌',采购员:'❌',生产主管:'✅',仓库管理员:'❌',质检员:'❌',财务:'❌'},
  { name:'库存管理',管理员:'✅',销售员:'❌',采购员:'❌',生产主管:'✅',仓库管理员:'✅',质检员:'❌',财务:'❌'},
  { name:'品质管理',管理员:'✅',销售员:'❌',采购员:'❌',生产主管:'❌',仓库管理员:'❌',质检员:'✅',财务:'❌'},
  { name:'财务管理',管理员:'✅',销售员:'❌',采购员:'❌',生产主管:'❌',仓库管理员:'❌',质检员:'❌',财务:'✅'},
  { name:'组织管理',管理员:'✅',销售员:'❌',采购员:'❌',生产主管:'❌',仓库管理员:'❌',质检员:'❌',财务:'❌'},
  { name:'数据分析',管理员:'✅',销售员:'✅',采购员:'✅',生产主管:'✅',仓库管理员:'✅',质检员:'✅',财务:'✅'},
]
</script>
<style scoped>.tab-body{display:flex;flex-direction:column;flex:1;}.toolbar{display:flex;align-items:center;margin-bottom:16px;}
.table-wrap{flex:1;overflow-y:auto;border:1px solid #f0f0f0;border-radius:8px;}.table{width:100%;border-collapse:collapse;font-size:13px;}.table thead{position:sticky;top:0;z-index:1;}
.table th{background:#fafafa;padding:10px 12px;text-align:left;color:#666;font-weight:600;font-size:12px;border-bottom:1px solid #e0e0e0;}
.table td{padding:10px 12px;border-bottom:1px solid #f5f5f5;color:#333;text-align:left;}.table tbody tr:hover{background:#f8faff;}
.granted{font-weight:600;}
</style>