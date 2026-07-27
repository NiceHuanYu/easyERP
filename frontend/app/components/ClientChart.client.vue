<template>
  <v-chart :option="option" :autoresize="autoresize" v-bind="$attrs" />
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'

defineProps<{
  option: Record<string, unknown>
  autoresize?: boolean
}>()
</script>
