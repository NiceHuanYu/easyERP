/**
 * useDelete — 通用删除确认流程 composable
 *
 * 消除 ~33 个 Tab 组件中重复的 showDel/dt/confirmDel/doDel 模式。
 *
 * 用法示例：
 * ```ts
 * const { showDelete, deleteTarget, confirmDelete, doDelete } = useDelete(data, 'code')
 * ```
 */

export function useDelete<T>(data: Ref<T[]>, keyField: keyof T & string = 'code' as unknown as keyof T & string) {
  const showDelete = ref(false)
  const deleteTarget = ref<T | null>(null)

  function confirmDelete(item: T) {
    deleteTarget.value = item
    showDelete.value = true
  }

  function doDelete() {
    if (deleteTarget.value) {
      const key = deleteTarget.value[keyField]
      data.value = data.value.filter(item => item[keyField] !== key)
    }
    showDelete.value = false
    deleteTarget.value = null
  }

  return { showDelete, deleteTarget, confirmDelete, doDelete }
}
