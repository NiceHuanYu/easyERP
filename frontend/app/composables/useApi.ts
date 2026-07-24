/**
 * useApi — 统一的 API 请求层
 *
 * 封装 $fetch，提供：
 * - 统一的 base URL 配置
 * - 请求/响应拦截（loading 状态、错误处理）
 * - 类型安全的泛型请求方法
 *
 * 当前阶段：在 mock 模式下返回本地数据，方便后续平滑切换到真实 API。
 */

import type { ApiResponse, PaginatedResult, PaginationParams } from '~/types/entities'

/** 是否启用 mock 模式（后续可通过环境变量控制） */
const MOCK_MODE = true

/** API 基础路径（部署时通过环境变量配置） */
const API_BASE = '/api'

interface RequestOptions {
  /** 是否显示 loading 状态 */
  loading?: boolean
}

// ============================================================
// 全局 loading 状态
// ============================================================
const globalLoading = ref(false)

export function useApi() {
  const loading = ref(false)

  /**
   * 通用 GET 请求
   */
  async function get<T>(url: string, options?: RequestOptions): Promise<T> {
    if (options?.loading !== false) {
      loading.value = true
      globalLoading.value = true
    }
    try {
      if (MOCK_MODE) {
        // Mock 模式：模拟网络延迟后抛出，由调用方 catch 处理
        await new Promise(r => setTimeout(r, 100))
        throw new Error('MOCK: 请在各组件中使用本地 ref 数据替代')
      }
      return await $fetch<T>(`${API_BASE}${url}`, { method: 'GET' })
    } finally {
      loading.value = false
      globalLoading.value = false
    }
  }

  /**
   * 通用 POST 请求
   */
  async function post<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
    if (options?.loading !== false) {
      loading.value = true
      globalLoading.value = true
    }
    try {
      if (MOCK_MODE) {
        await new Promise(r => setTimeout(r, 200))
        throw new Error('MOCK: 请在各组件中使用本地 ref 数据替代')
      }
      return await $fetch<T>(`${API_BASE}${url}`, {
        method: 'POST',
        body,
      })
    } finally {
      loading.value = false
      globalLoading.value = false
    }
  }

  /**
   * 通用 PUT 请求
   */
  async function put<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
    if (options?.loading !== false) {
      loading.value = true
      globalLoading.value = true
    }
    try {
      if (MOCK_MODE) {
        await new Promise(r => setTimeout(r, 200))
        throw new Error('MOCK: 请在各组件中使用本地 ref 数据替代')
      }
      return await $fetch<T>(`${API_BASE}${url}`, {
        method: 'PUT',
        body,
      })
    } finally {
      loading.value = false
      globalLoading.value = false
    }
  }

  /**
   * 通用 DELETE 请求
   */
  async function del<T>(url: string, options?: RequestOptions): Promise<T> {
    if (options?.loading !== false) {
      loading.value = true
      globalLoading.value = true
    }
    try {
      if (MOCK_MODE) {
        await new Promise(r => setTimeout(r, 150))
        throw new Error('MOCK: 请在各组件中使用本地 ref 数据替代')
      }
      return await $fetch<T>(`${API_BASE}${url}`, { method: 'DELETE' })
    } finally {
      loading.value = false
      globalLoading.value = false
    }
  }

  return { loading, get, post, put, del }
}

/** 全局 loading 状态（可用于页面顶部进度条） */
export function useGlobalLoading() {
  return readonly(globalLoading)
}
