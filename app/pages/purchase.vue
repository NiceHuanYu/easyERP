<template>
  <div class="purchase-page">
    <!-- 二级标签栏 -->
    <div class="sub-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <!-- 标签内容 -->
    <div class="tab-content">
      <div class="tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="tab-desc">{{ currentTab?.description }}</p>
      </div>

      <div class="tab-placeholder">
        <div class="placeholder-icon">📋</div>
        <p class="placeholder-text">{{ currentTab?.label }}功能开发中</p>
        <p class="placeholder-hint">后续将在此处实现采购单据的录入、审批、到货跟踪与对账</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const activeTab = ref('order')

const tabs = [
  { key: 'requisition', icon: '📝', label: '采购申请',     description: '创建并管理采购申请单，记录需求物料、数量与期望交期' },
  { key: 'order',       icon: '📋', label: '采购订单',     description: '采购订单的生成、审核、变更与供应商确认跟踪' },
  { key: 'arrival',     icon: '🚛', label: '到货管理',     description: '记录采购到货信息，触发来料检验（IQC）与入库' },
  { key: 'return',      icon: '↩️', label: '采购退货',     description: '处理不合格物料的退货、换货与补货流程' },
  { key: 'reconcile',   icon: '📊', label: '供应商对账',   description: '与供应商核对采购数量、单价与金额，生成对账单' },
  { key: 'payment',     icon: '💳', label: '付款管理',     description: '管理采购付款计划、付款申请与付款记录' },
]

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

<style scoped>
.purchase-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
}

.sub-tabs {
  display: flex;
  gap: 4px;
  background: #fff;
  border-radius: 10px;
  padding: 6px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  flex-wrap: wrap;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.tab-btn:hover {
  background: #f5f7fa;
  color: #333;
}

.tab-btn.active {
  background: #1a73e8;
  color: #fff;
  font-weight: 500;
}

.tab-icon {
  font-size: 16px;
}

.tab-content {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  flex: 1;
}

.tab-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-header h2 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #333;
}

.tab-desc {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.tab-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #bbb;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.placeholder-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 8px;
}

.placeholder-hint {
  font-size: 13px;
  color: #ccc;
}
</style>
