<template>
  <div class="erp-toolbar">
    <div class="erp-toolbar-left">
      <slot name="left">
        <!-- 默认搜索框 -->
        <div v-if="searchPlaceholder" class="erp-search-box">
          <span class="erp-search-icon">🔍</span>
          <input
            :value="searchValue"
            @input="$emit('update:searchValue', ($event.target as HTMLInputElement).value)"
            :placeholder="searchPlaceholder"
            class="erp-search-input"
          />
        </div>
        <!-- 额外筛选器 -->
        <slot name="filters" />
      </slot>
    </div>
    <div class="erp-toolbar-right">
      <slot name="right">
        <button v-if="createLabel" class="erp-btn erp-btn-primary" @click="$emit('create')">
          ＋ {{ createLabel }}
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  /** 搜索框占位文字。不传则隐藏默认搜索框，使用 #filters slot */
  searchPlaceholder?: string
  /** 搜索值（v-model 兼容） */
  searchValue?: string
  /** 新建按钮文字。不传则隐藏默认按钮，使用 #right slot */
  createLabel?: string
}>()

defineEmits<{
  'update:searchValue': [value: string]
  create: []
}>()
</script>
