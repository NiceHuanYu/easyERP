<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="header-left">
        <h1 class="app-title">easyERP</h1>
        <span class="app-subtitle">企业管理后台</span>
      </div>
      <div class="header-right">
        <span class="user-info">
          <span class="user-avatar">{{ user?.name?.charAt(0) }}</span>
          <span class="user-name">{{ user?.name }}</span>
          <span class="user-role">({{ user?.role === 'admin' ? '管理员' : '普通用户' }})</span>
        </span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="app-body">
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <template v-for="item in navItems" :key="item.path">
            <!-- 无子菜单：直接跳转 -->
            <NuxtLink
              v-if="!item.children"
              :to="item.disabled ? undefined : item.path"
              :class="['nav-item', { active: isActive(item.path), disabled: item.disabled }]"
              :title="item.disabled ? '开发中' : ''"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </NuxtLink>

            <!-- 有子菜单：点击展开/折叠 -->
            <div
              v-else
              :class="['nav-parent', { open: expandedMenu === item.path, active: isParentActive(item.path) }]"
            >
              <button class="nav-item nav-parent-btn" @click="toggleMenu(item.path)">
                <span class="nav-icon">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
                <span class="nav-arrow" :class="{ rotated: expandedMenu === item.path }">›</span>
              </button>

              <div v-show="expandedMenu === item.path" class="nav-children">
                <NuxtLink
                  v-for="child in item.children"
                  :key="child.key"
                  :to="`${item.path}?tab=${child.key}`"
                  :class="['nav-child', { active: isChildActive(item.path, child.key) }]"
                >
                  {{ child.label }}
                </NuxtLink>
              </div>
            </div>
          </template>
        </nav>
      </aside>

      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
const { user, logout } = useAuth()
const route = useRoute()

const navItems = [
  { path: '/dashboard', icon: '📊', label: '工作台', disabled: false },
  {
    path: '/basic-data', icon: '🗂️', label: '基础资料',
    children: [
      { key: 'material', label: '物料档案' },
      { key: 'product', label: '产品档案' },
      { key: 'bom', label: 'BOM管理' },
      { key: 'customer', label: '客户管理' },
      { key: 'supplier', label: '供应商管理' },
      { key: 'warehouse', label: '仓库管理' },
      { key: 'contact', label: '联络列表' },
      { key: 'unit', label: '计量单位' },
    ],
  },
  {
    path: '/sales', icon: '📦', label: '销售管理',
    children: [
      { key: 'quotation', label: '销售报价' },
      { key: 'order', label: '销售订单' },
      { key: 'shipment', label: '销售发货' },
      { key: 'return', label: '销售退货' },
      { key: 'tracking', label: '订单跟踪' },
    ],
  },
  {
    path: '/purchase', icon: '🛒', label: '采购管理',
    children: [
      { key: 'requisition', label: '采购申请' },
      { key: 'order', label: '采购订单' },
      { key: 'arrival', label: '采购到货' },
      { key: 'return', label: '采购退货' },
      { key: 'reconcile', label: '采购对账' },
      { key: 'payment', label: '采购付款' },
    ],
  },
  {
    path: '/production', icon: '🏭', label: '生产管理',
    children: [
      { key: 'plan', label: '生产计划' },
      { key: 'order', label: '生产工单' },
      { key: 'schedule', label: '生产排程' },
      { key: 'material', label: '生产领料' },
      { key: 'report', label: '生产报工' },
      { key: 'complete', label: '完工入库' },
    ],
  },
  {
    path: '/inventory', icon: '📋', label: '库存管理',
    children: [
      { key: 'overview', label: '库存概览' },
      { key: 'inbound', label: '入库管理' },
      { key: 'outbound', label: '出库管理' },
      { key: 'transfer', label: '库存调拨' },
      { key: 'count', label: '库存盘点' },
      { key: 'journal', label: '库存流水' },
    ],
  },
  {
    path: '/quality', icon: '✅', label: '品质管理',
    children: [
      { key: 'iqc', label: '来料检验' },
      { key: 'ipqc', label: '过程检验' },
      { key: 'oqc', label: '出货检验' },
      { key: 'nc', label: '不合格品' },
      { key: 'std', label: '检验标准' },
    ],
  },
  {
    path: '/finance', icon: '💰', label: '财务管理',
    children: [
      { key: 'ar', label: '应收管理' },
      { key: 'ap', label: '应付管理' },
      { key: 'invoice', label: '发票管理' },
      { key: 'payment', label: '收付款' },
      { key: 'cost', label: '成本核算' },
    ],
  },
  {
    path: '/organization', icon: '👥', label: '组织管理',
    children: [
      { key: 'user', label: '用户管理' },
      { key: 'role', label: '角色管理' },
      { key: 'perm', label: '权限管理' },
      { key: 'audit', label: '审计日志' },
    ],
  },
  {
    path: '/analytics', icon: '📈', label: '数据分析',
    children: [
      { key: 'dashboard', label: '综合看板' },
      { key: 'sales', label: '销售分析' },
      { key: 'purchase', label: '采购分析' },
      { key: 'production', label: '生产分析' },
      { key: 'inventory', label: '库存分析' },
      { key: 'quality', label: '品质分析' },
    ],
  },
  { path: '/settings', icon: '⚙️', label: '系统设置', disabled: true },
]

