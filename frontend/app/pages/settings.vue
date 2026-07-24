<template>
  <div class="erp-page">
    <div class="erp-tab-content">
      <div class="erp-tab-header">
        <h2>{{ currentTab?.label }}</h2>
        <p class="erp-tab-desc">{{ currentTab?.description }}</p>
      </div>
      <SettingsCodingTab v-if="activeTab === 'coding'" />
      <SettingsParamsTab v-else-if="activeTab === 'params'" />
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()

const tabs = [
  { key: 'coding', label: '编号规则', description: '配置各业务单据的编号生成规则，包括前缀、日期、流水号等' },
  { key: 'params', label: '系统参数', description: '配置系统基础参数，包括公司信息、默认币种、税率等' },
] as const

const activeTab = computed(() => {
  const q = route.query.tab as string | undefined
  return q && tabs.some(t => t.key === q) ? q : 'coding'
})

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

<style scoped>
.erp-page {
  padding: 20px 24px;
}
.erp-tab-content {
  background: transparent;
}
.erp-tab-header {
  margin-bottom: 20px;
}
.erp-tab-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}
.erp-tab-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: #888;
}
</style>
