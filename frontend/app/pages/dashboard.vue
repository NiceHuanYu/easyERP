<template>
  <div>
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <h2>欢迎回来，{{ user?.name }}！</h2>
      <p>今天是 {{ today }}，以下是系统概览。</p>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bg, color: stat.color }">{{ stat.icon }}</div>
        <div class="stat-info">
          <div class="stat-value-row">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-unit">{{ stat.unit }}</span>
          </div>
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}%
          </span>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- 待处理任务 -->
      <section class="panel">
        <h3>⏳ 待处理任务</h3>
        <div class="task-list">
          <div class="task-item" v-for="task in pendingTasks" :key="task.id">
            <span class="task-priority" :class="task.priority">{{ task.priorityLabel }}</span>
            <span class="task-desc">{{ task.desc }}</span>
            <span class="task-dept">{{ task.dept }}</span>
          </div>
          <div v-if="pendingTasks.length === 0" class="empty-hint">暂无待处理任务</div>
        </div>
      </section>

      <!-- 本月趋势 -->
      <section class="panel">
        <h3>📊 本月经营概况</h3>
        <div class="monthly-grid">
          <div class="monthly-item" v-for="item in monthlySummary" :key="item.label">
            <span class="monthly-label">{{ item.label }}</span>
            <span class="monthly-value">{{ item.value }}</span>
            <span class="monthly-compare" :class="item.up ? 'up' : 'down'">
              较上月 {{ item.up ? '↑' : '↓' }} {{ item.percent }}
            </span>
          </div>
        </div>
      </section>
    </div>

    <!-- 最近订单动态 -->
    <div class="recent-section">
      <section class="panel full-width">
        <div class="panel-header">
          <h3>📋 最近订单动态</h3>
        </div>
        <table class="recent-table">
          <thead>
            <tr>
              <th>单号</th>
              <th>类型</th>
              <th>客户/供应商</th>
              <th>金额</th>
              <th>状态</th>
              <th>日期</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in recentOrders" :key="order.id">
              <td class="order-id">{{ order.id }}</td>
              <td>{{ order.type }}</td>
              <td>{{ order.partner }}</td>
              <td class="order-amount">{{ order.amount }}</td>
              <td><span class="status-badge" :class="order.statusClass">{{ order.statusLabel }}</span></td>
              <td class="order-date">{{ order.date }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const { user, isLoggedIn } = useAuth()

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
})

// ========= 模拟数据（后续替换为真实 API） =========

const stats = [
  { icon: '📦', label: '待处理订单',      value: '8',  unit: '单', bg: '#e3f2fd', color: '#1565c0', trend: 12 },
  { icon: '⚠️', label: '库存预警物料',    value: '5',  unit: '项', bg: '#fce4ec', color: '#c62828', trend: -20 },
  { icon: '✅', label: '本月完工工单',    value: '23', unit: '单', bg: '#e8f5e9', color: '#2e7d32', trend: 8 },
  { icon: '📈', label: '进行中工单',      value: '12', unit: '单', bg: '#fff3e0', color: '#e65100', trend: 5 },
  { icon: '💰', label: '本月销售额',      value: '86.5', unit: '万', bg: '#f3e5f5', color: '#6a1b9a', trend: 15 },
  { icon: '📥', label: '待入库采购',      value: '6',  unit: '单', bg: '#e0f2f1', color: '#00695c', trend: -8 },
]

const pendingTasks = [
  { id: 1, desc: 'SO-2025-0089 销售订单待审核',     dept: '销售部', priority: 'high',   priorityLabel: '紧急' },
  { id: 2, desc: 'PO-2025-0056 采购订单待确认',     dept: '采购部', priority: 'high',   priorityLabel: '紧急' },
  { id: 3, desc: 'IQC-2025-0032 来料待检验',        dept: '品质部', priority: 'medium', priorityLabel: '重要' },
  { id: 4, desc: 'MO-2025-0045 生产工单待领料',     dept: '生产部', priority: 'medium', priorityLabel: '重要' },
  { id: 5, desc: 'INV-2025-0003 库存盘点差异待处理', dept: '仓库',   priority: 'low',    priorityLabel: '普通' },
]

const monthlySummary = [
  { label: '销售订单总额', value: '¥86.5 万', up: true,  percent: '15%' },
  { label: '采购订单总额', value: '¥52.3 万', up: false, percent: '8%' },
  { label: '生产完工产值', value: '¥71.2 万', up: true,  percent: '10%' },
  { label: '库存周转率',   value: '3.2 次',   up: true,  percent: '6%' },
]

