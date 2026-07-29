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

  // 权限数据未加载完成 → 占位不可点击，避免闪烁
  if (permissions.length === 0) {
    dim(el)
    return
  }

  const hasPermission = required.some((perm) => permissions.includes(perm))
  if (!hasPermission) {
    hide(el)
  } else {
    show(el)
  }
}

/** 权限未加载时半透明占位，避免按钮闪烁 */
function dim(el: HTMLElement) {
  el.style.opacity = '0.4'
  el.style.pointerEvents = 'none'
  el.setAttribute('disabled', 'true')
  el.setAttribute('aria-hidden', 'true')
}

/** CSS 隐藏 + 标记 disabled，比 DOM 移除更难被 DevTools 绕过 */
function hide(el: HTMLElement) {
  el.style.display = 'none'
  el.setAttribute('disabled', 'true')
  el.setAttribute('aria-hidden', 'true')
}

function show(el: HTMLElement) {
  el.style.display = ''
  el.style.opacity = ''
  el.style.pointerEvents = ''
  el.removeAttribute('disabled')
  el.removeAttribute('aria-hidden')
}
