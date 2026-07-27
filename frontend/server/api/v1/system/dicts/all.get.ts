import { defineEventHandler } from 'h3'

export default defineEventHandler(() => {
  return {
    code: 200,
    message: 'success',
    data: [
      {
        dictType: 'order_status',
        dictName: '订单状态',
        items: [
          { label: '待审核', value: 'pending_audit' },
          { label: '待发货', value: 'pending_ship' },
          { label: '已发货', value: 'shipped' },
          { label: '已完成', value: 'completed' },
          { label: '已取消', value: 'cancelled' },
          { label: '退货中', value: 'returning' },
        ],
      },
      {
        dictType: 'unit',
        dictName: '计量单位',
        items: [
          { label: '个', value: 'piece' },
          { label: '箱', value: 'box' },
          { label: '千克', value: 'kg' },
          { label: '克', value: 'g' },
          { label: '升', value: 'l' },
          { label: '米', value: 'm' },
          { label: '件', value: 'item' },
          { label: '套', value: 'set' },
        ],
      },
      {
        dictType: 'warehouse_type',
        dictName: '仓库类型',
        items: [
          { label: '普通仓', value: 'normal' },
          { label: '冷库', value: 'cold' },
          { label: '危险品仓', value: 'dangerous' },
          { label: '保税仓', value: 'bonded' },
        ],
      },
      {
        dictType: 'customer_level',
        dictName: '客户等级',
        items: [
          { label: 'VIP', value: 'vip' },
          { label: '普通', value: 'normal' },
          { label: '潜在', value: 'potential' },
          { label: '流失', value: 'lost' },
        ],
      },
      {
        dictType: 'payment_method',
        dictName: '付款方式',
        items: [
          { label: '现金', value: 'cash' },
          { label: '银行转账', value: 'bank_transfer' },
          { label: '微信支付', value: 'wechat_pay' },
          { label: '支付宝', value: 'alipay' },
          { label: '月结', value: 'monthly' },
        ],
      },
      {
        dictType: 'product_category',
        dictName: '商品分类',
        items: [
          { label: '原材料', value: 'raw_material' },
          { label: '半成品', value: 'semi_finished' },
          { label: '成品', value: 'finished' },
          { label: '备件', value: 'spare_part' },
        ],
      },
      {
        dictType: 'gender',
        dictName: '性别',
        items: [
          { label: '男', value: 'male' },
          { label: '女', value: 'female' },
        ],
      },
      {
        dictType: 'supplier_level',
        dictName: '供应商等级',
        items: [
          { label: 'A级', value: 'A' },
          { label: 'B级', value: 'B' },
          { label: 'C级', value: 'C' },
        ],
      },
    ],
  }
})
