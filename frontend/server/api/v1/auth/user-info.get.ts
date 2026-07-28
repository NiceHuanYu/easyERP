import { defineEventHandler, getHeaders } from 'h3'

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()

  const forwardHeaders: Record<string, string> = {}
  const incoming = getHeaders(event)
  for (const key of ['authorization', 'content-type', 'accept', 'cookie']) {
    if (incoming[key]) forwardHeaders[key] = incoming[key]
  }

  return await $fetch(`${config.backendUrl}${event.path}`, {
    method: 'GET',
    headers: forwardHeaders,
    ignoreResponseError: true,
  })
})
