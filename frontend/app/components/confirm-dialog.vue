<template>
  <Teleport to="body">
    <div v-if="show" class="overlay" @click.self="$emit('cancel')">
      <div class="panel confirm">
        <div class="header"><h3>{{ title }}</h3></div>
        <div class="body"><slot /></div>
        <div class="footer">
          <button class="btn btn-cancel" @click="$emit('cancel')">取消</button>
          <button class="btn btn-danger" @click="$emit('confirm')">确认删除</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{ show: boolean; title?: string }>()
defineEmits<{ confirm: []; cancel: [] }>()
</script>

<style scoped>
.overlay { position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,.4); display:flex; align-items:center; justify-content:center; z-index:1000; }
.panel { background:#fff; border-radius:12px; max-width:90vw; box-shadow:0 8px 32px rgba(0,0,0,.2); }
.panel.confirm { width:420px; }
.header { padding:20px 24px 0; }
.header h3 { margin:0; font-size:17px; color:#333; }
.body { padding:20px 24px; font-size:14px; color:#555; }
.footer { display:flex; justify-content:flex-end; gap:10px; padding:16px 24px; border-top:1px solid #f0f0f0; }
.btn { padding:8px 20px; border:none; border-radius:8px; font-size:13px; cursor:pointer; font-weight:500; }
.btn-cancel { background:#f5f5f5; color:#666; }
.btn-cancel:hover { background:#eee; }
.btn-danger { background:#d32f2f; color:#fff; }
.btn-danger:hover { background:#b71c1c; }
</style>
