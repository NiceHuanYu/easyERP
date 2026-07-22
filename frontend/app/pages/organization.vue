<template><div class="erp-page"><div class="erp-tab-content"><div class="erp-tab-header"><h2>{{ currentTab?.label }}</h2><p class="erp-tab-desc">{{ currentTab?.description }}</p></div>
<OrganizationUserTab v-if="activeTab==='user'" /><OrganizationRoleTab v-else-if="activeTab==='role'" />
<OrganizationPermTab v-else-if="activeTab==='perm'" /><OrganizationAuditTab v-else-if="activeTab==='audit'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const route=useRoute()
const activeTab=computed(()=>(route.query.tab as string)||'user')
const tabs=[{key:'user',icon:'👤',label:'用户管理',description:'管理系统用户账号、基本信息与启用状态'},{key:'role',icon:'🛡️',label:'角色管理',description:'定义角色及其对应的菜单与操作权限'},{key:'perm',icon:'🔐',label:'权限配置',description:'按模块配置数据范围权限与审批权限'},{key:'audit',icon:'📜',label:'操作日志',description:'查询关键操作的审计记录与变更历史'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>