<template>
  <div class="search-bar">
    <!-- 基础搜索区：始终可见 -->
    <el-card shadow="never" class="search-card">
      <div class="search-body">
        <!-- 搜索字段插槽 -->
        <div class="search-fields">
          <slot />
        </div>

        <!-- 操作按钮插槽 -->
        <div v-if="$slots.actions" class="search-actions">
          <slot name="actions" />
        </div>
      </div>

      <!-- 展开/收起按钮 — 仅在有高级字段时显示 -->
      <div v-if="hasAdvancedSlot" class="toggle-row">
        <el-button
          text
          type="primary"
          :icon="collapsed ? ArrowDown : ArrowUp"
          @click="collapsed = !collapsed"
        >
          {{ collapsed ? '展开高级搜索' : '收起高级搜索' }}
        </el-button>
      </div>
    </el-card>

    <!-- 高级搜索区：可折叠 -->
    <el-collapse-transition>
      <el-card v-show="!collapsed && hasAdvancedSlot" shadow="never" class="advanced-card">
        <div class="search-body">
          <div class="search-fields">
            <slot name="advanced" />
          </div>
        </div>
      </el-card>
    </el-collapse-transition>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    collapsed?: boolean
  }>(),
  {
    collapsed: true,
  },
)

const slots = useSlots()

const collapsed = ref(props.collapsed)

const hasAdvancedSlot = computed(() => !!slots.advanced)
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.search-card {
  border-radius: 6px;
}

.search-body {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}

.search-fields {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

.toggle-row {
  display: flex;
  justify-content: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.advanced-card {
  margin-top: 8px;
  border-radius: 6px;
}
</style>
