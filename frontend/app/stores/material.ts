/**
 * 物料管理 Store
 *
 * 集中管理物料数据，各 Tab 组件通过此 store 读写数据，
 * 替代原先各组件内独立的 ref([]) mock 数据。
 */

import type { Material } from '~/types/entities'

export const useMaterialStore = defineStore('material', () => {
  // ---- 状态 ----
  const items = ref<Material[]>([
    { code: 'MAT-001', name: '45#圆钢 Φ20',      category: '原材料',  spec: 'Φ20×6000mm', unit: 'kg',  stock: 520,  safetyStock: 200, price: 4.80,  status: '启用', remark: '' },
    { code: 'MAT-002', name: '304不锈钢板 2mm',   category: '原材料',  spec: '2×1200×2400mm', unit: '张', stock: 85,   safetyStock: 50,  price: 68.50, status: '启用', remark: '' },
    { code: 'MAT-003', name: 'Q235槽钢 10#',      category: '原材料',  spec: '10# 6m/根', unit: '根',  stock: 120,  safetyStock: 80,  price: 35.20, status: '启用', remark: '' },
    { code: 'MAT-004', name: 'M8×30六角螺栓',     category: '辅料',    spec: 'M8×30 8.8级', unit: '个',  stock: 2850, safetyStock: 500, price: 0.45,  status: '启用', remark: '' },
    { code: 'MAT-005', name: 'Φ50×3无缝钢管',     category: '原材料',  spec: 'Φ50×3×6000mm', unit: '根', stock: 45,   safetyStock: 60,  price: 42.00, status: '启用', remark: '低于安全库存' },
    { code: 'MAT-006', name: 'PVC绝缘胶带',        category: '辅料',    spec: '18mm×20m 黑色', unit: '卷', stock: 320,  safetyStock: 100, price: 3.20,  status: '启用', remark: '' },
    { code: 'MAT-007', name: '铜排 TMY-40×4',     category: '原材料',  spec: '40×4mm', unit: 'm',   stock: 68,   safetyStock: 30,  price: 86.00, status: '启用', remark: '' },
    { code: 'MAT-008', name: '壳体-减速器上盖',    category: '半成品',  spec: 'HT250 铸件', unit: '件', stock: 0,    safetyStock: 20,  price: 125.00,status: '停用', remark: '模具维修中' },
    { code: 'MAT-009', name: '瓦楞纸箱 600×400',   category: '包装物',  spec: '600×400×300mm 三层', unit: '个', stock: 560, safetyStock: 200, price: 4.50,  status: '启用', remark: '' },
    { code: 'MAT-010', name: '乳化切削液 18L',     category: '辅料',    spec: '18L/桶 水溶性', unit: '桶', stock: 12,   safetyStock: 5,   price: 185.00,status: '启用', remark: '' },
    { code: 'MAT-011', name: '铝合金棒 6061-T6',   category: '原材料',  spec: 'Φ30×1000mm', unit: 'kg',  stock: 210,  safetyStock: 100, price: 28.50, status: '启用', remark: '' },
    { code: 'MAT-012', name: '减速机输出轴锻坯',   category: '半成品',  spec: '40Cr 锻坯', unit: '件',  stock: 35,   safetyStock: 25,  price: 56.00, status: '启用', remark: '' },
    { code: 'MAT-013', name: 'O型密封圈 Φ50×3.5', category: '辅料',    spec: 'Φ50×3.5mm 丁腈橡胶', unit: '只', stock: 4200, safetyStock: 1000,price: 0.18,  status: '启用', remark: '' },
    { code: 'MAT-014', name: 'PET打包带',          category: '包装物',  spec: '16mm×0.8mm 绿色', unit: '卷', stock: 88,   safetyStock: 30,  price: 12.60, status: '启用', remark: '' },
    { code: 'MAT-015', name: '焊丝 ER50-6 Φ1.2', category: '辅料',    spec: 'Φ1.2mm 15kg/盘', unit: '盘', stock: 18,   safetyStock: 10,  price: 95.00, status: '启用', remark: '' },
  ])

  const loading = ref(false)

  // ---- 计算属性 ----
  const categories = computed(() => [...new Set(items.value.map(m => m.category))])

  const lowStockItems = computed(() =>
    items.value.filter(m => m.stock <= m.safetyStock && m.status === '启用'),
  )

  // ---- 操作 ----
  function add(item: Material) {
    items.value.push({ ...item })
  }

  function update(code: string, item: Material) {
    const idx = items.value.findIndex(m => m.code === code)
    if (idx !== -1) items.value[idx] = { ...item }
  }

  function remove(code: string) {
    items.value = items.value.filter(m => m.code !== code)
  }

  function getByCode(code: string): Material | undefined {
    return items.value.find(m => m.code === code)
  }

  return {
    items,
    loading,
    categories,
    lowStockItems,
    add,
    update,
    remove,
    getByCode,
  }
})
