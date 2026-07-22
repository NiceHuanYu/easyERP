<template>
  <div class="material-tab">
    <!-- 工具栏 -->
    <div class="erp-toolbar">
      <div class="erp-toolbar-left">
        <div class="erp-search-box">
          <span class="erp-search-icon">🔍</span>
          <input v-model="search" type="text" placeholder="搜索物料编码、名称、规格..." class="erp-search-input" />
        </div>
        <select v-model="filterCategory" class="erp-filter-select">
          <option value="">全部类别</option>
          <option v-for="cat in materialCategories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <select v-model="filterStatus" class="erp-filter-select">
          <option value="">全部状态</option>
          <option value="启用">启用</option>
          <option value="停用">停用</option>
        </select>
      </div>
      <div class="erp-toolbar-right">
        <button class="erp-btn erp-btn-primary" @click="openForm()">＋ 新建物料</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="erp-table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th @click="sortBy('code')" class="sortable">
              物料编码 <span v-if="sortField === 'code'">{{ sortAsc ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('name')" class="sortable">
              物料名称 <span v-if="sortField === 'name'">{{ sortAsc ? '▲' : '▼' }}</span>
            </th>
            <th>类别</th>
            <th>规格型号</th>
            <th>单位</th>
            <th @click="sortBy('stock')" class="sortable" style="text-align:right;">
              库存 <span v-if="sortField === 'stock'">{{ sortAsc ? '▲' : '▼' }}</span>
            </th>
            <th style="text-align:right;">安全库存</th>
            <th style="text-align:right;">参考单价(元)</th>
            <th>状态</th>
            <th style="text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedData" :key="item.code">
            <td class="erp-cell-code">{{ item.code }}</td>
            <td class="erp-cell-name">{{ item.name }}</td>
            <td><span class="erp-tag">{{ item.category }}</span></td>
            <td class="erp-cell-spec">{{ item.spec }}</td>
            <td>{{ item.unit }}</td>
            <td class="erp-cell-num" :class="{ 'low-stock': item.stock <= item.safetyStock }">{{ item.stock }}</td>
            <td class="erp-cell-num">{{ item.safetyStock }}</td>
            <td class="erp-cell-num">{{ item.price.toFixed(2) }}</td>
            <td>
              <span :class="['erp-dot', item.status === '启用' ? 'on' : '']"></span>
              {{ item.status }}
            </td>
            <td class="erp-cell-acts">
              <button class="erp-lnk" @click="openForm(item)">编辑</button>
              <button class="erp-lnk erp-lnk-danger" @click="confirmDelete(item)">删除</button>
            </td>
          </tr>
          <tr v-if="pagedData.length === 0">
            <td colspan="10" class="erp-cell-empty">暂无匹配的物料数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="erp-pagination">
      <span class="erp-page-info">共 {{ filteredData.length }} 条，第 {{ page }} / {{ totalPages }} 页</span>
      <div class="erp-page-controls">
        <button class="erp-page-btn" :disabled="page <= 1" @click="page = 1">首页</button>
        <button class="erp-page-btn" :disabled="page <= 1" @click="page--">‹</button>
        <span class="erp-page-num" v-for="p in visiblePages" :key="p" :class="{ current: p === page }" @click="typeof p === 'number' && (page = p)">{{ p }}</span>
        <button class="erp-page-btn" :disabled="page >= totalPages" @click="page++">›</button>
        <button class="erp-page-btn" :disabled="page >= totalPages" @click="page = totalPages">末页</button>
      </div>
      <label class="erp-page-size-label">
        每页
        <select v-model.number="pageSize" class="erp-page-size-select">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        条
      </label>
    </div>

    <!-- ===== 物料表单弹窗 ===== -->
    <Teleport to="body">
      <div v-if="showForm" class="erp-modal-overlay" @click.self="closeForm">
        <div class="erp-modal-panel">
          <div class="erp-modal-header">
            <h3>{{ isEditing ? '编辑物料' : '新建物料' }}</h3>
            <button class="erp-modal-close" @click="closeForm">✕</button>
          </div>
          <div class="erp-modal-body">
            <!-- 编号方式（仅新建时可选） -->
            <div v-if="!isEditing" class="erp-numbering-row">
              <label class="erp-radio-label">
                <input type="radio" v-model="numberingMode" value="auto" />
                <span>自动编号</span>
              </label>
              <label class="erp-radio-label">
                <input type="radio" v-model="numberingMode" value="manual" />
                <span>手动编号</span>
              </label>
            </div>

            <div class="erp-form-grid">
              <div class="erp-form-group">
                <label>物料编码 <span class="erp-form-req">*</span></label>
                <input
                  v-model="form.code"
                  type="text"
                  :disabled="!isEditing && numberingMode === 'auto'"
                  :placeholder="!isEditing && numberingMode === 'auto' ? '系统自动生成' : '请输入物料编码'"
                />
              </div>
              <div class="erp-form-group">
                <label>物料名称 <span class="erp-form-req">*</span></label>
                <input v-model="form.name" type="text" placeholder="请输入物料名称" />
              </div>
              <div class="erp-form-group">
                <label>物料类别 <span class="erp-form-req">*</span></label>
                <select v-model="form.category">
                  <option value="">请选择</option>
                  <option v-for="cat in materialCategories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
              </div>
              <div class="erp-form-group">
                <label>规格型号</label>
                <input v-model="form.spec" type="text" placeholder="如 Φ20×100" />
              </div>
              <div class="erp-form-group">
                <label>计量单位 <span class="erp-form-req">*</span></label>
                <select v-model="form.unit">
                  <option value="">请选择</option>
                  <option v-for="u in units" :key="u" :value="u">{{ u }}</option>
                </select>
              </div>
              <div class="erp-form-group">
                <label>参考单价 (元)</label>
                <input v-model.number="form.price" type="number" step="0.01" min="0" placeholder="0.00" />
              </div>
              <div class="erp-form-group">
                <label>安全库存</label>
                <input v-model.number="form.safetyStock" type="number" min="-1" placeholder="-1" />
              </div>
              <div class="erp-form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option value="启用">启用</option>
                  <option value="停用">停用</option>
                </select>
              </div>
              <div class="erp-form-group full">
                <label>备注</label>
                <textarea v-model="form.remark" rows="2" placeholder="可选备注信息"></textarea>
              </div>
            </div>
          </div>
          <div class="erp-modal-footer">
            <button class="erp-btn btn-cancel" @click="closeForm">取消</button>
            <button class="erp-btn erp-btn-primary" @click="save">保存</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ===== 删除确认弹窗 ===== -->
    <Teleport to="body">
      <div v-if="showDelete" class="erp-modal-overlay" @click.self="showDelete = false">
        <div class="erp-modal-panel confirm-panel">
          <div class="erp-modal-header"><h3>确认删除</h3></div>
          <div class="erp-modal-body">
            <p>确定要删除物料 <strong>{{ deleteTarget?.code }} - {{ deleteTarget?.name }}</strong> 吗？</p>
            <p class="warn-text">此操作不可恢复。</p>
          </div>
          <div class="erp-modal-footer">
            <button class="erp-btn btn-cancel" @click="showDelete = false">取消</button>
            <button class="erp-btn btn-danger" @click="doDelete">确认删除</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
// ========== 常量 ==========
const materialCategories = ['原材料', '半成品', '辅料', '包装物', '低值易耗品']
const units = ['个', '件', '只', '套', 'kg', 'g', 'm', 'cm', 'L', 'ml', '箱', '卷', '张', '片', '条']
const PAGE_SIZE_OPTIONS = [10, 20, 50]

// ========== 物料数据 ==========
const materials = ref([
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

// ========== 搜索 / 筛选 / 排序 ==========
const search = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const sortField = ref('code')
const sortAsc = ref(true)

function sortBy(field: string) {
  if (sortField.value === field) { sortAsc.value = !sortAsc.value }
  else { sortField.value = field; sortAsc.value = true }
}

const filteredData = computed(() => {
  let list = [...materials.value]
  const q = search.value.trim().toLowerCase()
  if (q) list = list.filter(m => m.code.toLowerCase().includes(q) || m.name.toLowerCase().includes(q) || m.spec.toLowerCase().includes(q))
  if (filterCategory.value) list = list.filter(m => m.category === filterCategory.value)
  if (filterStatus.value) list = list.filter(m => m.status === filterStatus.value)

  list.sort((a, b) => {
    const aVal = a[sortField.value as keyof typeof a]
    const bVal = b[sortField.value as keyof typeof b]
    if (typeof aVal === 'number' && typeof bVal === 'number') {
      return sortAsc.value ? aVal - bVal : bVal - aVal
    }
    return sortAsc.value ? String(aVal).localeCompare(String(bVal)) : String(bVal).localeCompare(String(aVal))
  })
  return list
})

// ========== 分页 ==========
const page = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.max(1, Math.ceil(filteredData.value.length / pageSize.value)))
const pagedData = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const cur = page.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  if (cur <= 4) return [1, 2, 3, 4, 5, '...', total]
  if (cur >= total - 3) return [1, '...', total - 4, total - 3, total - 2, total - 1, total]
  return [1, '...', cur - 1, cur, cur + 1, '...', total]
})

// 筛选条件变化时重置到第一页
watch([search, filterCategory, filterStatus], () => { page.value = 1 })

// ========== 表单 ==========
const showForm = ref(false)
const isEditing = ref(false)
const numberingMode = ref('auto')
const form = reactive({ code: '', name: '', category: '', spec: '', unit: '', price: 0, safetyStock: 0, status: '启用', remark: '' })
let editingCode = ''

function resetForm() {
  form.code = ''; form.name = ''; form.category = ''; form.spec = ''
  form.unit = ''; form.price = 0; form.safetyStock = 0
  form.status = '启用'; form.remark = ''
  editingCode = ''; numberingMode.value = 'auto'
}

function openForm(item?: typeof materials.value[0]) {
  if (item) {
    isEditing.value = true; editingCode = item.code
    Object.assign(form, { ...item })
  } else {
    isEditing.value = false; resetForm()
    form.code = `MAT-${String(materials.value.length + 1).padStart(3, '0')}`
  }
  showForm.value = true
}

function closeForm() { showForm.value = false }

function save() {
  if (!form.name || !form.category || !form.unit) { alert('请填写物料名称、类别和计量单位'); return }
  if (isEditing.value) {
    const idx = materials.value.findIndex(m => m.code === editingCode)
    if (idx !== -1) materials.value[idx] = { ...form } as typeof materials.value[0]
  } else {
    if (numberingMode.value === 'auto') {
      // 自动编号时使用表单当前显示的编码
    }
    materials.value.push({ ...form } as typeof materials.value[0])
  }
  closeForm()
}

// ========== 删除 ==========
const showDelete = ref(false)
const deleteTarget = ref<typeof materials.value[0] | null>(null)

function confirmDelete(item: typeof materials.value[0]) {
  deleteTarget.value = item; showDelete.value = true
}

function doDelete() {
  if (deleteTarget.value) {
    materials.value = materials.value.filter(m => m.code !== deleteTarget.value!.code)
  }
  showDelete.value = false; deleteTarget.value = null
}
</script>

<style scoped>
/* === 组件特有样式 === */
.material-tab { display: flex; flex-direction: column; flex: 1; }

/* 特有按钮 */
.btn-cancel { background: #f5f5f5; color: #666; }
.btn-cancel:hover { background: #eee; }
.btn-danger { background: #d32f2f; color: #fff; }
.btn-danger:hover { background: #b71c1c; }

/* 表格最后一行去底边框 */
.erp-table tbody tr:last-child td { border-bottom: none; }

/* 低库存高亮 */
.erp-cell-num.low-stock { color: #d32f2f; font-weight: 600; }

/* 确认弹窗宽度 */
.confirm-panel { width: 420px; }

/* 删除警告文字 */
.warn-text { color: #d32f2f; font-size: 13px; margin-top: 8px; }
</style>
