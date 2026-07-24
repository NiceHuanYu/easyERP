<template>
  <div class="form-section">
    <div class="form-section-header">
      <span class="form-section-title">{{ title }}</span>
      <span v-if="hint" class="form-section-hint">{{ hint }}</span>
    </div>
    <div class="form-section-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  hint?: string
}>()
</script>

<style scoped>
.form-section {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--erp-border-light, #f0f0f0);
}

.form-section-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.form-section-title {
  font-size: 13px;
  color: var(--erp-text-secondary, #555);
  font-weight: 500;
}

.form-section-hint {
  font-size: 12px;
  color: var(--erp-text-hint, #999);
}

.form-section-body {
  /* body 内容由 slot 自由填充 */
}
</style>
