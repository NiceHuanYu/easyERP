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
      <div class="tab-placeholder">
        <div class="placeholder-icon">📋</div>
        <p class="placeholder-text">{{ currentTab?.label }}功能开发中</p>
        <p class="placeholder-hint">后续将在此处展示可视化图表与经营分析看板</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const activeTab = ref('dashboard')
const tabs = [
  { key: 'dashboard', icon: '📊', label: '经营看板',   description: '关键经营指标总览：销售额、利润、订单交付率' },
  { key: 'sales',     icon: '📈', label: '销售分析',   description: '销售趋势、客户排名、产品畅销分析' },
  { key: 'purchase',  icon: '📉', label: '采购分析',   description: '采购支出、供应商表现、交期达成率' },
  { key: 'inventory', icon: '📦', label: '库存分析',   description: '库存周转率、呆滞物料、ABC分类' },
  { key: 'production',icon: '🏭', label: '生产分析',   description: '工单达成率、设备稼动率、不良率趋势' },
  { key: 'quality',   icon: '✅', label: '质量分析',   description: '来料/过程/出货合格率统计与趋势' },
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
.tab-content { background:#fff; border-radius:10px; padding:24px; box-shadow:0 1px 3px rgba(0,0,0,.06); flex:1; }
.tab-header { margin-bottom:24px; padding-bottom:16px; border-bottom:1px solid #f0f0f0; }
.tab-header h2 { margin:0 0 4px 0; font-size:18px; color:#333; }
.tab-desc { margin:0; font-size:13px; color:#999; }
.tab-placeholder { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:60px 20px; color:#bbb; }
.placeholder-icon { font-size:48px; margin-bottom:16px; opacity:.5; }
.placeholder-text { font-size:16px; color:#999; margin-bottom:8px; }
.placeholder-hint { font-size:13px; color:#ccc; }
</style>
