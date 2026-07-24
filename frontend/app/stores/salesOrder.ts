/**
 * 销售订单 Store
 */

import type { SalesOrder, OrderItem } from '~/types/entities'

export const useSalesOrderStore = defineStore('salesOrder', () => {
  const items = ref<SalesOrder[]>([
    { code: 'SO-2025-0089', customer: '华强电子', items: [{ material: '减速器 SA67', qty: 5, unit: '台', amount: 14250 }], delivery: '2025-08-15', status: '待审核', remark: '', sc: 'pending' },
    { code: 'SO-2025-0088', customer: '精密模具', items: [{ material: '电动滚筒 5.5kW', qty: 3, unit: '台', amount: 20400 }, { material: '减速器 SA67', qty: 2, unit: '台', amount: 5700 }], delivery: '2025-08-10', status: '生产中', remark: '', sc: 'progress' },
    { code: 'SO-2025-0087', customer: '鑫达科技', items: [{ material: '皮带输送机 10m', qty: 1, unit: '套', amount: 32500 }], delivery: '2025-07-25', status: '已发货', remark: '物流已发出', sc: 'shipped' },
    { code: 'SO-2025-0086', customer: '华强电子', items: [{ material: '螺旋输送机 LS400', qty: 2, unit: '台', amount: 37200 }], delivery: '2025-07-18', status: '已完成', remark: '已签收', sc: 'completed' },
    { code: 'SO-2025-0085', customer: '丰华重型', items: [{ material: '振动电机 YZO-8-4', qty: 10, unit: '台', amount: 16500 }, { material: '减速器 SA67', qty: 3, unit: '台', amount: 8550 }], delivery: '2025-08-20', status: '草稿', remark: '', sc: 'draft' },
    { code: 'SO-2025-0084', customer: '天工精密', items: [{ material: '刮板机配件-链节', qty: 50, unit: '件', amount: 4250 }], delivery: '2025-07-30', status: '已取消', remark: '客户取消', sc: 'cancelled' },
    { code: 'SO-2025-0083', customer: '宏远机械', items: [{ material: '皮带输送机 10m', qty: 1, unit: '套', amount: 32500 }], delivery: '2025-08-05', status: '已审核', remark: '加急处理', sc: 'approved' },
  ])

  function add(order: SalesOrder) {
    items.value.push({ ...order, items: order.items.map(i => ({ ...i })) })
  }

  function update(code: string, order: SalesOrder) {
    const idx = items.value.findIndex(o => o.code === code)
    if (idx !== -1) items.value[idx] = { ...order, items: order.items.map(i => ({ ...i })) }
  }

  function remove(code: string) {
    items.value = items.value.filter(o => o.code !== code)
  }

  function submit(code: string) {
    const order = items.value.find(o => o.code === code)
    if (order) {
      order.status = '待审核'
      order.sc = 'pending'
    }
  }

  return { items, add, update, remove, submit }
})
