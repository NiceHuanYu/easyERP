<template>
  <div class="step-progress-bar">
    <button
      :class="['step-all-btn', { active: modelValue === null }]"
      @click="emit('update:modelValue', null)"
    >
      全部
    </button>
    <div class="step-track">
      <template v-for="(step, i) in steps" :key="i">
        <button
          :class="['step-node', {
            active: modelValue === step,
            done: stepIndex(step) < stepIndex(modelValue ?? ''),
          }]"
          @click="toggleStep(step)"
        >
          <span class="step-dot"></span>
          <span class="step-label">{{ step }}</span>
        </button>
        <span v-if="i < steps.length - 1" :class="['step-line', { done: stepIndex(step) < stepIndex(modelValue ?? '') }]"></span>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  steps: string[]
  modelValue: string | null
}>()

const emit = defineEmits<{
  'update:modelValue': [v: string | null]
}>()

function stepIndex(step: string): number {
  return props.steps.indexOf(step)
}

function toggleStep(step: string) {
  // 点击已选中则取消筛选，否则选中
  emit('update:modelValue', props.modelValue === step ? null : step)
}
</script>

<style scoped>
.step-progress-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 14px 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.step-all-btn {
  padding: 4px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fafafa;
  font-size: 12px;
  color: #888;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}

.step-all-btn:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.step-all-btn.active {
  background: #e8f0fe;
  border-color: #1a73e8;
  color: #1a73e8;
  font-weight: 500;
}

.step-track {
  display: flex;
  align-items: center;
  flex: 1;
}

.step-node {
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.15s;
  flex-shrink: 0;
}

.step-node:hover {
  background: #f5f7fa;
}

.step-node.active .step-dot {
  background: #1a73e8;
  box-shadow: 0 0 0 4px rgba(26,115,232,.15);
}

.step-node.active .step-label {
  color: #1a73e8;
  font-weight: 600;
}

.step-node.done .step-dot {
  background: #1a73e8;
}

.step-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: all 0.2s;
  flex-shrink: 0;
}

.step-label {
  font-size: 13px;
  color: #888;
  white-space: nowrap;
  transition: color 0.15s;
}

.step-node.done .step-label {
  color: #1a73e8;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e0e0e0;
  min-width: 20px;
  margin: 0 2px;
  transition: background 0.2s;
}

.step-line.done {
  background: #1a73e8;
}
</style>
