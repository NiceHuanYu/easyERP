<template>
  <div class="erp-item-table">
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key"
              :style="{ textAlign: col.align || 'left' }">
              {{ col.label }}
              <span v-if="col.required" class="erp-form-req">*</span>
            </th>
            <th style="text-align:center;width:60px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in items" :key="idx">
            <template v-for="col in columns" :key="col.key">
              <!-- autocomplete -->
              <td v-if="col.type === 'autocomplete'" class="erp-item-autocomplete-cell">
                <input
                  :value="item[col.key]"
                  @input="onAutocompleteInput(idx, col.key, ($event.target as HTMLInputElement).value)"
                  @focus="onAutocompleteFocus(idx)"
                  @blur="onAutocompleteBlur"
                  :placeholder="col.placeholder || '搜索或输入...'"
                  class="erp-tbl-input"
                  autocomplete="off"
                />
                <ul v-if="activeAutocompleteIdx === idx && autocompleteSuggestions.length > 0" class="erp-autocomplete-dropdown">
                  <li
                    v-for="(s, si) in autocompleteSuggestions"
                    :key="si"
                    @mousedown.prevent="onAutocompleteSelect(idx, s)"
                    :class="{ active: si === autocompleteHighlight }"
                  >
                    <span class="ac-name">{{ s.name }}</span>
                    <span class="ac-code">{{ s.code }}</span>
                    <span class="ac-spec">{{ s.spec }}</span>
                  </li>
                </ul>
              </td>

              <!-- number -->
              <td v-else-if="col.type === 'number'" :style="{ textAlign: col.align || 'right' }">
                <input
                  v-model.number="item[col.key]"
                  type="number"
                  :min="col.min ?? 1"
                  :step="col.step ?? 1"
                  :placeholder="col.placeholder || ''"
                  class="erp-tbl-input erp-tbl-input-num"
                />
              </td>

              <!-- select -->
              <td v-else-if="col.type === 'select'">
                <select v-model="item[col.key]" class="erp-tbl-select">
                  <option v-for="opt in (col.options || [])" :key="opt" :value="opt">{{ opt }}</option>
                </select>
              </td>

              <!-- text -->
              <td v-else>
                <input
                  v-model="item[col.key]"
                  type="text"
                  :placeholder="col.placeholder || ''"
                  class="erp-tbl-input"
                />
              </td>
            </template>

            <td class="erp-cell-acts">
              <button class="erp-lnk erp-lnk-danger" @click="removeItem(idx)">删除</button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td :colspan="columns.length + 1" class="erp-cell-empty">{{ emptyText }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 底部工具栏 -->
    <div class="erp-item-toolbar">
      <button class="erp-btn erp-btn-primary" style="padding:6px 16px;font-size:12px;" @click="addItem()">
        {{ addLabel }}
      </button>
      <span v-for="t in totals" :key="t.key" style="font-size:13px;color:#555;margin-left:16px;">
        {{ t.label }}：<strong style="color:#1a73e8;">{{ formatTotal(t) }}</strong>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Material } from '~/types/entities'

export interface ItemColumn {
  key: string
  label: string
  type?: 'text' | 'number' | 'select' | 'autocomplete'
  required?: boolean
  align?: 'left' | 'right' | 'center'
  placeholder?: string
  options?: string[]
  min?: number
  step?: number
  total?: boolean
  totalLabel?: string
}

const props = withDefaults(defineProps<{
  columns: ItemColumn[]
  items: Record<string, any>[]
  emptyText?: string
  addLabel?: string
}>(), {
  emptyText: '暂无数据，请添加',
  addLabel: '＋ 添加行',
})

const emit = defineEmits<{
  'update:items': [items: Record<string, any>[]]
}>()

// ---- autocomplete ----
const activeAutocompleteIdx = ref(-1)
const autocompleteSuggestions = ref<Material[]>([])
const autocompleteHighlight = ref(0)
let blurTimer: ReturnType<typeof setTimeout> | null = null

