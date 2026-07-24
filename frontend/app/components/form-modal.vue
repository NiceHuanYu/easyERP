<template>
  <Teleport to="body">
    <div v-if="show" class="erp-modal-overlay" @click.self="$emit('close')">
      <div :class="['erp-modal-panel', sizeClass]">
        <div class="erp-modal-header">
          <h3>{{ title }}</h3>
          <button class="erp-modal-close" @click="$emit('close')">✕</button>
        </div>

        <!-- 内置编号选择行 -->
        <div v-if="showNumbering" class="erp-numbering-row">
          <label class="erp-radio-label">
            <input type="radio" :value="'auto'" :checked="numberingMode === 'auto'" @change="$emit('update:numberingMode', 'auto')" />
            <span>自动编号</span>
          </label>
          <label class="erp-radio-label">
            <input type="radio" :value="'manual'" :checked="numberingMode === 'manual'" @change="$emit('update:numberingMode', 'manual')" />
            <span>手动编号</span>
          </label>
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
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  show: boolean
  title: string
  size?: 'md' | 'lg'
  showNumbering?: boolean
  numberingMode?: string
}>(), {
  size: 'md',
  showNumbering: false,
  numberingMode: 'auto',
})

defineEmits<{
  close: []
  save: []
  'update:numberingMode': [v: string]
}>()

const sizeClass = computed(() => props.size === 'lg' ? 'erp-modal-lg' : '')
</script>

<style scoped>
/* size: lg 变体 */
.erp-modal-panel.erp-modal-lg {
  width: 900px;
}

/* 内置编号行 — 与全局 erp-numbering-row 相同但在弹窗 header 下方 */
.erp-modal-panel > .erp-numbering-row {
  display: flex;
  gap: 24px;
  margin: 0 24px;
  padding: 10px 14px;
  background: #f8faff;
  border-radius: 8px;
  border: 1px solid #e0eeff;
}

.erp-radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
}

.erp-radio-label input[type="radio"] {
  accent-color: #1a73e8;
}
</style>
