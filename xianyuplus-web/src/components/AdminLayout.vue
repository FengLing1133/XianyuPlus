<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-logo">闲鱼Plus 管理</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Ticket /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button link @click="$router.push('/')" style="color: #bfcbd9;">
          <el-icon><Back /></el-icon> 返回前台
        </el-button>
      </div>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span class="admin-title">管理后台</span>
        <span>当前管理员：{{ userStore.userInfo?.nickname }}</span>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<style scoped>
.admin-layout { height: 100vh; }
.sidebar { background-color: #304156; display: flex; flex-direction: column; overflow-y: auto; }
.sidebar-logo { color: #fff; font-size: 18px; font-weight: bold; text-align: center; padding: 20px 0; border-bottom: 1px solid rgba(255,255,255,0.1); }
.sidebar-footer { margin-top: auto; padding: 15px; text-align: center; border-top: 1px solid rgba(255,255,255,0.1); }
.admin-header { background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.08); padding: 0 20px; }
.admin-title { font-size: 16px; font-weight: 500; }
.admin-main { background: #f0f2f5; padding: 20px; }
</style>