// 当前展开的一级菜单
const expandedMenu = ref<string | null>(null)

// 根据当前路由自动展开对应菜单
watch(() => route.path, (path) => {
  const parent = navItems.find(item => item.children && path.startsWith(item.path))
  if (parent) {
    expandedMenu.value = parent.path
  }
}, { immediate: true })

function toggleMenu(path: string) {
  expandedMenu.value = expandedMenu.value === path ? null : path
}

function isActive(path: string) {
  return route.path === path
}

function isParentActive(path: string) {
  return route.path.startsWith(path)
}

function isChildActive(parentPath: string, childKey: string) {
  return route.path === parentPath && route.query.tab === childKey
}

function handleLogout() {
  logout()
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

/* Header */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.app-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a73e8;
  margin: 0;
}

.app-subtitle {
  font-size: 12px;
  color: #999;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1a73e8;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-name { font-weight: 500; }
.user-role { color: #999; font-size: 12px; }

.logout-btn {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  border-color: #d32f2f;
  color: #d32f2f;
}

/* Body */
.app-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  padding: 8px 0;
  flex-shrink: 0;
  overflow-y: auto;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
}

/* 通用导航项 */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  font-size: 14px;
  color: #555;
  text-decoration: none;
  transition: all 0.15s;
  border: none;
  border-left: 3px solid transparent;
  width: 100%;
  background: none;
  cursor: pointer;
  font-family: inherit;
}

.nav-item:hover { background: #f5f7fa; }

.nav-item.active {
  background: #e8f0fe;
  color: #1a73e8;
  font-weight: 500;
  border-left-color: #1a73e8;
}

.nav-item.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

/* 父级菜单 */
.nav-parent {
  display: flex;
  flex-direction: column;
}

.nav-parent.active > .nav-parent-btn {
  color: #1a73e8;
  font-weight: 500;
  border-left-color: #1a73e8;
}

.nav-parent-btn {
  padding-right: 12px;
}

.nav-arrow {
  margin-left: auto;
  font-size: 16px;
  transition: transform 0.2s;
  color: #bbb;
}

.nav-arrow.rotated {
  transform: rotate(90deg);
}

/* 子菜单 */
.nav-children {
  display: flex;
  flex-direction: column;
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;
}

.nav-child {
  display: block;
  padding: 8px 20px 8px 54px;
  font-size: 13px;
  color: #666;
  text-decoration: none;
  transition: all 0.15s;
  border: none;
  border-left: 3px solid transparent;
}

.nav-child:hover {
  background: #f0f4ff;
  color: #1a73e8;
}

.nav-child.active {
  background: #e8f0fe;
  color: #1a73e8;
  font-weight: 500;
  border-left-color: #1a73e8;
}

/* Main Content */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
