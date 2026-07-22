<template>
  <div class="erp-pagination">
    <span class="erp-page-info">共 {{ total }} 条，第 {{ modelValue }} / {{ totalPages }} 页</span>
    <div class="erp-page-controls">
      <button class="erp-page-btn" :disabled="modelValue <= 1" @click="$emit('update:modelValue', 1)">首页</button>
      <button class="erp-page-btn" :disabled="modelValue <= 1" @click="$emit('update:modelValue', modelValue - 1)">‹</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="erp-page-ellipsis">…</span>
        <span v-else :class="['erp-page-num', { current: p === modelValue }]" @click="typeof p === 'number' && $emit('update:modelValue', p)">{{ p }}</span>
      </template>
      <button class="erp-page-btn" :disabled="modelValue >= totalPages" @click="$emit('update:modelValue', modelValue + 1)">›</button>
      <button class="erp-page-btn" :disabled="modelValue >= totalPages" @click="$emit('update:modelValue', totalPages)">末页</button>
    </div>
    <label class="erp-page-size-label">
      每页
      <select :value="pageSize" @change="$emit('update:pageSize', Number(($event.target as HTMLSelectElement).value))" class="erp-page-size-select">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
      条
    </label>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ total: number; modelValue: number; pageSize: number }>()
defineEmits<{ 'update:modelValue': [v: number]; 'update:pageSize': [v: number] }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const visiblePages = computed(() => {
  const t = totalPages.value; const c = props.modelValue
  if (t <= 7) return Array.from({ length: t }, (_, i) => i + 1)
  if (c <= 4) return [1,2,3,4,5,'...',t]
  if (c >= t - 3) return [1,'...',t-4,t-3,t-2,t-1,t]
  return [1,'...',c-1,c,c+1,'...',t]
})
</script>

