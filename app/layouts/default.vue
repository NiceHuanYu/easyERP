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
          <NuxtLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.disabled ? undefined : item.path"
            :class="['nav-item', { active: isActive(item.path), disabled: item.disabled }]"
            :title="item.disabled ? '开发中' : ''"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </NuxtLink>
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
  { path: '/basic-data', icon: '🗂️', label: '基础资料', disabled: false },
  { path: '/sales', icon: '📦', label: '销售管理', disabled: true },
  { path: '/purchase', icon: '🛒', label: '采购管理', disabled: true },
  { path: '/production', icon: '🏭', label: '生产管理', disabled: true },
  { path: '/inventory', icon: '📋', label: '库存管理', disabled: true },
  { path: '/quality', icon: '✅', label: '品质管理', disabled: true },
  { path: '/finance', icon: '💰', label: '财务管理', disabled: true },
  { path: '/organization', icon: '👥', label: '组织管理', disabled: true },
  { path: '/analytics', icon: '📈', label: '数据分析', disabled: true },
  { path: '/settings', icon: '⚙️', label: '系统设置', disabled: true },
]

function isActive(path: string) {
  return route.path === path
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

.user-name {
  font-weight: 500;
}

.user-role {
  color: #999;
  font-size: 12px;
}

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
  width: 200px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  padding: 16px 0;
  flex-shrink: 0;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  font-size: 14px;
  color: #555;
  text-decoration: none;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: #f5f7fa;
}

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
}

/* Main Content */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
