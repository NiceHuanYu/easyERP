<template>
  <el-container class="app-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="app-sidebar">
      <div class="sidebar-logo" @click="navigateTo('/')">
        <span v-if="!isCollapsed" class="logo-text">EasyERP</span>
        <span v-else class="logo-text--collapsed">E</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        :router="true"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-sub-menu index="/base-data">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>基础数据</span>
          </template>
          <el-menu-item index="/base-data/materials">物料管理</el-menu-item>
          <el-menu-item index="/base-data/boms">BOM 管理</el-menu-item>
          <el-menu-item index="/base-data/customers">客户管理</el-menu-item>
          <el-menu-item index="/base-data/suppliers">供应商管理</el-menu-item>
          <el-menu-item index="/base-data/warehouses">仓库管理</el-menu-item>
          <el-menu-item index="/base-data/employees">员工管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/sales">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>销售管理</span>
          </template>
          <el-menu-item index="/sales/orders">销售订单</el-menu-item>
          <el-menu-item index="/sales/deliveries">销售发货</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/production">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>生产管理</span>
          </template>
          <el-menu-item index="/production/orders">生产工单</el-menu-item>
          <el-menu-item index="/production/pickings">生产领料</el-menu-item>
          <el-menu-item index="/production/finishings">生产完工</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/purchase">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>采购管理</span>
          </template>
          <el-menu-item index="/purchase/requisitions">采购申请</el-menu-item>
          <el-menu-item index="/purchase/orders">采购订单</el-menu-item>
          <el-menu-item index="/purchase/receivings">采购收货</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/inventory">
          <template #title>
            <el-icon><List /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/inventory/stock">库存查询</el-menu-item>
          <el-menu-item index="/inventory/transactions">库存流水</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/finance">
          <template #title>
            <el-icon><Coin /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/receivables">应收账款</el-menu-item>
          <el-menu-item index="/finance/payables">应付账款</el-menu-item>
          <el-menu-item index="/finance/payments">收付款管理</el-menu-item>
          <el-menu-item index="/finance/bank-accounts">银行账户</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">用户管理</el-menu-item>
          <el-menu-item index="/system/roles">角色管理</el-menu-item>
          <el-menu-item index="/system/dicts">字典管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- Main Area -->
    <el-container class="app-main">
      <!-- Header -->
      <el-header height="50px" class="app-header">
        <div class="header-left">
          <el-button
            :icon="isCollapsed ? Expand : Fold"
            text
            @click="isCollapsed = !isCollapsed"
          />
          <el-breadcrumb separator="/" class="header-breadcrumb">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="28" icon="UserFilled" />
              <span class="user-name">{{ authStore.userInfo?.name || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><Switch /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="app-content">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {
  Monitor,
  Box,
  ShoppingCart,
  DataAnalysis,
  Tickets,
  List,
  Coin,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  Switch,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const isCollapsed = ref(false)

interface BreadcrumbItem {
  title: string
  path: string
}

const menuTitleMap: Record<string, string> = {
  '/dashboard': '工作台',
  '/base-data/materials': '物料管理',
  '/base-data/boms': 'BOM 管理',
  '/base-data/customers': '客户管理',
  '/base-data/suppliers': '供应商管理',
  '/base-data/warehouses': '仓库管理',
  '/base-data/employees': '员工管理',
  '/base-data/company-accounts': '公司账户',
  '/sales/orders': '销售订单',
  '/sales/deliveries': '销售发货',
  '/production/orders': '生产工单',
  '/production/pickings': '生产领料',
  '/production/finishings': '生产完工',
  '/purchase/requisitions': '采购申请',
  '/purchase/orders': '采购订单',
  '/purchase/receivings': '采购收货',
  '/inventory/stock': '库存查询',
  '/inventory/transactions': '库存流水',
  '/finance/receivables': '应收账款',
  '/finance/payables': '应付账款',
  '/finance/payments': '收付款管理',
  '/base-data/company-accounts': '银行账户',
  '/finance/bank-accounts': '银行账户',
  '/system/users': '用户管理',
  '/system/roles': '角色管理',
  '/system/dicts': '字典管理',
}

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const path = route.path
  const parts = path.split('/').filter(Boolean)
  const crumbs: BreadcrumbItem[] = [{ title: '首页', path: '/dashboard' }]

  if (parts.length >= 2) {
    const fullPath = '/' + parts.slice(0, 2).join('/')
    const title = menuTitleMap[fullPath]
    if (title) {
      crumbs.push({ title, path: fullPath })
    }
  }

  // 三级路径：追加"详情"或"编辑"等
  if (parts.length >= 3) {
    const parentPath = '/' + parts.slice(0, 2).join('/')
    if (menuTitleMap[parentPath]) {
      crumbs.push({ title: '详情', path: route.path })
    }
  }

  return crumbs
})

async function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.app-sidebar {
  background-color: #304156;
  transition: width 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  letter-spacing: 1px;
}

.logo-text--collapsed {
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.app-main {
  flex-direction: column;
  overflow: hidden;
}

.app-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-breadcrumb {
  margin-left: 4px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-content {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 16px;
}
</style>
