<template>
  <div class="erp-tab-body">
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <span class="erp-page-info">配置各业务单据的编号生成规则</span>
      </div>
    </div>
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead><tr>
          <th>单据类型</th><th>编号前缀</th><th style="text-align:center;">含日期</th><th style="text-align:center;">流水位数</th><th>示例</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in rules" :key="row.type">
            <td>{{ row.type }}</td>
            <td><input v-model="row.prefix" class="erp-tbl-input" style="width:80px;" /></td>
            <td style="text-align:center;"><input type="checkbox" v-model="row.includeDate" /></td>
            <td style="text-align:center;">
              <select v-model.number="row.digits" class="erp-tbl-select" style="width:60px;">
                <option :value="3">3</option><option :value="4">4</option><option :value="5">5</option><option :value="6">6</option>
              </select>
            </td>
            <td class="erp-cell-code">{{ example(row) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div style="margin-top:16px;text-align:right;">
      <button class="erp-btn erp-btn-primary" @click="save">保存编号规则</button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Rule { type:string;prefix:string;includeDate:boolean;digits:number }
const rules = ref<Rule[]>([
  { type:'销售订单',prefix:'SO-',includeDate:true,digits:4 },
  { type:'销售报价',prefix:'QTN-',includeDate:false,digits:3 },
  { type:'采购申请',prefix:'PR-',includeDate:false,digits:3 },
  { type:'采购订单',prefix:'PO-',includeDate:true,digits:4 },
  { type:'采购付款',prefix:'PAY-',includeDate:false,digits:3 },
  { type:'生产工单',prefix:'WO-',includeDate:true,digits:4 },
  { type:'生产领料',prefix:'MO-',includeDate:false,digits:3 },
  { type:'生产报工',prefix:'RPT-',includeDate:false,digits:3 },
  { type:'库存调拨',prefix:'TR-',includeDate:false,digits:3 },
])

function example(r:Rule){
  const pad = '0'.repeat(r.digits)
  const date = r.includeDate ? new Date().getFullYear()+'-' : ''
  return `${r.prefix}${date}${pad.slice(1)}1`
}

function save(){ alert('编号规则已保存（演示）') }
</script>

<style scoped>
.erp-tbl-input{width:100%;padding:6px 8px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;font-family:inherit;}
.erp-tbl-input:focus{border-color:#1a73e8;background:#fff;}
.erp-tbl-select{padding:6px 4px;border:1px solid #e0e0e0;border-radius:4px;font-size:12px;outline:none;background:#fafafa;}
</style>
