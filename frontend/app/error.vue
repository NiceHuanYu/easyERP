<template>
  <NuxtLayout name="login">
    <div class="error-page">
      <!-- 404 -->
      <el-result
        v-if="error.statusCode === 404"
        icon="warning"
        title="404"
        sub-title="抱歉，您访问的页面不存在"
      >
        <template #extra>
          <el-button type="primary" @click="handleBackHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
        </template>
      </el-result>

      <!-- 403 -->
      <el-result
        v-else-if="error.statusCode === 403"
        icon="warning"
        title="403"
        sub-title="抱歉，您没有权限访问此页面"
      >
        <template #extra>
          <el-button type="primary" @click="handleBackHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
        </template>
      </el-result>

      <!-- 其他错误 -->
      <el-result
        v-else
        icon="error"
        :title="String(error.statusCode || 'Error')"
        :sub-title="error.message || '服务器出了点问题，请稍后再试'"
      >
        <template #extra>
          <el-button type="primary" @click="handleBackHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
        </template>
      </el-result>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { House } from '@element-plus/icons-vue'

const props = defineProps<{
  error: {
    statusCode?: number | null
    message?: string | null
    url?: string
  }
}>()

async function handleBackHome() {
  // clearError 是 Nuxt 提供的工具函数，用于清除错误状态
  await clearError({ redirect: '/dashboard' })
}
</script>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
</style>
