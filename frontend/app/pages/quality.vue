<template><div class="erp-page"><div class="erp-tab-content"><div class="erp-tab-header"><h2>{{ currentTab?.label }}</h2><p class="erp-tab-desc">{{ currentTab?.description }}</p></div>
<QualityIqcTab v-if="activeTab==='iqc'" /><QualityIpqcTab v-else-if="activeTab==='ipqc'" />
<QualityOqcTab v-else-if="activeTab==='oqc'" /><QualityNcTab v-else-if="activeTab==='nc'" />
<QualityStdTab v-else-if="activeTab==='std'" /></div></div></template>
<script setup lang="ts">
definePageMeta({middleware:'auth'})
const route=useRoute()
const activeTab=ref((route.query.tab as string)||'iqc')
const tabs=[{key:'iqc',icon:'🔍',label:'来料检验',description:'IQC — 对采购到货进行抽样或全检，判定合格/不合格'},{key:'ipqc',icon:'⚙️',label:'过程检验',description:'IPQC — 生产过程中的首检、巡检与完工检验'},{key:'oqc',icon:'📦',label:'出货检验',description:'OQC — 成品出货前的质量检验与放行'},{key:'nc',icon:'⚠️',label:'不合格品管理',description:'登记不合格品、判定处理方式（退货/返工/让步）'},{key:'std',icon:'📐',label:'检验标准',description:'维护检验项目、抽样方案、AQL值与判定标准'}]
const currentTab=computed(()=>tabs.find(t=>t.key===activeTab.value))
</script>