import { useAuthStore } from '../../stores/auth'
import { toPageParam, fromPageResult, ApiError, type PageData } from '#shared/types/api'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

async function request<T>(
  method: HttpMethod,
  path: string,
  body?: unknown,
  query?: Record<string, string | number | undefined>,
): Promise<T> {
  const authStore = useAuthStore()
  const headers: Record<string, string> = {}
  if (authStore.token) headers.Authorization = `Bearer ${authStore.token}`

  const qs = query
    ? '?' +
      Object.entries(query)
        .filter(([, v]) => v !== undefined && v !== '')
        .map(([k, v]) => `${k}=${encodeURIComponent(v!)}`)
        .join('&')
    : ''

  const res = await $fetch<{ code: number; message: string; data: T }>(
    `/api/v1${path}${qs}`,
    { method, body, headers },
  )
  if (res.code !== 200) {
    throw new ApiError(res.message || `请求失败 (${res.code})`, res.code)
  }
  return res.data
}

export const api = {
  get<T>(path: string, query?: Record<string, string | number | undefined>) {
    return request<T>('GET', path, undefined, query)
  },
  post<T>(path: string, body?: unknown) {
    return request<T>('POST', path, body)
  },
  put<T>(path: string, body?: unknown) {
    return request<T>('PUT', path, body)
  },
  del<T>(path: string) {
    return request<T>('DELETE', path)
  },

  async page<T>(
    path: string,
    page: number,
    pageSize: number,
    extraQuery?: Record<string, string | number | undefined>,
  ): Promise<PageData<T>> {
    const data = await request<{ total: number; records: T[] }>(
      'GET',
      path,
      undefined,
      { ...toPageParam(page, pageSize), ...extraQuery },
    )
    return fromPageResult({ ...data, page, size: pageSize })
  },
}
