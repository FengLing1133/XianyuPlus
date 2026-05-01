<template>
  <div class="admin-page">
    <div class="card">
      <div class="table-wrap">
        <table class="data-table" v-if="!loading">
          <thead>
            <tr>
              <th>订单号</th>
              <th>商品</th>
              <th>金额</th>
              <th>买家</th>
              <th>卖家</th>
              <th>状态</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id">
              <td class="td-mono">{{ o.id }}</td>
              <td class="td-title">{{ o.productTitle }}</td>
              <td class="td-price">¥{{ o.amount }}</td>
              <td>{{ o.buyerName }}</td>
              <td>{{ o.sellerName }}</td>
              <td>
                <span :class="['pill-tag', statusClass(o.status)]">{{ statusText(o.status) }}</span>
              </td>
              <td class="td-mono">{{ o.createdAt?.substring(0, 19) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="loading" style="padding:40px;text-align:center;color:var(--text-secondary);">加载中...</div>

      <div v-if="!loading && orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无订单数据</p>
      </div>

      <div v-if="total > 10" class="pagination">
        <button :disabled="page === 1" @click="goPage(page - 1)">上一页</button>
        <button v-for="p in totalPages" :key="p" :class="{ active: p === page }" @click="goPage(p)">{{ p }}</button>
        <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const orders = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / 10))

function statusText(s) { const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已完成', 5: '已取消' }; return map[s] || '未知' }
function statusClass(s) {
  const map = { 0: 'pill-tag-blue', 1: 'pill-tag-orange', 2: 'pill-tag-cyan', 3: 'pill-tag-teal', 4: 'pill-tag-green', 5: 'pill-tag-red' }
  return map[s] || 'pill-tag-gray'
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

function goPage(p) { page.value = p; fetchData() }
</script>

<style scoped>
.admin-page { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.card { padding: 24px; }

.table-wrap { overflow-x: auto; }

.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th {
  text-align: left;
  padding: 12px 14px;
  border-bottom: 2px solid var(--border-lighter);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  font-size: 13px;
}
.data-table td {
  padding: 12px 14px;
  border-bottom: 1px solid var(--border-lighter);
  color: var(--text-primary);
}
.data-table tbody tr:hover { background: #fafafa; }
.td-mono { font-family: 'SF Mono', 'Consolas', monospace; font-size: 12px; color: var(--text-secondary); }
.td-title { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td-price { font-weight: 600; color: var(--price-red); white-space: nowrap; }

/* Status tag colors */
.pill-tag-blue { background: #f0f4ff; color: #5b7fff; }
.pill-tag-orange { background: #fff3e0; color: #e65100; }
.pill-tag-cyan { background: #e8f4fd; color: #0277bd; }
.pill-tag-teal { background: #e0f7fa; color: #00838f; }
.pill-tag-green { background: #e8f5e9; color: var(--green-500); }
.pill-tag-red { background: #fef0f0; color: var(--price-red); }
.pill-tag-gray { background: #f5f5f5; color: #666; }
</style>
