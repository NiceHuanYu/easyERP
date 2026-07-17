<template>
  <div class="sales-page">
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
        <p class="placeholder-hint">后续将在此处实现销售单据的录入、查询、审批与跟踪</p>
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
  { key: 'inquiry',    icon: '📩', label: '客户询价',     description: '管理客户的询价需求，记录询价内容与答复状态' },
  { key: 'quotation',  icon: '📄', label: '销售报价',     description: '创建并管理报价单，支持版本管理与客户确认' },
  { key: 'order',      icon: '📦', label: '销售订单',     description: '销售订单的录入、审核、变更与进度跟踪' },
  { key: 'shipment',   icon: '🚚', label: '销售发货',     description: '管理出库发货、物流单号与客户签收' },
  { key: 'return',     icon: '↩️', label: '销售退货',     description: '处理客户退货申请、检验与退款' },
  { key: 'tracking',   icon: '📊', label: '订单跟踪',     description: '按订单维度查看生产、发货、收款全链路进度' },
]

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

<style scoped>
.sales-page {
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

/* 占位提示 */
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
