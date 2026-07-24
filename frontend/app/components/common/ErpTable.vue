<template>
  <div class="erp-table-wrap">
    <table class="erp-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="{
              sortable: col.sortable,
              'text-right': col.align === 'right',
              'text-center': col.align === 'center',
            }"
            :style="{ width: col.width }"
            @click="col.sortable && $emit('sort', col.key)"
          >
            {{ col.label }}
            <span v-if="col.sortable && sortField === col.key">
              {{ sortAsc ? ' ▲' : ' ▼' }}
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <slot name="rows" :items="items" />
        <tr v-if="!items || items.length === 0">
          <td :colspan="columns.length" class="erp-cell-empty">
            {{ emptyText }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
export interface TableColumn {
  key: string
  label: string
  sortable?: boolean
  align?: 'left' | 'right' | 'center'
  width?: string
}

defineProps<{
  columns: TableColumn[]
  items: unknown[]
  sortField?: string
  sortAsc?: boolean
  emptyText?: string
}>()

defineEmits<{
  sort: [field: string]
}>()
</script>
