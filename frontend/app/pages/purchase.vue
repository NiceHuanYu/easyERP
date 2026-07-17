<template>
  <div class="purchase-page">
    <div class="sub-tabs">
      <button v-for="tab in tabs" :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key">
        <span class="tab-icon">{{ tab.icon }}</span><span>{{ tab.label }}</span>
      </button>
    </div>
    <div class="tab-content">
      <div class="tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="tab-desc">{{ currentTab?.description }}</p>
      </div>

      <PurchaseRequisitionTab v-if="activeTab === 'requisition'" />
      <PurchaseOrderTab v-else-if="activeTab === 'order'" />
      <PurchaseArrivalTab v-else-if="activeTab === 'arrival'" />
      <PurchaseReturnTab v-else-if="activeTab === 'return'" />
      <PurchaseReconcileTab v-else-if="activeTab === 'reconcile'" />
      <PurchasePaymentTab v-else-if="activeTab === 'payment'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const activeTab = ref('order')
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

<style scoped>
.purchase-page { display:flex; flex-direction:column; height:100%; }
.sub-tabs { display:flex; gap:4px; background:#fff; border-radius:10px; padding:6px; margin-bottom:20px; box-shadow:0 1px 3px rgba(0,0,0,.06); flex-wrap:wrap; }
.tab-btn { display:flex; align-items:center; gap:6px; padding:8px 16px; border:none; border-radius:8px; background:transparent; color:#666; font-size:13px; cursor:pointer; transition:all .15s; white-space:nowrap; }
.tab-btn:hover { background:#f5f7fa; color:#333; }
.tab-btn.active { background:#1a73e8; color:#fff; font-weight:500; }
.tab-icon { font-size:16px; }
.tab-content { background:#fff; border-radius:10px; padding:24px; box-shadow:0 1px 3px rgba(0,0,0,.06); flex:1; display:flex; flex-direction:column; }
.tab-header { margin-bottom:20px; padding-bottom:14px; border-bottom:1px solid #f0f0f0; flex-shrink:0; }
.tab-header h2 { margin:0 0 4px 0; font-size:18px; color:#333; }
.tab-desc { margin:0; font-size:13px; color:#999; }
</style>
