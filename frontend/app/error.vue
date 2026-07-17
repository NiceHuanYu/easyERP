<template>
  <div class="error-page">
    <div class="error-card">
      <div class="error-code">{{ error?.statusCode || 404 }}</div>
      <h1 class="error-title">
        {{ error?.statusCode === 404 ? '页面未找到' : error?.statusCode === 500 ? '服务器错误' : '出错了' }}
      </h1>
      <p class="error-message">{{ error?.message || error?.statusMessage || '页面不存在或已被移除。' }}</p>
      <div class="error-actions">
        <button class="btn-home" @click="goHome">返回首页</button>
        <button class="btn-back" @click="goBack">返回上一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  error?: { statusCode?: number; message?: string; statusMessage?: string }
}>()

const error = computed(() => props.error)

function goHome() {
  clearError({ redirect: '/' })
}

function goBack() {
  clearError()
  if (import.meta.client) {
    window.history.back()
  }
}
</script>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.error-card {
  text-align: center;
  background: #fff;
  border-radius: 12px;
  padding: 48px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.error-code {
  font-size: 80px;
  font-weight: 800;
  color: #1a73e8;
  line-height: 1;
  margin-bottom: 8px;
  opacity: 0.6;
}

.error-title {
  font-size: 24px;
  color: #333;
  margin: 0 0 12px 0;
}

.error-message {
  color: #888;
  font-size: 14px;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-home {
  padding: 10px 28px;
  background: #1a73e8;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-home:hover {
  background: #1557b0;
}

.btn-back {
  padding: 10px 28px;
  background: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}
</style>
