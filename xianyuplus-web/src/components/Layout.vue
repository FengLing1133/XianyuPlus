<template>
  <div class="layout">
    <nav class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">
          <div class="logo-icon-wrapper">
            <GraduationCap :size="18" color="white" />
          </div>
          <span class="logo-text">闲鱼Plus</span>
        </router-link>

        <div class="nav-links">
          <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
            <Home :size="16" />
            <span>首页</span>
          </router-link>
          <router-link v-if="userStore.isLoggedIn" to="/publish" class="nav-link" :class="{ active: $route.path === '/publish' }">
            <Package :size="16" />
            <span>发布商品</span>
          </router-link>
          <router-link v-if="userStore.isLoggedIn" to="/profile" class="nav-link" :class="{ active: $route.path === '/profile' }">
            <User :size="16" />
            <span>个人中心</span>
          </router-link>
        </div>

        <div class="nav-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/chat" class="chat-badge" title="消息">
              <MessageCircle :size="22" />
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </router-link>

            <NotificationBell v-if="userStore.token" />

            <div class="user-menu" @click="showDropdown = !showDropdown" v-click-outside="() => showDropdown = false">
              <img :src="userStore.userInfo?.avatar || ''" class="user-avatar" @error="e => e.target.style.display='none'" />
              <span v-if="!userStore.userInfo?.avatar" class="avatar-placeholder">
                <User :size="16" />
              </span>
              <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
              <ChevronDown :size="14" class="arrow-icon" />

              <div v-if="showDropdown" class="dropdown">
                <router-link to="/profile" class="dropdown-item" @click="showDropdown = false">
                  <User :size="16" /> 个人中心
                </router-link>
                <router-link to="/favorites" class="dropdown-item" @click="showDropdown = false">
                  <Star :size="16" /> 我的收藏
                </router-link>
                <router-link to="/history" class="dropdown-item" @click="showDropdown = false">
                  <Clock :size="16" /> 浏览历史
                </router-link>
                <router-link to="/orders" class="dropdown-item" @click="showDropdown = false">
                  <ClipboardList :size="16" /> 我的订单
                </router-link>
                <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="showDropdown = false">
                  <Settings :size="16" /> 管理后台
                </router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item" @click="handleLogout">
                  <LogOut :size="16" /> 退出登录
                </button>
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
import {
  GraduationCap, Home, Package, User, MessageCircle,
  ChevronDown, Star, Clock, ClipboardList, Settings, LogOut
} from 'lucide-vue-next'

const router = useRouter()
const userStore = useUserStore()
const showDropdown = ref(false)
const unreadCount = ref(0)
let timer = null

async function fetchUnread() {
  if (!userStore.token) return
  try {
    const res = await request.get('/message/unread', { silent: true })
    unreadCount.value = res.data.count || 0
  } catch {}
}

function handleLogout() {
  userStore.logout()
  showDropdown.value = false
  router.push('/login')
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
  border-bottom: 2px solid var(--color-border);
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
  font-size: 17px;
  font-weight: 700;
  color: var(--color-foreground);
}

.logo-icon-wrapper {
  width: 34px;
  height: 34px;
  background: var(--gradient-logo);
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 18px;
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border: 1.5px solid var(--border-light);
  background: #fff;
  transition: all var(--transition-fast);
}

.nav-link:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.nav-link.active,
.nav-link.router-link-exact-active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-badge {
  position: relative;
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.chat-badge:hover {
  color: var(--color-primary);
}

.badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--color-primary);
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
  padding: 5px 10px;
  border-radius: var(--radius-pill);
  background: #F9FAFB;
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
}

.user-menu:hover {
  border-color: var(--color-border);
}

.user-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-name {
  font-size: 13px;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.arrow-icon {
  color: var(--text-muted);
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
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
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
