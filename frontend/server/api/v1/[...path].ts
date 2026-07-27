import { defineEventHandler, getRequestPath, readBody, getMethod } from 'h3'

/**
 * Catch-all CRUD handler for /api/v1/*
 * Returns mock paginated data for GET, success message for POST/PUT/DELETE.
 */
export default defineEventHandler(async (event) => {
  const method = getMethod(event)
  const path = getRequestPath(event)
  // Extract the resource name from the path, e.g. /api/v1/product/list → product/list
  const resource = path.replace(/^\/api\/v1\//, '')

  // Non-GET methods: simple success response
  if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
    let body: Record<string, unknown> | null = null
    if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
      body = await readBody(event).catch(() => null)
    }
    return {
      code: 200,
      message: 'success',
      data: body ?? {},
    }
  }

  // GET: return mock paginated list
  return {
    code: 200,
    message: 'success',
    data: {
      list: [],
      total: 0,
      pageSize: 20,
      current: 1,
      resource,
    },
  }
})
