// Unified API response types matching backend

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageParam {
  page: number
  size: number
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

// Convert frontend pagination to backend PageParam
export function toPageParam(page: number, pageSize: number): PageParam {
  return { page, size: pageSize }
}

// Extract list + total from PageResult for frontend use
export interface PageData<T> {
  list: T[]
  total: number
}

export function fromPageResult<T>(result: PageResult<T>): PageData<T> {
  return { list: result.records, total: Number(result.total) || 0 }
}

// ==================== 错误类型 ====================

/** 后端 API 错误，可安全用于 catch 分支的类型判断 */
export class ApiError extends Error {
  constructor(
    message: string,
    public code: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}
