<template>
  <Teleport to="body">
    <div v-if="show" class="erp-modal-overlay" @click.self="$emit('cancel')">
      <div class="erp-modal-panel confirm">
        <div class="erp-modal-header"><h3>{{ title }}</h3></div>
        <div class="erp-modal-body"><slot /></div>
        <div class="erp-modal-footer">
          <button class="erp-btn erp-btn-cancel" @click="$emit('cancel')">取消</button>
          <button class="erp-btn erp-btn-danger" @click="$emit('confirm')">确认删除</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{ show: boolean; title?: string }>()
defineEmits<{ confirm: []; cancel: [] }>()
</script>

