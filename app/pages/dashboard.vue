<template>
  <div class="dashboard">
    <header class="dashboard-header">
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

    <div class="dashboard-body">
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <a class="nav-item active" href="/dashboard">
            <span class="nav-icon">📊</span>
            <span>工作台</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">🗂️</span>
            <span>基础资料</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">📦</span>
            <span>销售管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">🛒</span>
            <span>采购管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">🏭</span>
            <span>生产管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">📋</span>
            <span>库存管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">✅</span>
            <span>品质管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">💰</span>
            <span>财务管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">👥</span>
            <span>组织管理</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">📈</span>
            <span>数据分析</span>
          </a>
          <a class="nav-item disabled" href="#" title="开发中">
            <span class="nav-icon">⚙️</span>
            <span>系统设置</span>
          </a>
        </nav>
      </aside>

      <main class="main-content">
        <div class="welcome-banner">
          <h2>欢迎回来，{{ user?.name }}！</h2>
          <p>今天是 {{ today }}，以下是系统概览。</p>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e3f2fd; color: #1565c0;">📦</div>
            <div class="stat-info">
              <span class="stat-value">0</span>
              <span class="stat-label">待处理订单</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #fce4ec; color: #c62828;">⚠️</div>
            <div class="stat-info">
              <span class="stat-value">0</span>
              <span class="stat-label">库存预警</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #e8f5e9; color: #2e7d32;">✅</div>
            <div class="stat-info">
              <span class="stat-value">0</span>
              <span class="stat-label">本月完工</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #fff3e0; color: #e65100;">📈</div>
            <div class="stat-info">
              <span class="stat-value">0</span>
              <span class="stat-label">进行中工单</span>
            </div>
          </div>
        </div>

        <div class="content-grid">
          <section class="panel">
            <h3>📌 快捷操作</h3>
            <div class="quick-actions">
              <button class="action-btn" disabled>新建销售订单</button>
              <button class="action-btn" disabled>新增采购申请</button>
              <button class="action-btn" disabled>创建生产工单</button>
              <button class="action-btn" disabled>库存盘点</button>
            </div>
            <p class="dev-hint">功能开发中，敬请期待</p>
          </section>

          <section class="panel">
            <h3>📰 系统信息</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">系统版本</span>
                <span class="info-value">v0.1.0 (开发版)</span>
              </div>
              <div class="info-item">
                <span class="info-label">技术栈</span>
                <span class="info-value">Nuxt 4 + Vue 3</span>
              </div>
              <div class="info-item">
                <span class="info-label">登录账户</span>
                <span class="info-value">{{ user?.username }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">用户角色</span>
                <span class="info-value">{{ user?.role === 'admin' ? '管理员' : '普通用户' }}</span>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const { user, logout } = useAuth()

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
})

function handleLogout() {
  logout()
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
}

/* Header */
.dashboard-header {
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
.dashboard-body {
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

.welcome-banner {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  color: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 24px;
}

.welcome-banner h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
}

.welcome-banner p {
  margin: 0;
  opacity: 0.85;
  font-size: 14px;
}

/* Stats */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #888;
}

/* Content Panels */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.panel {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.panel h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.action-btn {
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fafafa;
  color: #555;
  font-size: 13px;
  cursor: not-allowed;
  transition: background 0.15s;
}

.action-btn:hover {
  background: #f0f0f0;
}

.dev-hint {
  margin: 12px 0 0 0;
  font-size: 12px;
  color: #bbb;
  text-align: center;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.info-label {
  color: #888;
}

.info-value {
  color: #333;
  font-weight: 500;
}
</style>
