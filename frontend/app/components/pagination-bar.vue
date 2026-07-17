<template>
  <div class="pagination-bar">
    <span class="page-info">共 {{ total }} 条，第 {{ modelValue }} / {{ totalPages }} 页</span>
    <div class="page-controls">
      <button class="page-btn" :disabled="modelValue <= 1" @click="$emit('update:modelValue', 1)">首页</button>
      <button class="page-btn" :disabled="modelValue <= 1" @click="$emit('update:modelValue', modelValue - 1)">‹</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="page-ellipsis">…</span>
        <span v-else :class="['page-num', { current: p === modelValue }]" @click="$emit('update:modelValue', p)">{{ p }}</span>
      </template>
      <button class="page-btn" :disabled="modelValue >= totalPages" @click="$emit('update:modelValue', modelValue + 1)">›</button>
      <button class="page-btn" :disabled="modelValue >= totalPages" @click="$emit('update:modelValue', totalPages)">末页</button>
    </div>
    <label class="page-size-label">
      每页
      <select :value="pageSize" @change="$emit('update:pageSize', Number(($event.target as HTMLSelectElement).value))" class="page-size-select">
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

<style scoped>
.pagination-bar { display:flex; align-items:center; justify-content:space-between; padding-top:12px; margin-top:4px; flex-wrap:wrap; gap:10px; }
.page-info { font-size:12px; color:#999; }
.page-controls { display:flex; align-items:center; gap:2px; }
.page-btn { padding:4px 10px; border:1px solid #e0e0e0; border-radius:4px; background:#fff; font-size:12px; color:#555; cursor:pointer; transition:all .15s; }
.page-btn:hover:not(:disabled) { border-color:#1a73e8; color:#1a73e8; }
.page-btn:disabled { opacity:.4; cursor:not-allowed; }
.page-num { padding:4px 10px; font-size:12px; color:#555; cursor:pointer; border-radius:4px; min-width:28px; text-align:center; }
.page-num:hover { background:#f0f4ff; }
.page-num.current { background:#1a73e8; color:#fff; font-weight:500; }
.page-ellipsis { padding:4px 4px; font-size:12px; color:#999; }
.page-size-label { font-size:12px; color:#999; display:flex; align-items:center; gap:4px; }
.page-size-select { padding:3px 6px; border:1px solid #e0e0e0; border-radius:4px; font-size:12px; color:#555; outline:none; }
</style>
