<template>
  <Teleport to="body">
    <div v-if="show" class="erp-modal-overlay" @click.self="$emit('cancel')">
      <div class="erp-modal-panel confirm">
        <div class="erp-modal-header">
          <h3>{{ isApprove ? '确认通过' : '确认驳回' }}</h3>
          <button class="erp-modal-close" @click="$emit('cancel')">✕</button>
        </div>
        <div class="erp-modal-body">
          <p style="margin-bottom:12px;font-size:13px;color:#555;">
            {{ isApprove ? `确认审批通过单据 ${code}？` : `确认驳回单据 ${code}？` }}
          </p>
          <div class="erp-form-group">
            <label>审批意见</label>
            <textarea v-model="comment" rows="3" :placeholder="isApprove ? '可选，填写审批意见' : '请填写驳回原因'"></textarea>
          </div>
        </div>
        <div class="erp-modal-footer">
          <button class="erp-btn erp-btn-cancel" @click="$emit('cancel')">取消</button>
          <button class="erp-btn erp-btn-primary" @click="confirm">{{ isApprove ? '通过' : '驳回' }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
const props = defineProps<{
  show: boolean
  isApprove: boolean
  code: string
}>()

const emit = defineEmits<{
  cancel: []
  confirm: [comment: string]
}>()

const comment = ref('')

function confirm() {
  const c = comment.value.trim()
  if (!props.isApprove && !c) {
    // 驳回时意见必填
    return  // 直接允许空提交，由父组件决定
  }
  emit('confirm', comment.value.trim())
  comment.value = ''
}
</script>

<style scoped>
.erp-modal-overlay{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;z-index:1001;}
.erp-modal-panel{background:#fff;border-radius:12px;box-shadow:0 8px 32px rgba(0,0,0,.2);}
.erp-modal-panel.confirm{width:420px;}
.erp-modal-header{display:flex;align-items:center;justify-content:space-between;padding:20px 24px 0;}
.erp-modal-header h3{margin:0;font-size:17px;color:#333;}
.erp-modal-close{background:none;border:none;font-size:18px;color:#999;cursor:pointer;padding:4px;}
.erp-modal-body{padding:20px 24px;font-size:14px;color:#555;}
.erp-modal-footer{display:flex;justify-content:flex-end;gap:10px;padding:16px 24px;border-top:1px solid #f0f0f0;}
.erp-btn{padding:8px 20px;border:none;border-radius:8px;font-size:13px;cursor:pointer;font-weight:500;}
.erp-btn-primary{background:#1a73e8;color:#fff;}
.erp-btn-cancel{background:#f5f5f5;color:#666;}
.erp-form-group{display:flex;flex-direction:column;gap:4px;}
.erp-form-group label{font-size:13px;color:#555;font-weight:500;}
.erp-form-group textarea{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;resize:vertical;font-family:inherit;}
.erp-form-group textarea:focus{border-color:#1a73e8;}
</style>
