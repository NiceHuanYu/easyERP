// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: process.env.NODE_ENV !== 'production' },
  devServer: {
    port: 3000,
    host: '0.0.0.0',
  },

  modules: [
    '@pinia/nuxt',
  ],

  css: [
    'element-plus/dist/index.css',
    '~/assets/css/main.css',
  ],

  app: {
    head: {
      title: 'EasyERP',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  },

  vite: {
    resolve: {
      alias: {
        'echarts': 'echarts',
      },
    },
  },

  routeRules: {
    '/login': { ssr: false },
  },

  runtimeConfig: {
    backendUrl: 'http://localhost:8080',
  },
})
