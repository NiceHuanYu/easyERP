import { defineEventHandler, readBody, getHeaders } from 'h3'

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const target = `${config.backendUrl}${event.path}`

  const forwardHeaders: Record<string, string> = {}
  const incoming = getHeaders(event)
  for (const key of ['authorization', 'content-type', 'accept', 'cookie']) {
    if (incoming[key]) forwardHeaders[key] = incoming[key]
  }

  const body =
    ['POST', 'PUT', 'PATCH'].includes(event.method)
      ? await readBody(event).catch(() => undefined)
      : undefined

  return await $fetch(target, {
    method: event.method as 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH',
    headers: forwardHeaders,
    body,
    ignoreResponseError: true,
  })
})
