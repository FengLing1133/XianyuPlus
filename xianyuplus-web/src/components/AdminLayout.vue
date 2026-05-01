<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin" class="admin-logo">
        <span class="logo-text">闲鱼管理</span>
      </router-link>

      <nav class="admin-nav">
        <router-link to="/admin" class="admin-nav-item">
          仪表盘
        </router-link>
        <router-link to="/admin/users" class="admin-nav-item">
          用户管理
        </router-link>
        <router-link to="/admin/products" class="admin-nav-item">
          商品管理
        </router-link>
        <router-link to="/admin/orders" class="admin-nav-item">
          订单管理
        </router-link>
        <router-link to="/admin/reports" class="admin-nav-item">
          举报管理
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <router-link to="/" class="back-link">
          返回前台
        </router-link>
      </div>
    </aside>

    <div class="admin-right">
      <header class="admin-header">
        <span class="admin-title">管理后台</span>
        <span class="admin-user">当前管理员：{{ userStore.userInfo?.nickname }}</span>
      </header>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}

.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid var(--border-lighter);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-primary);
  border-bottom: 1px solid var(--border-lighter);
}
.logo-text { color: var(--text-primary); }

.admin-nav {
  padding: 12px 8px;
  flex: 1;
}

.admin-nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 2px;
  transition: all var(--transition-fast);
}
.admin-nav-item:hover {
  background: var(--bg-page);
}
.admin-nav-item.router-link-exact-active,
.admin-nav-item.router-link-active {
  background: var(--color-background);
  color: var(--color-primary);
  font-weight: 500;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid var(--border-lighter);
}

.back-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}
.back-link:hover {
  background: var(--bg-page);
  color: var(--text-primary);
}

.admin-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.admin-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.admin-user {
  font-size: 13px;
  color: var(--text-secondary);
}

.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-page);
}
</style>
