import type { UseFetchOptions } from 'nuxt/app'
import { useAuthStore } from '../../stores/auth'

export function useApi<T = unknown>(
  url: string | (() => string),
  options?: UseFetchOptions<T>,
) {
  const authStore = useAuthStore()

  const mergedOptions: UseFetchOptions<T> = {
    baseURL: '/api/v1',
    ...options,
    headers: {
      ...(authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {}),
      ...(options?.headers as Record<string, string> | undefined),
    },
    onResponseError({ response }) {
      // Unified error handling via ElMessage
      const msg =
        (response._data as { message?: string })?.message ||
        `请求失败 (${response.status})`
      ElMessage.error(msg)

      // Call user's own error handler if provided
      options?.onResponseError?.({ response } as Parameters<NonNullable<UseFetchOptions<T>['onResponseError']>>[0])
    },
  }

  return useFetch<T>(url, mergedOptions)
}
