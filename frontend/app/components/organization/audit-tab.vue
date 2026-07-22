<template><div class="erp-tab-body">
<div class="erp-toolbar"><div class="erp-toolbar-left"><div class="erp-search-box"><span class="erp-search-icon">🔍</span><input v-model="s" placeholder="搜索操作人、模块..." class="erp-search-input"/></div>
<select v-model="fs" class="erp-filter-select"><option value="">全部级别</option><option>信息</option><option>警告</option><option>错误</option></select></div></div>
<div class="erp-table-wrap"><table class="erp-table"><thead><tr><th>时间</th><th>操作人</th><th>模块</th><th>操作类型</th><th>操作内容</th><th>级别</th><th>IP地址</th></tr></thead>
<tbody><tr v-for="r in paged" :key="r.id"><td class="erp-cell-spec">{{ r.time }}</td><td>{{ r.user }}</td><td>{{ r.module }}</td><td>{{ r.action }}</td><td class="erp-cell-spec">{{ r.detail }}</td><td><span :class="['erp-tag',r.sc]">{{ r.level }}</span></td><td class="erp-cell-spec">{{ r.ip }}</td></tr>
<tr v-if="paged.length===0"><td colspan="7" class="erp-cell-empty">暂无日志</td></tr></tbody></table></div>
<PaginationBar :total="filtered.length" v-model="page" v-model:page-size="ps"/></div></template>
<script setup lang="ts">
const data = ref([
  { id:1,time:'2025-08-01 09:10',user:'admin',module:'基础资料',action:'新增',detail:'创建物料 MAT-016',level:'信息',ip:'192.168.1.100',sc:'info'},
  { id:2,time:'2025-08-01 08:30',user:'zhangsan',module:'销售管理',action:'审核',detail:'审核销售订单 SO-2025-0089',level:'信息',ip:'192.168.1.101',sc:'info'},
  { id:3,time:'2025-07-31 16:20',user:'lisi',module:'采购管理',action:'新增',detail:'创建采购订单 PO-2025-0056',level:'信息',ip:'192.168.1.102',sc:'info'},
  { id:4,time:'2025-07-31 14:00',user:'wangwu',module:'生产管理',action:'下达',detail:'下达生产工单 MO-2025-0045',level:'信息',ip:'192.168.1.103',sc:'info'},
  { id:5,time:'2025-07-30 11:00',user:'admin',module:'组织管理',action:'登录失败',detail:'密码错误尝试',level:'警告',ip:'192.168.1.200',sc:'warn'},
  { id:6,time:'2025-07-30 10:30',user:'zhaoliu',module:'库存管理',action:'删除',detail:'删除盘点单 CT-005',level:'警告',ip:'192.168.1.105',sc:'warn'},
  { id:7,time:'2025-07-29 09:00',user:'system',module:'系统',action:'错误',detail:'数据库连接超时',level:'错误',ip:'-',sc:'error'},
])
const s=ref('');const fs=ref('');const page=ref(1);const ps=ref(10)
const filtered=computed(()=>{let l=[...data.value];const q=s.value.trim().toLowerCase();if(q)l=l.filter(m=>m.user.includes(q)||m.module.includes(q)||m.action.includes(q));if(fs.value)l=l.filter(m=>m.level===fs.value);return l})
const paged=computed(()=>{const s2=(page.value-1)*ps.value;return filtered.value.slice(s2,s2+ps.value)})
watch([s,fs],()=>page.value=1)
</script>
<style scoped>.erp-tag.info{background:#e3f2fd;color:#1565c0;}.erp-tag.warn{background:#fff3e0;color:#e65100;}.erp-tag.error{background:#fce4ec;color:#c62828;}
</style>