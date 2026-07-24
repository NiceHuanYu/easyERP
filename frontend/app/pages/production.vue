<template>
  <div class="erp-page">
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>

      <ProductionPlanTab v-if="activeTab === 'plan'" />
      <ProductionOrderTab v-else-if="activeTab === 'order'" />
      <ProductionScheduleTab v-else-if="activeTab === 'schedule'" />
      <ProductionMaterialTab v-else-if="activeTab === 'material'" />
      <ProductionReportTab v-else-if="activeTab === 'report'" />
      <ProductionCompleteTab v-else-if="activeTab === 'complete'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'order')
watch(() => route.query.tab, (v) => { if (v) activeTab.value = v as string })
const tabs = [
  { key: 'plan',    icon: '📅', label: '生产计划',   description: '主生产计划（MPS）的编制、排程与调整' },
  { key: 'order',   icon: '📋', label: '生产工单',   description: '工单创建、审核、下达与完工关闭' },
  { key: 'schedule',icon: '⏱️', label: '生产排程',   description: '按设备、人员、优先级进行工序排程' },
  { key: 'material',icon: '🔧', label: '领退料管理', description: '按工单领料、退料与消耗登记' },
  { key: 'report',  icon: '📊', label: '工序报工',   description: '记录每道工序的完工数量、工时与不良' },
  { key: 'complete',icon: '✅', label: '完工入库',   description: '成品完工检验、入库与工单结算' },
]
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

