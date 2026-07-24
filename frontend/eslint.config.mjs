import { dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const _dirname = dirname(fileURLToPath(import.meta.url))

// https://eslint.nuxt.com
export default [
  {
    name: 'easyerp:ignores',
    ignores: [
      'node_modules/',
      '.nuxt/',
      '.output/',
      'dist/',
    ],
  },
  {
    name: 'easyerp:rules',
    files: ['**/*.{ts,vue}'],
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'warn',
      '@typescript-eslint/no-explicit-any': 'warn',
      'no-console': ['warn', { allow: ['warn', 'error'] }],
    },
  },
]