function onAutocompleteInput(idx: number, key: string, value: string) {
  const newItems = [...props.items]
  if (newItems[idx]) newItems[idx] = { ...newItems[idx], [key]: value }
  emit('update:items', newItems)

  const q = value.trim().toLowerCase()
  if (q) {
    try {
      const store = useMaterialStore()
      autocompleteSuggestions.value = store.items
        .filter(m => m.name.toLowerCase().includes(q) || m.code.toLowerCase().includes(q) || m.spec.toLowerCase().includes(q))
        .slice(0, 8)
    } catch { autocompleteSuggestions.value = [] }
  } else {
    autocompleteSuggestions.value = []
  }
  autocompleteHighlight.value = 0
}

function onAutocompleteFocus(idx: number) {
  activeAutocompleteIdx.value = idx
  if (blurTimer) { clearTimeout(blurTimer); blurTimer = null }
}

function onAutocompleteBlur() {
  blurTimer = setTimeout(() => {
    activeAutocompleteIdx.value = -1
    autocompleteSuggestions.value = []
  }, 200)
}

function onAutocompleteSelect(idx: number, material: Material) {
  const newItems = [...props.items]
  if (newItems[idx]) {
    newItems[idx] = { ...newItems[idx], material: material.name, unit: newItems[idx].unit || material.unit }
  }
  emit('update:items', newItems)
  activeAutocompleteIdx.value = -1
  autocompleteSuggestions.value = []
}

// ---- 行操作 ----
function addItem() {
  const empty: Record<string, any> = {}
  for (const col of props.columns) {
    if (col.type === 'number') empty[col.key] = col.min ?? 1
    else if (col.type === 'select') empty[col.key] = col.options?.[0] ?? ''
    else empty[col.key] = ''
  }
  emit('update:items', [...props.items, empty])
}

function removeItem(idx: number) {
  const newItems = [...props.items]
  newItems.splice(idx, 1)
  emit('update:items', newItems)
}

// ---- 合计 ----
const totals = computed(() =>
  props.columns.filter(c => c.total).map(c => ({
    key: c.key,
    label: c.totalLabel || `合计${c.label}`,
    value: props.items.reduce((s, item) => s + (Number(item[c.key]) || 0), 0),
  })),
)

function formatTotal(t: { key: string; value: number }) {
  const col = props.columns.find(c => c.key === t.key)
  return col?.step ? t.value.toFixed(2) : String(t.value)
}
</script>

<style scoped>
.erp-item-table {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--erp-border-light, #f0f0f0);
}

.erp-item-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  flex-wrap: wrap;
  gap: 8px;
}

/* autocomplete */
.erp-item-autocomplete-cell { position: relative; }

.erp-autocomplete-dropdown {
  position: absolute;
  top: 100%; left: 0; right: 0;
  z-index: 100;
  background: #fff;
  border: 1px solid var(--erp-border);
  border-radius: var(--erp-radius-md);
  box-shadow: var(--erp-shadow-md);
  max-height: 200px;
  overflow-y: auto;
  list-style: none;
  padding: 0; margin: 2px 0 0;
}

.erp-autocomplete-dropdown li {
  padding: 7px 10px;
  cursor: pointer;
  font-size: 12px;
  display: flex; gap: 8px; align-items: center;
}

.erp-autocomplete-dropdown li:hover,
.erp-autocomplete-dropdown li.active { background: var(--erp-primary-bg); }

.ac-name { font-weight: 500; color: var(--erp-text); flex: 1; }
.ac-code { color: var(--erp-primary); font-family: var(--erp-font-mono); font-size: 11px; }
.ac-spec { color: var(--erp-text-hint); font-size: 11px; }

/* 按钮 */
.erp-btn { padding: 8px 20px; border: none; border-radius: var(--erp-radius-lg); font-size: 13px; cursor: pointer; font-weight: 500; }
.erp-btn-primary { background: var(--erp-primary); color: #fff; }
</style>
