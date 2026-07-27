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
  return { list: result.records, total: result.total }
}
