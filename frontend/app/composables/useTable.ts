/**
 * useTable — 通用表格排序 / 搜索 / 分页 composable
 *
 * 消除 ~47 个 Tab 组件中重复的 s/fs/sf/sa/page/ps + sort/filtered/paged/watch 逻辑。
 *
 * 用法示例：
 * ```ts
 * const { search, sortBy, filteredWith, pagedFrom, page, pageSize, sortField, sortAsc } = useTable({
 *   data: materials,
 *   searchFields: ['code', 'name', 'spec'],
 *   initialSort: 'code',
 * })
 *
 * const filterCategory = ref('')
 * const filtered = filteredWith(item => {
 *   if (filterCategory.value && item.category !== filterCategory.value) return false
 *   return true
 * })
 * const paged = pagedFrom(filtered)
 *
 * // 额外筛选器变化时重置页码
 * watch([filterCategory], () => { page.value = 1 })
 * ```
 */

export interface UseTableOptions<T extends Record<string, unknown>> {
  /** 响应式数据源 */
  data: Ref<T[]>
  /** 搜索时要匹配的字段名 */
  searchFields: (keyof T & string)[]
  /** 初始排序字段（可选，默认无排序） */
  initialSort?: string
  /** 每页条数（可选，默认 10） */
  initialPageSize?: number
}

export function useTable<T extends Record<string, unknown>>(options: UseTableOptions<T>) {
  const search = ref('')
  const sortField = ref(options.initialSort ?? '')
  const sortAsc = ref(true)
  const page = ref(1)
  const pageSize = ref(options.initialPageSize ?? 10)

  /** 切换排序字段 / 方向 */
  function sortBy(field: string) {
    if (sortField.value === field) {
      sortAsc.value = !sortAsc.value
    } else {
      sortField.value = field
      sortAsc.value = true
    }
  }

  /** 应用搜索过滤 */
  function applySearch(list: T[]): T[] {
    const q = search.value.trim().toLowerCase()
    if (!q) return list
    return list.filter(item =>
      options.searchFields.some(f => {
        const v = item[f]
        return v != null ? String(v).toLowerCase().includes(q) : false
      }),
    )
  }

  /** 应用排序 */
  function applySort(list: T[]): T[] {
    if (!sortField.value) return list
    return [...list].sort((a, b) => {
      const av = a[sortField.value as keyof T]
      const bv = b[sortField.value as keyof T]
      if (typeof av === 'number' && typeof bv === 'number') {
        return sortAsc.value ? av - bv : bv - av
      }
      return sortAsc.value
        ? String(av ?? '').localeCompare(String(bv ?? ''))
        : String(bv ?? '').localeCompare(String(av ?? ''))
    })
  }

  /**
   * 创建带额外筛选条件的 filtered 计算属性。
   * @param extraFilter 可选，在搜索之后、排序之前应用的筛选函数
   */
  function filteredWith(extraFilter?: (item: T) => boolean) {
    return computed(() => {
      let list = applySearch(options.data.value)
      if (extraFilter) list = list.filter(extraFilter)
      return applySort(list)
    })
  }

  /** 从 filtered 计算分页数据 */
  function pagedFrom(filtered: ComputedRef<T[]>) {
    return computed(() => {
      const start = (page.value - 1) * pageSize.value
      return filtered.value.slice(start, start + pageSize.value)
    })
  }

  /** 从 filtered 计算总条数 */
  function totalFrom(filtered: ComputedRef<T[]>) {
    return computed(() => filtered.value.length)
  }

  /** 从 filtered 计算总页数 */
  function totalPagesFrom(filtered: ComputedRef<T[]>) {
    return computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)))
  }

  // 搜索词变化时重置页码
  watch(search, () => {
    page.value = 1
  })

  return {
    search,
    sortField,
    sortAsc,
    page,
    pageSize,
    sortBy,
    filteredWith,
    pagedFrom,
    totalFrom,
    totalPagesFrom,
  }
}
