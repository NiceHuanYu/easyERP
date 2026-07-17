<template>
  <div class="tab-placeholder">
    <div class="placeholder-icon">📋</div>
    <p class="placeholder-text">{{ label }}功能开发中</p>
    <p class="placeholder-hint">{{ hint }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  hint?: string
}>()
</script>

<style scoped>
.tab-placeholder {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 60px 20px; color: #bbb; flex: 1;
}
.placeholder-icon { font-size: 48px; margin-bottom: 16px; opacity: .5; }
.placeholder-text { font-size: 16px; color: #999; margin-bottom: 8px; }
.placeholder-hint { font-size: 13px; color: #ccc; }
</style>
