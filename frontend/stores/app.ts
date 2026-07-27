import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', () => {
  // ---- State ----
  const sidebarCollapsed = ref<boolean>(false)

  // ---- Actions ----
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    sidebarCollapsed,
    toggleSidebar,
  }
})
