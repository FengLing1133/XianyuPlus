<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <el-tabs v-model="type" @tab-change="fetchData">
          <el-tab-pane label="我买到的" name="buy" />
          <el-tab-pane label="我卖出的" name="sell" />
        </el-tabs>
      </template>
      <div v-loading="loading">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-header">
            <span>订单号：{{ order.id }}</span>
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>
          <div class="order-body" @click="$router.push(`/product/${order.productId}`)">
            <img :src="order.productImage || defaultImg" class="order-img" />
            <div class="order-info">
              <div class="order-title">{{ order.productTitle }}</div>
              <div class="order-price">¥{{ order.amount }}</div>
            </div>
          </div>
          <div class="order-footer">
            <span class="other-user">
              {{ type === 'buy' ? '卖家' : '买家' }}：{{ type === 'buy' ? order.sellerName : order.buyerName }}
            </span>
            <span class="order-time">{{ order.createdAt?.substring(0, 16) }}</span>
            <div class="order-actions">
              <el-button v-if="type === 'buy' && order.status === 0" size="small" type="danger" @click="cancelOrder(order)">取消订单</el-button>
              <el-button v-if="type === 'sell' && order.status === 0" size="small" type="primary" @click="payOrder(order)">标记已付款</el-button>
              <el-button v-if="type === 'sell' && order.status === 1" size="small" type="success" @click="completeOrder(order)">标记已完成</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
      </div>
      <div class="pagination" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const type = ref('buy')
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const defaultImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjgwIiBmaWxsPSIjZjBmMGYwIi8+PC9zdmc+'

function statusText(s) {
  const map = { 0: '待付款', 1: '已付款', 2: '已完成', 3: '已取消' }
  return map[s] || '未知'
}

function statusType(s) {
  const map = { 0: 'info', 1: '', 2: 'success', 3: 'danger' }
  return map[s] || 'info'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/order', { params: { type: type.value, page: page.value, size: 10 } })
    orders.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function cancelOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 3 } })
  ElMessage.success('订单已取消')
  fetchData()
}

async function payOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 1 } })
  ElMessage.success('已标记为已付款')
  fetchData()
}

async function completeOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 2 } })
  ElMessage.success('订单已完成')
  fetchData()
}
</script>

<style scoped>
.order-page { max-width: 800px; margin: 0 auto; }
.order-item { border: 1px solid #ebeef5; border-radius: 8px; padding: 15px; margin-bottom: 12px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-size: 13px; color: #909399; }
.order-body { display: flex; gap: 12px; cursor: pointer; }
.order-img { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; }
.order-info { flex: 1; }
.order-title { font-size: 15px; margin-bottom: 6px; }
.order-price { font-size: 18px; font-weight: bold; color: #f56c6c; }
.order-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f0f0f0; font-size: 13px; }
.other-user { color: #606266; }
.order-time { color: #909399; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
