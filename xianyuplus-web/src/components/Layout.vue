<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left">
        <router-link to="/" class="logo">闲鱼Plus</router-link>
        <el-menu mode="horizontal" :default-active="$route.path" router class="header-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/publish" v-if="userStore.isLoggedIn">发布商品</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-icon">
            <el-button link @click="$router.push('/chat')">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="myProducts">我的发布</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button link @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let timer = null

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else if (cmd === 'myProducts') {
    router.push('/profile')
  } else {
    router.push('/' + cmd)
  }
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await request.get('/message/unread', { silent: true })
    unreadCount.value = res.data.count
  } catch {}
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
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0 40px;
}
.header-left { display: flex; align-items: center; }
.logo { font-size: 20px; font-weight: bold; color: #409eff; text-decoration: none; margin-right: 30px; }
.header-menu { border-bottom: none !important; }
.header-right { display: flex; align-items: center; gap: 15px; }
.header-icon { margin-right: 5px; }
.user-dropdown { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; }
.main { min-height: calc(100vh - 60px); padding: 20px 40px; max-width: 1200px; margin: 0 auto; }
</style>
