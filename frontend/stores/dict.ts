import { defineStore } from 'pinia'
import { useAuthStore } from './auth'

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
      const authStore = useAuthStore()
      const headers: Record<string, string> = {}
      if (authStore.token) headers.Authorization = `Bearer ${authStore.token}`
      const res = await $fetch<{ code: number; data: Record<string, DictItem[]> }>(
        '/api/v1/system/dicts/all',
        { headers },
      )
      if (res.code !== 200 || !res.data) throw new Error('加载字典失败')
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
