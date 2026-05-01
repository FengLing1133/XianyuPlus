<template>
  <div class="layout">
    <nav class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">🎓</span>
          <span class="logo-text">闲鱼Plus</span>
        </router-link>

        <div class="nav-links">
          <router-link to="/" class="nav-link">🏠 首页</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/publish" class="nav-link">📦 发布商品</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/profile" class="nav-link">👤 个人中心</router-link>
        </div>

        <div class="nav-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/chat" class="chat-badge" title="消息">
              <span class="chat-icon">💬</span>
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </router-link>

            <NotificationBell v-if="userStore.token" />

            <div class="user-menu" @click="showDropdown = !showDropdown" v-click-outside="() => showDropdown = false">
              <img :src="userStore.userInfo?.avatar || ''" class="user-avatar" @error="e => e.target.style.display='none'" />
              <span v-if="!userStore.userInfo?.avatar" class="avatar-placeholder">👤</span>
              <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
              <span class="arrow">▾</span>

              <div v-if="showDropdown" class="dropdown">
                <router-link to="/profile" class="dropdown-item" @click="showDropdown = false">👤 个人中心</router-link>
                <router-link to="/favorites" class="dropdown-item" @click="showDropdown = false">⭐ 我的收藏</router-link>
                <router-link to="/orders" class="dropdown-item" @click="showDropdown = false">📋 我的订单</router-link>
                <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="showDropdown = false">⚙️ 管理后台</router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item" @click="handleLogout">🚪 退出登录</button>
              </div>
            </div>
          </template>
          <template v-else>
            <button class="btn-pill btn-pill-outline" @click="$router.push('/login')">登录</button>
            <button class="btn-pill btn-pill-primary" @click="$router.push('/register')" style="margin-left: 10px;">注册</button>
          </template>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import NotificationBell from './NotificationBell.vue'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const showDropdown = ref(false)
let timer = null

function handleLogout() {
  userStore.logout()
  showDropdown.value = false
  router.push('/')
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await request.get('/message/unread', { silent: true })
    unreadCount.value = res.data.count
  } catch {}
}

const vClickOutside = {
  mounted(el, binding) {
    el.__clickOutside = (e) => {
      if (!el.contains(e.target)) binding.value()
    }
    document.addEventListener('click', el.__clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el.__clickOutside)
  }
}

onMounted(() => {
  fetchUnread()
  timer = setInterval(fetchUnread, 10000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: var(--shadow-nav);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--green-500);
}
.logo-icon { font-size: 24px; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  padding: 6px 16px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  transition: all var(--transition-fast);
}
.nav-link:hover,
.nav-link.router-link-exact-active {
  background: var(--green-50);
  color: var(--green-500);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-badge {
  position: relative;
  font-size: 20px;
  display: flex;
  align-items: center;
}
.badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--price-red);
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  font-weight: 600;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  position: relative;
  padding: 4px 8px;
  border-radius: var(--radius-pill);
}
.user-menu:hover {
  background: var(--bg-page);
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--green-50);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.user-name {
  font-size: 14px;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow {
  font-size: 10px;
  color: var(--text-secondary);
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-hover);
  min-width: 180px;
  padding: 8px;
  z-index: 200;
}
.dropdown-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  background: none;
  transition: background var(--transition-fast);
}
.dropdown-item:hover {
  background: var(--bg-page);
}
.dropdown-divider {
  height: 1px;
  background: var(--border-lighter);
  margin: 4px 0;
}

.main-content {
  min-height: calc(100vh - 60px);
  padding: 20px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
