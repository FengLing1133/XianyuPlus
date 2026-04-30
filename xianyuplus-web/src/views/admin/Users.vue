<template>
  <div class="admin-page">
    <div class="card">
      <div class="toolbar">
        <input v-model="keyword" class="form-input search-input" placeholder="🔍 搜索用户名/昵称..." @change="fetchData" style="max-width: 280px;" />
      </div>

      <div class="table-wrap">
        <table class="data-table" v-if="!loading">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>昵称</th>
              <th>手机号</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td class="td-mono">{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.nickname }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>
                <span :class="['pill-tag', user.role === 1 ? 'pill-tag-red' : 'pill-tag-gray']">
                  {{ user.role === 1 ? '管理员' : '用户' }}
                </span>
              </td>
              <td>
                <span :class="['pill-tag', user.status === 0 ? 'pill-tag-green' : 'pill-tag-red']">
                  {{ user.status === 0 ? '正常' : '封禁' }}
                </span>
              </td>
              <td class="td-mono">{{ user.createdAt?.substring(0, 19) }}</td>
              <td>
                <button
                  v-if="user.status === 0"
                  class="btn-pill btn-danger-sm"
                  @click="toggleStatus(user, 1)"
                >封禁</button>
                <button
                  v-else
                  class="btn-pill btn-success-sm"
                  @click="toggleStatus(user, 0)"
                >解封</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="loading" style="padding:40px;text-align:center;color:var(--text-secondary);">加载中...</div>

      <div v-if="!loading && users.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <p>暂无用户数据</p>
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

const users = ref([])
const loading = ref(true)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / 10))

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/admin/users', { params })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(user, status) {
  await request.put(`/admin/users/${user.id}/status`, null, { params: { status } })
  Toast.success(status === 1 ? '已封禁' : '已解封')
  fetchData()
}

function goPage(p) { page.value = p; fetchData() }
</script>

<style scoped>
.admin-page { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.card { padding: 24px; }
.toolbar { margin-bottom: 20px; }
.search-input { max-width: 280px; }

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

.pill-tag-red { background: #fef0f0; color: var(--price-red); }
.pill-tag-gray { background: #f5f5f5; color: #666; }

.btn-danger-sm {
  background: #fef0f0; color: var(--price-red);
  padding: 4px 14px; font-size: 12px; border-radius: var(--radius-pill);
}
.btn-danger-sm:hover { background: #fde2e2; }

.btn-success-sm {
  background: var(--green-50); color: var(--green-500);
  padding: 4px 14px; font-size: 12px; border-radius: var(--radius-pill);
}
.btn-success-sm:hover { background: #c8e6c9; }
</style>
