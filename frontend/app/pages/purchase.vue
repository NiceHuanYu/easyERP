<template>
  <div class="erp-page" :key="route.fullPath">
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>

      <LazyPurchaseRequisitionTab v-if="activeTab === 'requisition'" />
      <LazyPurchaseOrderTab v-else-if="activeTab === 'order'" />
      <LazyPurchaseArrivalTab v-else-if="activeTab === 'arrival'" />
      <LazyPurchaseReturnTab v-else-if="activeTab === 'return'" />
      <LazyPurchaseReconcileTab v-else-if="activeTab === 'reconcile'" />
      <LazyPurchasePaymentTab v-else-if="activeTab === 'payment'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const activeTab = computed(() => (route.query.tab as string) || 'requisition')
const tabs = [
  { key: 'requisition', icon: '📝', label: '采购申请',   description: '创建并管理采购申请单，记录需求物料、数量与期望交期' },
  { key: 'order',       icon: '📋', label: '采购订单',   description: '采购订单的生成、审核、变更与供应商确认跟踪' },
  { key: 'arrival',     icon: '🚛', label: '到货管理',   description: '记录采购到货信息，触发来料检验（IQC）与入库' },
  { key: 'return',      icon: '↩️', label: '采购退货',   description: '处理不合格物料的退货、换货与补货流程' },
  { key: 'reconcile',   icon: '📊', label: '供应商对账', description: '与供应商核对采购数量、单价与金额，生成对账单' },
  { key: 'payment',     icon: '💳', label: '付款管理',   description: '管理采购付款计划、付款申请与付款记录' },
]
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

