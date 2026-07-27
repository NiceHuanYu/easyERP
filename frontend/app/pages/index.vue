<template>
  <div class="index-page">
    <el-result icon="info" title="正在跳转...">
      <template #sub-title>
        <p>正在前往工作台，请稍候</p>
      </template>
      <template #extra>
        <el-button type="primary" @click="navigateTo('/dashboard')">
          如果没有自动跳转，点击这里
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
// 301 重定向到 Dashboard 页面
await navigateTo('/dashboard', { redirectCode: 301 })
</script>

<style scoped>
.index-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
</style>
