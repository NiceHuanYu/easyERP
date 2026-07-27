import { defineEventHandler, readBody, getHeaders } from 'h3'

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const target = `${config.backendUrl}${event.path}`

  // Forward relevant request headers
  const forwardHeaders: Record<string, string> = {}
  const incoming = getHeaders(event)
  for (const key of ['authorization', 'content-type', 'accept', 'cookie']) {
    if (incoming[key]) forwardHeaders[key] = incoming[key]
  }

  // DEBUG: log whether auth header is reaching the proxy
  if (event.path.includes('user-info')) {
    console.log('[proxy] incoming headers for user-info:', JSON.stringify(incoming))
    console.log('[proxy] forwardHeaders:', JSON.stringify(forwardHeaders))
  }

  const body =
    ['POST', 'PUT', 'PATCH'].includes(event.method)
      ? await readBody(event).catch(() => undefined)
      : undefined

  const res = await $fetch(target, {
    method: event.method as 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH',
    headers: forwardHeaders,
    body,
    ignoreResponseError: true,
  })

  // DEBUG: log backend response for user-info
  if (event.path.includes('user-info')) {
    console.log('[proxy] backend response for user-info:', JSON.stringify(res))
  }

  return res
})
