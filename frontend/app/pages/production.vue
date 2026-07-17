<template>
  <div class="page">
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
const activeTab = ref('order')
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

<style scoped>
.page { display:flex; flex-direction:column; height:100%; }
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
