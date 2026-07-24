<template>
  <div class="sales-flow-bar">
    <span class="flow-label">销售流程</span>
    <div class="flow-track">
      <template v-for="(step, i) in steps" :key="step.key">
        <button
          :class="['flow-node', {
            active: step.key === currentTab,
            done: stepIndex(step.key) < stepIndex(currentTab),
          }]"
          @click="goTo(step.key)"
          :title="step.label"
        >
          <span class="flow-dot"></span>
          <span class="flow-label-text">{{ step.label }}</span>
        </button>
        <span v-if="i < steps.length - 1" :class="['flow-line', { done: stepIndex(step.key) < stepIndex(currentTab) }]"></span>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  currentTab: string
}>()

const steps = [
  { key: 'quotation', label: '报价' },
  { key: 'order',     label: '订单' },
  { key: 'shipment',  label: '发货' },
  { key: 'tracking',  label: '完成' },
]

function stepIndex(key: string): number {
  return steps.findIndex(s => s.key === key)
}

function goTo(key: string) {
  navigateTo(`/sales?tab=${key}`)
}
</script>

<style scoped>
.sales-flow-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #eef2f7;
}

.flow-label {
  font-size: 12px;
  color: #999;
  font-weight: 500;
  flex-shrink: 0;
}

.flow-track {
  display: flex;
  align-items: center;
  flex: 1;
}

.flow-node {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.15s;
  flex-shrink: 0;
}

.flow-node:hover {
  background: #f5f7fa;
}

.flow-node.active .flow-dot {
  background: #1a73e8;
  box-shadow: 0 0 0 4px rgba(26,115,232,.15);
  animation: flow-pulse 2s infinite;
}

.flow-node.active .flow-label-text {
  color: #1a73e8;
  font-weight: 600;
}

.flow-node.done .flow-dot {
  background: #1a73e8;
}

.flow-node.done .flow-label-text {
  color: #1a73e8;
}

@keyframes flow-pulse {
  0% { box-shadow: 0 0 0 0 rgba(26,115,232,.4) }
  70% { box-shadow: 0 0 0 6px rgba(26,115,232,0) }
  100% { box-shadow: 0 0 0 0 rgba(26,115,232,0) }
}

.flow-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: all 0.2s;
  flex-shrink: 0;
}

.flow-label-text {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  transition: color 0.15s;
}

.flow-line {
  flex: 1;
  height: 2px;
  background: #e0e0e0;
  min-width: 24px;
  margin: 0 4px;
  transition: background 0.2s;
}

.flow-line.done {
  background: #1a73e8;
}
</style>
