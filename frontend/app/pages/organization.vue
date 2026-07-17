<template><div class="page"><div class="sub-tabs"><button v-for="tab in tabs" :key="tab.key" :class="['tab-btn',{active:activeTab===tab.key}]" @click="activeTab=tab.key"><span class="tab-icon">{{ tab.icon }}</span><span>{{ tab.label }}</span></button></div>
<div class="tab-content"><div class="tab-header"><h2>{{ currentTab?.label }}</h2><p class="tab-desc">{{ currentTab?.description }}</p></div>
<OrganizationUserTab v-if="activeTab==='user'" /><OrganizationRoleTab v-else-if="activeTab==='role'" />
<OrganizationPermTab v-else-if="activeTab==='perm'" /><OrganizationAuditTab v-else-if="activeTab==='audit'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const activeTab=ref('user')
const tabs=[{key:'user',icon:'👤',label:'用户管理',description:'管理系统用户账号、基本信息与启用状态'},{key:'role',icon:'🛡️',label:'角色管理',description:'定义角色及其对应的菜单与操作权限'},{key:'perm',icon:'🔐',label:'权限配置',description:'按模块配置数据范围权限与审批权限'},{key:'audit',icon:'📜',label:'操作日志',description:'查询关键操作的审计记录与变更历史'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>
<style scoped>
.page{display:flex;flex-direction:column;height:100%;}.sub-tabs{display:flex;gap:4px;background:#fff;border-radius:10px;padding:6px;margin-bottom:20px;box-shadow:0 1px 3px rgba(0,0,0,.06);flex-wrap:wrap;}
.tab-btn{display:flex;align-items:center;gap:6px;padding:8px 16px;border:none;border-radius:8px;background:transparent;color:#666;font-size:13px;cursor:pointer;transition:all .15s;white-space:nowrap;}
.tab-btn:hover{background:#f5f7fa;color:#333;}.tab-btn.active{background:#1a73e8;color:#fff;font-weight:500;}.tab-icon{font-size:16px;}
.tab-content{background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 3px rgba(0,0,0,.06);flex:1;display:flex;flex-direction:column;}
.tab-header{margin-bottom:20px;padding-bottom:14px;border-bottom:1px solid #f0f0f0;flex-shrink:0;}
.tab-header h2{margin:0 0 4px 0;font-size:18px;color:#333;}.tab-desc{margin:0;font-size:13px;color:#999;}
</style>