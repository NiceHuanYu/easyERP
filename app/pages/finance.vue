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
        <p class="placeholder-hint">后续将在此处实现财务记账、收付款与成本分析</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const activeTab = ref('ar')
const tabs = [
  { key: 'ar',      icon: '💰', label: '应收账款',   description: '管理销售产生的应收款项、账龄与催收' },
  { key: 'ap',      icon: '💳', label: '应付账款',   description: '管理采购产生的应付款项与付款计划' },
  { key: 'invoice', icon: '🧾', label: '发票管理',   description: '开具销售发票、接收采购发票与进项管理' },
  { key: 'payment', icon: '💵', label: '收付款管理', description: '记录收款与付款流水，核销对应单据' },
  { key: 'cost',    icon: '📊', label: '成本核算',   description: '按订单/产品核算材料、人工与制造成本' },
  { key: 'profit',  icon: '📈', label: '利润分析',   description: '按客户、订单、产品维度分析毛利与净利' },
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
