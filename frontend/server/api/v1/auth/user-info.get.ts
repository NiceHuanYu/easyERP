import { defineEventHandler, getHeaders } from 'h3'

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const target = `${config.backendUrl}${event.path}`

  const forwardHeaders: Record<string, string> = {}
  const incoming = getHeaders(event)
  for (const key of ['authorization', 'content-type', 'accept', 'cookie']) {
    if (incoming[key]) forwardHeaders[key] = incoming[key]
  }

  console.log('[proxy user-info] incoming headers:', JSON.stringify(incoming))
  console.log('[proxy user-info] forwardHeaders:', JSON.stringify(forwardHeaders))

  const res = await $fetch(target, {
    method: event.method as 'GET',
    headers: forwardHeaders,
    ignoreResponseError: true,
  })

  console.log('[proxy user-info] backend response:', JSON.stringify(res))
  return res
})
