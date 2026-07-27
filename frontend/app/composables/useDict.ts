import { useDictStore } from '../../stores/dict'

export function useDict(type: string) {
  const dictStore = useDictStore()

  // Auto-fetch all dicts on first access if not loaded
  if (!dictStore.loaded) {
    dictStore.fetchAllDicts()
  }

  const items = computed(() => dictStore.dicts[type] ?? [])

  const label = computed(() => {
    return (value: string | number) => {
      const item = items.value.find(
        (i: { value: string | number; label: string }) =>
          String(i.value) === String(value),
      )
      return item?.label ?? String(value)
    }
  })

  return { items, label }
}
