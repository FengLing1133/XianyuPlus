<template>
  <div class="dashboard">
    <div class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-value">{{ data.userCount }}</div>
        <div class="stat-label">总用户数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ data.productCount }}</div>
        <div class="stat-label">总商品数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ data.orderCount }}</div>
        <div class="stat-label">总订单数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ data.todayNewUsers }}</div>
        <div class="stat-label">今日新增用户</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ data.todayNewProducts }}</div>
        <div class="stat-label">今日新增商品</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const data = ref({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  todayNewUsers: 0,
  todayNewProducts: 0
})

onMounted(async () => {
  const res = await request.get('/admin/dashboard')
  data.value = res.data
})
</script>

<style scoped>
.dashboard { padding: 10px; }
.stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; }
.stat-card { text-align: center; }
.stat-value { font-size: 32px; font-weight: bold; color: #409eff; }
.stat-label { margin-top: 8px; color: #909399; font-size: 14px; }
</style>
