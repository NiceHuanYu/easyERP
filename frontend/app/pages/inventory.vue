<template><div class="erp-page" :key="route.fullPath"><div class="erp-tab-content"><div class="erp-tab-header"><h2>{{ currentTab?.label }}</h2><p class="erp-tab-desc">{{ currentTab?.description }}</p></div>
<LazyInventoryOverviewTab v-if="activeTab==='overview'" />
<LazyInventoryInboundTab v-else-if="activeTab==='inbound'" />
<LazyInventoryOutboundTab v-else-if="activeTab==='outbound'" />
<LazyInventoryTransferTab v-else-if="activeTab==='transfer'" />
<LazyInventoryCountTab v-else-if="activeTab==='count'" />
<LazyInventoryJournalTab v-else-if="activeTab==='journal'" />
</div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const route=useRoute()
const activeTab = computed(() => (route.query.tab as string) || 'overview')
const tabs=[{key:'overview',icon:'📊',label:'库存概览',description:'查看各仓库的实时库存余额与可用量'},{key:'inbound',icon:'📥',label:'入库管理',description:'采购入库、生产完工入库、退货入库等'},{key:'outbound',icon:'📤',label:'出库管理',description:'销售出库、生产领料、调拨出库等'},{key:'transfer',icon:'🔄',label:'库存调拨',description:'仓库间或库位间的物料调拨管理'},{key:'count',icon:'🔢',label:'库存盘点',description:'创建盘点计划、录入实盘数量、处理差异'},{key:'journal',icon:'📜',label:'库存流水',description:'按时间顺序查看所有库存变动明细'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>