<template>
  <form class="erp-form" @submit.prevent="$emit('submit')">
    <!-- 通用表单栅格 -->
    <div class="erp-form-grid">
      <slot />
    </div>

    <!-- 操作按钮 -->
    <div v-if="showActions" class="erp-form-actions">
      <button type="button" class="erp-btn erp-btn-cancel" @click="$emit('cancel')">
        {{ cancelText }}
      </button>
      <button type="submit" class="erp-btn erp-btn-primary" :disabled="submitting">
        {{ submitting ? submittingText : submitText }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  showActions?: boolean
  submitText?: string
  cancelText?: string
  submitting?: boolean
  submittingText?: string
}>(), {
  showActions: true,
  submitText: '保存',
  cancelText: '取消',
  submitting: false,
  submittingText: '保存中...',
})

defineEmits<{
  submit: []
  cancel: []
}>()
</script>

<style scoped>
.erp-form { display: flex; flex-direction: column; gap: 16px; }
.erp-form-actions {
  display: flex; justify-content: flex-end; gap: 10px;
  padding-top: 16px; border-top: 1px solid var(--erp-border-light, #f0f0f0);
}
.erp-btn { padding: 8px 20px; border: none; border-radius: var(--erp-radius-lg, 8px); font-size: 13px; cursor: pointer; font-weight: 500; }
.erp-btn-primary { background: var(--erp-primary, #1a73e8); color: #fff; }
.erp-btn-cancel { background: #f5f5f5; color: #666; }
.erp-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
