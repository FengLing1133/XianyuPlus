<template>
  <div class="admin-orders">
    <el-card>
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="180" />
        <el-table-column prop="productTitle" label="商品" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

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
    const res = await request.get('/admin/orders', { params: { page: page.value, size: 10 } })
    orders.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-orders { padding: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
