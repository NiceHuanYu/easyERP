<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">easyERP</h1>
      <p class="login-subtitle">小型制造企业管理系统</p>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="login-hint">
        <p>测试账号：admin / admin123</p>
        <p>测试账号：user / user123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
})

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const { login, isLoggedIn } = useAuth()

// 已登录则直接跳转
if (isLoggedIn.value) {
  navigateTo('/dashboard')
}

async function handleLogin() {
  error.value = ''
  loading.value = true

  // 模拟短暂延迟
  await new Promise(resolve => setTimeout(resolve, 500))

  const result = login(username.value, password.value)
  loading.value = false

  if (result.ok) {
    navigateTo('/dashboard')
  } else {
    error.value = result.message
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #1a73e8;
  margin: 0 0 4px 0;
}

.login-subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin: 0 0 32px 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-group input {
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.error-msg {
  color: #d32f2f;
  font-size: 13px;
  margin: 0;
  text-align: center;
}

.login-btn {
  padding: 12px;
  background: #1a73e8;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:hover {
  background: #1557b0;
}

.login-btn:disabled {
  background: #93b8f0;
  cursor: not-allowed;
}

.login-hint {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  text-align: center;
}

.login-hint p {
  margin: 4px 0;
  font-size: 12px;
  color: #999;
}
</style>
