<template>
  <div class="admin-page">
    <div class="card">
      <div class="toolbar">
        <input v-model="keyword" class="form-input" placeholder="🔍 搜索商品标题..." @change="fetchData" style="max-width: 280px;" />
        <select v-model="status" class="form-input form-select" @change="fetchData" style="width: 140px;">
          <option :value="null">全部状态</option>
          <option :value="1">在售</option>
          <option :value="2">已售出</option>
          <option :value="3">已下架</option>
        </select>
      </div>

      <div class="table-wrap">
        <table class="data-table" v-if="!loading">
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>卖家</th>
              <th>价格</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td class="td-mono">{{ p.id }}</td>
              <td class="td-title">{{ p.title }}</td>
              <td>{{ p.sellerName }}</td>
              <td class="td-price">¥{{ p.price }}</td>
              <td>
                <span :class="['pill-tag', statusClass(p.status)]">{{ statusText(p.status) }}</span>
              </td>
              <td class="td-mono">{{ p.createdAt?.substring(0, 19) }}</td>
              <td>
                <button class="btn-pill btn-danger-sm" @click="deleteProduct(p)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="loading" style="padding:40px;text-align:center;color:var(--text-secondary);">加载中...</div>

      <div v-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无商品数据</p>
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
import { Toast } from '@/utils/toast'
import { Dialog } from '@/utils/dialog'

const products = ref([])
const loading = ref(true)
const keyword = ref('')
const status = ref(null)
const page = ref(1)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / 10))

function statusText(s) { const map = { 1: '在售', 2: '已售出', 3: '已下架' }; return map[s] || '' }
function statusClass(s) {
  const map = { 1: 'pill-tag-green', 2: 'pill-tag-gray', 3: 'pill-tag-red' }
  return map[s] || 'pill-tag-gray'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    if (status.value) params.status = status.value
    const res = await request.get('/admin/products', { params })
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function deleteProduct(row) {
  const ok = await Dialog.confirm({ title: '删除确认', message: `确定删除商品「${row.title}」？此操作不可恢复。` })
  if (!ok) return
  await request.delete(`/admin/products/${row.id}`)
  Toast.success('已删除')
  fetchData()
}

function goPage(p) { page.value = p; fetchData() }
</script>

<style scoped>
.admin-page { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.card { padding: 24px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 20px; }
.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23999' d='M6 8L2 4h8z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  padding-right: 28px;
  cursor: pointer;
}

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

.btn-danger-sm {
  background: #fef0f0; color: var(--price-red);
  padding: 4px 14px; font-size: 12px; border-radius: var(--radius-pill);
}
.btn-danger-sm:hover { background: #fde2e2; }
</style>
