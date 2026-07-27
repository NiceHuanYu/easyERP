import { useAuthStore } from '../../stores/auth'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('permission', {
    mounted(el: HTMLElement, binding) {
      checkPermission(el, binding)
    },
    updated(el: HTMLElement, binding) {
      checkPermission(el, binding)
    },
  })
})

function checkPermission(
  el: HTMLElement,
  binding: { value: string | string[] | undefined },
): void {
  const { value } = binding
  if (!value) return

  const required: string[] = Array.isArray(value) ? value : [value]
  if (required.length === 0) return

  const authStore = useAuthStore()
  const permissions: string[] = authStore.permissions ?? []

  // If no permission data yet (e.g. still loading), don't remove the element
  if (permissions.length === 0) return

  // Super admin has all permissions
  if (permissions.includes('*')) return

  const hasPermission = required.some((perm) => permissions.includes(perm))
  if (!hasPermission) {
    el.parentNode?.removeChild(el)
  }
}
