<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-panel">
        <div class="modal-header">
          <h3>{{ title }}</h3>
          <button class="modal-close" @click="$emit('close')">✕</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="$emit('close')">取消</button>
          <button class="btn btn-primary" @click="$emit('save')">保存</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{ show: boolean; title: string }>()
defineEmits<{ close: []; save: [] }>()
</script>

<style scoped>
.modal-overlay { position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,.4); display:flex; align-items:center; justify-content:center; z-index:1000; }
.modal-panel { background:#fff; border-radius:12px; width:640px; max-width:90vw; max-height:85vh; display:flex; flex-direction:column; box-shadow:0 8px 32px rgba(0,0,0,.2); }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:20px 24px 0; }
.modal-header h3 { margin:0; font-size:17px; color:#333; }
.modal-close { background:none; border:none; font-size:18px; color:#999; cursor:pointer; padding:4px; }
.modal-close:hover { color:#333; }
.modal-body { padding:20px 24px; overflow-y:auto; flex:1; }
.modal-footer { display:flex; justify-content:flex-end; gap:10px; padding:16px 24px; border-top:1px solid #f0f0f0; }
.btn { padding:8px 20px; border:none; border-radius:8px; font-size:13px; cursor:pointer; transition:all .15s; font-weight:500; }
.btn-primary { background:#1a73e8; color:#fff; }
.btn-primary:hover { background:#1557b0; }
.btn-cancel { background:#f5f5f5; color:#666; }
.btn-cancel:hover { background:#eee; }
</style>