const recentOrders = [
  { id: 'SO-2025-0089', type: '销售订单', partner: '华强电子',      amount: '¥28,500', statusClass: 'pending',  statusLabel: '待审核',  date: '07-15' },
  { id: 'PO-2025-0056', type: '采购订单', partner: '宏远钢材',      amount: '¥15,200', statusClass: 'confirming', statusLabel: '待确认',  date: '07-15' },
  { id: 'SO-2025-0088', type: '销售订单', partner: '精密模具',      amount: '¥42,000', statusClass: 'progress',  statusLabel: '生产中',  date: '07-14' },
  { id: 'MO-2025-0045', type: '生产工单', partner: '装配车间',      amount: '-',       statusClass: 'ready',     statusLabel: '待领料',  date: '07-14' },
  { id: 'PO-2025-0055', type: '采购订单', partner: '宏远钢材',      amount: '¥8,600',  statusClass: 'done',      statusLabel: '已到货',  date: '07-13' },
  { id: 'SO-2025-0087', type: '销售订单', partner: '鑫达科技',      amount: '¥35,000', statusClass: 'shipped',   statusLabel: '已发货',  date: '07-12' },
  { id: 'SO-2025-0086', type: '销售订单', partner: '华强电子',      amount: '¥12,300', statusClass: 'completed', statusLabel: '已完成',  date: '07-11' },
]
</script>

<style scoped>
/* ========== 欢迎横幅 ========== */
.welcome-banner {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  color: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 24px;
}
.welcome-banner h2 { margin: 0 0 4px 0; font-size: 22px; }
.welcome-banner p { margin: 0; opacity: 0.85; font-size: 14px; }

/* ========== 指标卡片 ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  transition: box-shadow .2s;
}
.stat-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,.1); }
.stat-icon {
  width: 46px; height: 46px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; flex-shrink: 0;
}
.stat-info { display: flex; flex-direction: column; min-width: 0; }
.stat-value-row { display: flex; align-items: baseline; gap: 4px; }
.stat-value { font-size: 26px; font-weight: 700; color: #333; line-height: 1.2; }
.stat-unit { font-size: 12px; color: #999; }
.stat-label { font-size: 12px; color: #888; margin-top: 1px; }
.stat-trend { font-size: 11px; margin-top: 2px; }
.stat-trend.up { color: #2e7d32; }
.stat-trend.down { color: #c62828; }

/* ========== 双栏内容 ========== */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
@media (max-width: 900px) { .content-grid { grid-template-columns: 1fr; } }

.panel {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.panel h3 { margin: 0 0 14px 0; font-size: 15px; color: #333; }

/* ========== 待处理任务 ========== */
.task-list { display: flex; flex-direction: column; gap: 8px; }
.task-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #fafafa;
  font-size: 13px;
}
.task-item:hover { background: #f0f4ff; }
.task-priority {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}
.task-priority.high { background: #fce4ec; color: #c62828; }
.task-priority.medium { background: #fff3e0; color: #e65100; }
.task-priority.low { background: #e8f5e9; color: #2e7d32; }
.task-desc { flex: 1; color: #333; }
.task-dept { color: #999; font-size: 12px; }
.empty-hint { text-align: center; color: #bbb; padding: 20px 0; font-size: 13px; }

/* ========== 本月经营概况 ========== */
.monthly-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.monthly-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.monthly-label { font-size: 12px; color: #888; }
.monthly-value { font-size: 18px; font-weight: 700; color: #333; }
.monthly-compare { font-size: 11px; }
.monthly-compare.up { color: #2e7d32; }
.monthly-compare.down { color: #c62828; }

/* ========== 最近订单动态 ========== */
.recent-section { margin-top: 0; }
.panel.full-width { grid-column: 1 / -1; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.panel-header h3 { margin: 0; }

.recent-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.recent-table th {
  text-align: left;
  padding: 10px 12px;
  color: #888;
  font-weight: 500;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
}
.recent-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f8f8f8;
  color: #333;
}
.recent-table tbody tr:hover { background: #f8faff; }
.order-id { font-family: 'SFMono', monospace; font-size: 12px; color: #1a73e8; }
.order-amount { font-family: 'SFMono', monospace; font-weight: 500; }
.order-date { color: #999; }

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}
.status-badge.pending { background: #fce4ec; color: #c62828; }
.status-badge.confirming { background: #fff3e0; color: #e65100; }
.status-badge.progress { background: #e3f2fd; color: #1565c0; }
.status-badge.ready { background: #fff8e1; color: #f9a825; }
.status-badge.done { background: #e8f5e9; color: #2e7d32; }
.status-badge.shipped { background: #e0f2f1; color: #00695c; }
.status-badge.completed { background: #f3e5f5; color: #6a1b9a; }
</style>
