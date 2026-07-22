<template>
  <div class="erp-page">
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>

      <SalesInquiryTab v-if="activeTab === 'inquiry'" />
      <SalesQuotationTab v-else-if="activeTab === 'quotation'" />
      <SalesOrderTab v-else-if="activeTab === 'order'" />
      <SalesShipmentTab v-else-if="activeTab === 'shipment'" />
      <SalesReturnTab v-else-if="activeTab === 'return'" />
      <SalesTrackingTab v-else-if="activeTab === 'tracking'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'order')
watch(() => route.query.tab, (v) => { if (v) activeTab.value = v as string })
const tabs = [
  { key: 'inquiry',    icon: '📩', label: '客户询价',   description: '管理客户的询价需求，记录询价内容与答复状态' },
  { key: 'quotation',  icon: '📄', label: '销售报价',   description: '创建并管理报价单，支持版本管理与客户确认' },
  { key: 'order',      icon: '📦', label: '销售订单',   description: '销售订单的录入、审核、变更与进度跟踪' },
  { key: 'shipment',   icon: '🚚', label: '销售发货',   description: '管理出库发货、物流单号与客户签收' },
  { key: 'return',     icon: '↩️', label: '销售退货',   description: '处理客户退货申请、检验与退款' },
  { key: 'tracking',   icon: '📊', label: '订单跟踪',   description: '按订单维度查看生产、发货、收款全链路进度' },
]
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

