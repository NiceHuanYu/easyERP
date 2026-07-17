<template>
  <div class="basic-data-page">
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
        <p class="placeholder-hint">后续将在此处实现数据的增删改查与导入导出</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const activeTab = ref('material')

const tabs = [
  { key: 'material',    icon: '🔩', label: '物料档案',     description: '管理原材料、半成品、辅料等所有物料的编码、规格、属性和参数' },
  { key: 'product',     icon: '📦', label: '产品档案',     description: '管理成品的编码、规格、售价、图片及相关技术资料' },
  { key: 'bom',         icon: '📐', label: 'BOM管理',      description: '维护物料清单（BOM），定义产品的物料组成与用量' },
  { key: 'customer',    icon: '🤝', label: '客户管理',     description: '管理客户基本信息、联系方式、信用额度及交易记录' },
  { key: 'supplier',    icon: '🚚', label: '供应商管理',   description: '管理供应商基本信息、资质、报价及供货记录' },
  { key: 'warehouse',   icon: '🏗️', label: '仓库管理',     description: '管理仓库/库位信息、存储类型及容量参数' },
  { key: 'unit',        icon: '⚖️', label: '计量单位',     description: '管理计量单位及单位之间的换算关系' },
]

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

<style scoped>
.basic-data-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
}

/* 二级标签栏 */
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

/* 标签内容 */
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
