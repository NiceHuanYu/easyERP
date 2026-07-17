<template>
  <!-- 此页面仅做重定向，无内容 -->
  <div></div>
</template>

<script setup lang="ts">
const { isLoggedIn } = useAuth()

if (isLoggedIn.value) {
  await navigateTo('/dashboard', { replace: true })
} else {
  await navigateTo('/login', { replace: true })
}
</script>
