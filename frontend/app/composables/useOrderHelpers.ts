/**
 * useOrderHelpers — 订单/采购/入库等带明细项的通用计算工具
 *
 * 消除 ~12 个 Tab 组件中重复的 totalQty / totalAmount / summary 函数。
 *
 * 用法示例：
 * ```ts
 * const { totalQty, totalAmount, summary } = useOrderHelpers()
 * ```
 */

export interface OrderItemLike {
  material?: string
  qty?: number
  amount?: number
}

export function totalQty(items: OrderItemLike[]): number {
  return items.reduce((s, i) => s + (i.qty || 0), 0)
}

export function totalAmount(items: OrderItemLike[]): number {
  return items.reduce((s, i) => s + (i.amount || 0), 0)
}

export function summary(items: OrderItemLike[]): string {
  if (!items.length) return '-'
  const first = items[0].material || ''
  return items.length === 1 ? first : `${first} 等${items.length}项`
}
