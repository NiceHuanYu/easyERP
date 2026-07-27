<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h1 class="login-title">EasyERP</h1>
        <p class="login-subtitle">企业资源管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            native-type="submit"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-footer">© {{ new Date().getFullYear() }} EasyERP. All rights reserved.</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'login' })

const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login({
      username: form.username,
      password: form.password,
    })
    await navigateTo((route.query.redirect as string) || '/')
  } catch (err: any) {
    ElMessage.error(err?.data?.message || err?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.login-card {
  width: 420px;
  border-radius: 8px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin: 0;
}
</style>
