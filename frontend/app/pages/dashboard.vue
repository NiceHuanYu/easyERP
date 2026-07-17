<template>
  <div>
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
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const { user } = useAuth()

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
})
</script>

<style scoped>
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
