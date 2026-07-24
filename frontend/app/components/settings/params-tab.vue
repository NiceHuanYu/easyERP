<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <span class="erp-page-info">配置系统基础参数</span>
      </div>
    </div>
    <div class="erp-form-grid" style="max-width:640px;">
      <div class="erp-form-group full">
        <label>公司名称</label>
        <input v-model="p.companyName" placeholder="请输入公司名称" />
      </div>
      <div class="erp-form-group">
        <label>系统名称</label>
        <input v-model="p.systemName" placeholder="easyERP" />
      </div>
      <div class="erp-form-group">
        <label>默认币种</label>
        <select v-model="p.currency">
          <option>CNY (人民币)</option><option>USD (美元)</option><option>EUR (欧元)</option>
        </select>
      </div>
      <div class="erp-form-group">
        <label>默认税率 (%)</label>
        <input v-model.number="p.taxRate" type="number" min="0" max="100" step="0.5" />
      </div>
      <div class="erp-form-group">
        <label>默认页大小</label>
        <select v-model.number="p.pageSize">
          <option :value="10">10 条/页</option><option :value="20">20 条/页</option><option :value="50">50 条/页</option>
        </select>
      </div>
      <div class="erp-form-group">
        <label>日期格式</label>
        <select v-model="p.dateFormat">
          <option>YYYY-MM-DD</option><option>YYYY/MM/DD</option><option>DD/MM/YYYY</option>
        </select>
      </div>
      <div class="erp-form-group full">
        <label>公司地址</label>
        <input v-model="p.address" placeholder="公司地址" />
      </div>
    </div>
    <div style="margin-top:16px;text-align:right;max-width:640px;">
      <button class="erp-btn erp-btn-primary" @click="save">保存参数</button>
    </div>
  </div>
</template>

<script setup lang="ts">
const p = reactive({
  companyName: '某某制造有限公司',
  systemName: 'easyERP',
  currency: 'CNY (人民币)',
  taxRate: 13,
  pageSize: 10,
  dateFormat: 'YYYY-MM-DD',
  address: '广东省深圳市宝安区',
})

function save(){ alert('系统参数已保存（演示）') }
</script>

<style scoped>
.erp-form-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;}
.erp-form-group{display:flex;flex-direction:column;gap:4px;}
.erp-form-group.full{grid-column:1/-1;}
.erp-form-group label{font-size:13px;color:#555;font-weight:500;}
.erp-form-group input,.erp-form-group select{padding:8px 12px;border:1px solid #e0e0e0;border-radius:6px;font-size:13px;outline:none;background:#fafafa;}
.erp-form-group input:focus,.erp-form-group select:focus{border-color:#1a73e8;background:#fff;}
</style>
