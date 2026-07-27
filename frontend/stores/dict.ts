import { defineStore } from 'pinia'

interface DictItem {
  label: string
  value: string
}

export const useDictStore = defineStore('dict', () => {
  // ---- State ----
  const dictMap = ref<Record<string, DictItem[]>>({})
  const loaded = ref<boolean>(false)

  // ---- Getters ----
  function getDictItems(dictType: string): DictItem[] {
    return dictMap.value[dictType] ?? []
  }

  function getDictLabel(dictType: string, value: any): string {
    const items = dictMap.value[dictType]
    if (!items) return ''
    const item = items.find((i) => i.value === String(value))
    return item?.label ?? ''
  }

  // ---- Actions ----
  async function fetchAllDicts(): Promise<void> {
    try {
      const res = await $fetch<{ code: number; data: Record<string, DictItem[]> }>(
        '/api/v1/system/dicts/all',
      )
      dictMap.value = res.data
      loaded.value = true
    } catch (error: any) {
      console.error('Fetch dicts failed:', error?.message ?? error)
      throw error
    }
  }

  return {
    // state
    dictMap,
    loaded,
    // getters
    getDictItems,
    getDictLabel,
    // actions
    fetchAllDicts,
  }
})
