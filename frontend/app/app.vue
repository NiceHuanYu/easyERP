<template>
  <div id="easyerp-app">
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<script setup lang="ts">
// 初始化时确保 useAuth 的客户端状态恢复
if (import.meta.client) {
  useAuth()
}
</script>

