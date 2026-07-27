import ElementPlus, { ID_INJECTION_KEY, ZINDEX_INJECTION_KEY } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

export default defineNuxtPlugin((nuxtApp) => {
  // Provide Element Plus SSR-required injection keys
  nuxtApp.vueApp.provide(ID_INJECTION_KEY, {
    prefix: 1024,
    current: 0,
  })
  nuxtApp.vueApp.provide(ZINDEX_INJECTION_KEY, { current: 2000 })

  // Element Plus CSS is imported globally via nuxt.config.ts
  nuxtApp.vueApp.use(ElementPlus, { locale: zhCn })

  // Register all Element Plus icons globally
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    nuxtApp.vueApp.component(key, component)
  }
})
