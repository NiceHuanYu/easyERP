<template>
  <Teleport to="body">
    <div v-if="show" class="erp-modal-overlay" @click.self="$emit('close')">
      <div class="erp-modal-panel">
        <div class="erp-modal-header">
          <h3>{{ title }}</h3>
          <button class="erp-modal-close" @click="$emit('close')">✕</button>
        </div>
        <div class="erp-modal-body">
          <slot />
        </div>
        <div class="erp-modal-footer">
          <button class="erp-btn erp-btn-cancel" @click="$emit('close')">取消</button>
          <button class="erp-btn erp-btn-primary" @click="$emit('save')">保存</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{ show: boolean; title: string }>()
defineEmits<{ close: []; save: [] }>()
</script>

