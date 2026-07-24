<template>
  <div class="erp-page" :key="route.fullPath">
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>

      <LazyApprovalSalesApproval v-if="activeTab === 'sales'" />
      <LazyApprovalPurchaseApproval v-else-if="activeTab === 'purchase'" />
      <LazyApprovalProductionApproval v-else-if="activeTab === 'production'" />
      <LazyApprovalInventoryApproval v-else-if="activeTab === 'inventory'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const activeTab = computed(() => (route.query.tab as string) || 'sales')
const tabs = [
  { key: 'sales',      label: '销售审批',   description: '待审核的销售订单，确认后流转至生产环节' },
  { key: 'purchase',   label: '采购审批',   description: '待审批的采购申请与付款单，通过后执行采购' },
  { key: 'production', label: '生产审批',   description: '待审核的生产领料与报工单据' },
  { key: 'inventory',  label: '库存审批',   description: '待审核的库存调拨申请' },
]
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>
