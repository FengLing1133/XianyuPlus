<template>
  <div class="dashboard">
    <div class="stat-grid">
      <div class="stat-card card">
        <div class="stat-icon">👥</div>
        <div class="stat-value">{{ data.userCount }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon">📦</div>
        <div class="stat-value">{{ data.productCount }}</div>
        <div class="stat-label">总商品数</div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon">📋</div>
        <div class="stat-value">{{ data.orderCount }}</div>
        <div class="stat-label">总订单数</div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon">🆕</div>
        <div class="stat-value">{{ data.todayNewUsers }}</div>
        <div class="stat-label">今日新增用户</div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon">🔥</div>
        <div class="stat-value">{{ data.todayNewProducts }}</div>
        <div class="stat-label">今日新增商品</div>
      </div>
      <div class="stat-card card" v-if="data.pendingReports > 0">
        <div class="stat-icon">🚩</div>
        <div class="stat-value">{{ data.pendingReports }}</div>
        <div class="stat-label">待处理举报</div>
        <button class="btn-view-reports" @click="$router.push('/admin/reports')">立即处理</button>
      </div>
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
  try {
    const res = await request.get('/admin/dashboard')
    data.value = res.data
  } catch {}
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  padding: 28px 20px;
  text-align: center;
  transition: transform var(--transition-fast);
}
.stat-card:hover { transform: translateY(-2px); }

.stat-icon { font-size: 32px; margin-bottom: 10px; }
.stat-value { font-size: 32px; font-weight: 700; color: var(--green-500); }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 6px; }

.btn-view-reports {
  margin-top: 12px;
  padding: 6px 16px;
  border: none;
  background: #ef4444;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.btn-view-reports:hover {
  background: #dc2626;
}

@media (max-width: 1024px) {
  .stat-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
