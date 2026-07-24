<template><div class="erp-page" :key="route.fullPath"><div class="erp-tab-content"><div class="erp-tab-header"><h2>{{ currentTab?.label }}</h2><p class="erp-tab-desc">{{ currentTab?.description }}</p></div>
<LazyFinanceArTab v-if="activeTab==='ar'" /><LazyFinanceApTab v-else-if="activeTab==='ap'" />
<LazyFinanceInvoiceTab v-else-if="activeTab==='invoice'" /><LazyFinancePaymentTab v-else-if="activeTab==='payment'" />
<LazyFinanceCostTab v-else-if="activeTab==='cost'" /><LazyFinanceProfitTab v-else-if="activeTab==='profit'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const route=useRoute()
const activeTab=computed(()=>(route.query.tab as string)||'ar')
const tabs=[{key:'ar',icon:'💰',label:'应收账款',description:'管理销售产生的应收款项、账龄与催收'},{key:'ap',icon:'💳',label:'应付账款',description:'管理采购产生的应付款项与付款计划'},{key:'invoice',icon:'🧾',label:'发票管理',description:'开具销售发票、接收采购发票与进项管理'},{key:'payment',icon:'💵',label:'收付款管理',description:'记录收款与付款流水，核销对应单据'},{key:'cost',icon:'📊',label:'成本核算',description:'按订单/产品核算材料、人工与制造成本'},{key:'profit',icon:'📈',label:'利润分析',description:'按客户、订单、产品维度分析毛利与净利'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>