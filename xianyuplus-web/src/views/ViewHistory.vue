<template>
  <div class="view-history">
    <div class="page-header">
      <h2>浏览历史</h2>
      <button v-if="historyList.length > 0" class="btn-clear" @click="handleClear">
        清空历史
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="historyList.length === 0" class="empty">
      <div class="empty-icon">📖</div>
      <div class="empty-text">暂无浏览记录</div>
      <router-link to="/" class="btn-go">去逛逛</router-link>
    </div>
    <div v-else class="history-content">
      <!-- 按日期分组 -->
      <div v-for="group in groupedHistory" :key="group.date" class="date-group">
        <div class="date-label">{{ group.date }}</div>
        <div class="product-grid">
          <div v-for="item in group.items" :key="item.id" class="history-item">
            <router-link :to="`/product/${item.productId}`" class="product-link">
              <ProductCard :product="item.product" />
            </router-link>
            <button class="btn-delete" @click="handleDelete(item.id)" title="删除">×</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 20" class="pagination">
        <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / 20) }}</span>
        <button :disabled="page >= Math.ceil(total / 20)" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getViewHistory, clearViewHistory, deleteViewHistory } from '@/api/viewHistory'
import request from '@/api/request'
import { Dialog } from '@/utils/dialog'
import { Toast } from '@/utils/toast'
import ProductCard from '@/components/ProductCard.vue'

const historyList = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(true)

// 按日期分组
const groupedHistory = computed(() => {
  const groups = {}
  const today = new Date().toLocaleDateString('zh-CN')
  const yesterday = new Date(Date.now() - 86400000).toLocaleDateString('zh-CN')

  historyList.value.forEach(item => {
    const date = new Date(item.createdAt).toLocaleDateString('zh-CN')
    let label = date
    if (date === today) label = '今天'
    else if (date === yesterday) label = '昨天'

    if (!groups[label]) {
      groups[label] = { date: label, items: [] }
    }
    groups[label].items.push(item)
  })

  return Object.values(groups)
})

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getViewHistory(page.value, 20)
    if (res.code === 200) {
      const records = res.data.records || []
      total.value = res.data.total || 0

      // 获取每个商品的详细信息
      const enriched = await Promise.all(
        records.map(async (item) => {
          try {
            const productRes = await request.get(`/product/${item.productId}`)
            return {
              ...item,
              product: productRes.data || null
            }
          } catch {
            return { ...item, product: null }
          }
        })
      )

      historyList.value = enriched.filter(item => item.product)
    }
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  page.value = p
  fetchHistory()
  window.scrollTo(0, 0)
}

async function handleDelete(id) {
  await deleteViewHistory(id)
  historyList.value = historyList.value.filter(item => item.id !== id)
  total.value--
  Toast.success('已删除')
}

async function handleClear() {
  const ok = await Dialog.confirm({
    title: '清空浏览历史',
    message: '确定要清空所有浏览记录吗？此操作不可恢复。'
  })
  if (!ok) return

  await clearViewHistory()
  historyList.value = []
  total.value = 0
  Toast.success('已清空')
}

onMounted(fetchHistory)
</script>

<style scoped>
.view-history {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.btn-clear {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.btn-clear:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.btn-go {
  display: inline-block;
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
}

.btn-go:hover {
  opacity: 0.9;
}

.date-group {
  margin-bottom: 32px;
}

.date-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.history-item {
  position: relative;
}

.product-link {
  text-decoration: none;
  color: inherit;
}

.btn-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .btn-delete {
  opacity: 1;
}

.btn-delete:hover {
  background: rgba(239, 68, 68, 0.8);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px 0;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}
</style>
