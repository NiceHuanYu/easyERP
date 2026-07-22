<template>
  <div class="erp-page">
    <!-- 标签内容（由独立组件渲染） -->
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>

      <!-- 物料档案 -->
      <BasicDataMaterialTab v-if="activeTab === 'material'" />

      <!-- 产品档案 -->
      <BasicDataProductTab v-else-if="activeTab === 'product'" />

      <!-- BOM管理 -->
      <BasicDataBomTab v-else-if="activeTab === 'bom'" />

      <!-- 客户管理 -->
      <BasicDataCustomerTab v-else-if="activeTab === 'customer'" />

      <!-- 供应商管理 -->
      <BasicDataSupplierTab v-else-if="activeTab === 'supplier'" />

      <!-- 仓库管理 -->
      <BasicDataWarehouseTab v-else-if="activeTab === 'warehouse'" />

      <!-- 计量单位 -->
      <BasicDataUnitTab v-else-if="activeTab === 'unit'" />

      <!-- 联络列表 -->
      <BasicDataContactTab v-else-if="activeTab === 'contact'" />

      <!-- 其他标签占位（兜底） -->
      <BasicDataTabPlaceholder v-else
        :label="currentTab?.label || ''"
        hint="后续将在此处实现数据的增删改查与导入导出"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'material')
watch(() => route.query.tab, (v) => { if (v) activeTab.value = v as string })
const tabs = [
  { key: 'material',    icon: '🔩', label: '物料档案',     description: '管理原材料、半成品、辅料等所有物料的编码、规格、属性和参数' },
  { key: 'product',     icon: '📦', label: '产品档案',     description: '管理成品的编码、规格、售价、图片及相关技术资料' },
  { key: 'bom',         icon: '📐', label: 'BOM管理',      description: '维护物料清单（BOM），定义产品的物料组成与用量' },
  { key: 'customer',    icon: '🤝', label: '客户管理',     description: '管理客户基本信息、联系方式、信用额度及交易记录' },
  { key: 'supplier',    icon: '🚚', label: '供应商管理',   description: '管理供应商基本信息、资质、报价及供货记录' },
  { key: 'warehouse',   icon: '🏗️', label: '仓库管理',     description: '管理仓库/库位信息、存储类型及容量参数' },
  { key: 'contact',    icon: '📞', label: '联络列表',     description: '集中管理客户、供应商、仓库等业务伙伴的联系人与联系方式' },
  { key: 'unit',        icon: '⚖️', label: '计量单位',     description: '管理计量单位及单位之间的换算关系' },
]
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

