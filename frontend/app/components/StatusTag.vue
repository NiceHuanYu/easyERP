<template>
  <el-tag :type="tagType" :size="size" :effect="effect" :round="round">
    <slot>{{ displayText }}</slot>
  </el-tag>
</template>

<script setup lang="ts">
export interface StatusTagProps {
  /** 状态值 */
  status: string
  /** 可选的自定义类型映射 */
  type?: string
  /** 尺寸 */
  size?: '' | 'large' | 'default' | 'small'
  /** 主题 */
  effect?: 'dark' | 'light' | 'plain'
  /** 是否圆角 */
  round?: boolean
}

const props = withDefaults(defineProps<StatusTagProps>(), {
  type: undefined,
  size: 'default',
  effect: 'light',
  round: false,
})

// ---- 内置颜色映射 ----
const statusColorMap: Record<string, string> = {
  // 通用状态
  active: 'success',
  enabled: 'success',
  normal: '',
  ok: 'success',

  // 审核 / 流程状态
  pending: 'warning',
  draft: 'info',
  approved: 'success',
  rejected: 'danger',
  cancelled: 'info',

  // 业务状态
  open: 'primary',
  closed: '',
  completed: 'success',
  in_progress: 'warning',
  processing: 'warning',
  shipped: 'primary',
  delivered: 'success',
  returned: 'danger',

  // 启用 / 禁用
  disabled: 'danger',
  locked: 'danger',
  expired: 'warning',
}

const tagType = computed(() => {
  if (props.type) return props.type
  const key = String(props.status).toLowerCase()
  return statusColorMap[key] ?? 'info'
})

const displayText = computed(() => {
  const s = String(props.status)
  // 常用中文映射
  const textMap: Record<string, string> = {
    active: '启用',
    enabled: '启用',
    disabled: '禁用',
    normal: '正常',
    ok: '正常',
    pending: '待处理',
    draft: '草稿',
    approved: '已审核',
    rejected: '已驳回',
    cancelled: '已取消',
    open: '进行中',
    closed: '已关闭',
    completed: '已完成',
    in_progress: '处理中',
    processing: '处理中',
    shipped: '已发货',
    delivered: '已送达',
    returned: '已退货',
    locked: '已锁定',
    expired: '已过期',
  }
  return textMap[s] ?? s
})
</script>
