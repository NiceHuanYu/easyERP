<template><div class="erp-page" :key="route.fullPath"><div class="erp-tab-content"><div class="erp-tab-header"><h2>{{ currentTab?.label }}</h2><p class="erp-tab-desc">{{ currentTab?.description }}</p></div>
<LazyAnalyticsDashboardTab v-if="activeTab==='dashboard'" /><LazyAnalyticsSalesTab v-else-if="activeTab==='sales'" />
<LazyAnalyticsPurchaseTab v-else-if="activeTab==='purchase'" /><LazyAnalyticsInventoryTab v-else-if="activeTab==='inventory'" />
<LazyAnalyticsProductionTab v-else-if="activeTab==='production'" /><LazyAnalyticsQualityTab v-else-if="activeTab==='quality'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const route=useRoute()
const activeTab=computed(()=>(route.query.tab as string)||'dashboard')
const tabs=[{key:'dashboard',icon:'📊',label:'经营看板',description:'关键经营指标总览：销售额、利润、订单交付率'},{key:'sales',icon:'📈',label:'销售分析',description:'销售趋势、客户排名、产品畅销分析'},{key:'purchase',icon:'📉',label:'采购分析',description:'采购支出、供应商表现、交期达成率'},{key:'inventory',icon:'📦',label:'库存分析',description:'库存周转率、呆滞物料、ABC分类'},{key:'production',icon:'🏭',label:'生产分析',description:'工单达成率、设备稼动率、不良率趋势'},{key:'quality',icon:'✅',label:'质量分析',description:'来料/过程/出货合格率统计与趋势'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>