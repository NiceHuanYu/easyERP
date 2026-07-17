<template><div class="page"><div class="sub-tabs"><button v-for="tab in tabs" :key="tab.key" :class="['tab-btn',{active:activeTab===tab.key}]" @click="activeTab=tab.key"><span class="tab-icon">{{ tab.icon }}</span><span>{{ tab.label }}</span></button></div>
<div class="tab-content"><div class="tab-header"><h2>{{ currentTab?.label }}</h2><p class="tab-desc">{{ currentTab?.description }}</p></div>
<FinanceArTab v-if="activeTab==='ar'" /><FinanceApTab v-else-if="activeTab==='ap'" />
<FinanceInvoiceTab v-else-if="activeTab==='invoice'" /><FinancePaymentTab v-else-if="activeTab==='payment'" />
<FinanceCostTab v-else-if="activeTab==='cost'" /><FinanceProfitTab v-else-if="activeTab==='profit'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const activeTab=ref('ar')
const tabs=[{key:'ar',icon:'💰',label:'应收账款',description:'管理销售产生的应收款项、账龄与催收'},{key:'ap',icon:'💳',label:'应付账款',description:'管理采购产生的应付款项与付款计划'},{key:'invoice',icon:'🧾',label:'发票管理',description:'开具销售发票、接收采购发票与进项管理'},{key:'payment',icon:'💵',label:'收付款管理',description:'记录收款与付款流水，核销对应单据'},{key:'cost',icon:'📊',label:'成本核算',description:'按订单/产品核算材料、人工与制造成本'},{key:'profit',icon:'📈',label:'利润分析',description:'按客户、订单、产品维度分析毛利与净利'}]
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