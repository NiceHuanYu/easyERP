<template><div class="erp-tab-body"><div class="erp-toolbar"><h3 style="font-size:15px;color:#333;margin:0;">模块权限配置</h3></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>模块</th><th v-for="r in roles" :key="r" style="text-align:center;">{{ r }}</th></tr></thead>
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
<style scoped>.granted{font-weight:600;}
</style>