<template>
  <div class="seller-profile" v-if="seller">
    <!-- 卖家资料卡片 -->
    <div class="profile-card card">
      <div class="profile-header">
        <div class="avatar">
          <img v-if="seller.avatar" :src="seller.avatar" alt="头像">
          <span v-else class="avatar-placeholder">{{ seller.nickname?.charAt(0) || '?' }}</span>
        </div>
        <div class="info">
          <h2 class="nickname">{{ seller.nickname || '未设置昵称' }}</h2>
          <div class="join-time">注册于 {{ formatDate(seller.createdAt) }}</div>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">
            <span class="star-icon">&#9733;</span>
            {{ seller.avgRating || 0 }}
          </div>
          <div class="stat-label">信用评分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.completedOrders || 0 }}</div>
          <div class="stat-label">完成交易</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.positiveRate || 0 }}%</div>
          <div class="stat-label">好评率</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ seller.reviewCount || 0 }}</div>
          <div class="stat-label">评价数</div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-section card">
      <h3>TA的商品 ({{ total }})</h3>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="products.length === 0" class="empty">暂无在售商品</div>
      <div v-else class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 12" class="pagination">
        <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / 12) }}</span>
        <button :disabled="page >= Math.ceil(total / 12)" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="loading-page">加载中...</div>
  <div v-else class="error-page">卖家不存在</div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getSellerProfile, getSellerStats, getSellerProducts } from '@/api/user'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const sellerId = ref(route.params.id)

const seller = ref(null)
const stats = ref({})
const products = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(true)

async function fetchSeller() {
  loading.value = true
  try {
    const [profileRes, statsRes] = await Promise.all([
      getSellerProfile(sellerId.value),
      getSellerStats(sellerId.value)
    ])

    if (profileRes.code === 200) {
      seller.value = profileRes.data
    }
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }

    await fetchProducts()
  } finally {
    loading.value = false
  }
}

async function fetchProducts() {
  const res = await getSellerProducts(sellerId.value, page.value, 12)
  if (res.code === 200) {
    products.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function goPage(p) {
  page.value = p
  fetchProducts()
  window.scrollTo(0, 0)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(fetchSeller)

watch(() => route.params.id, (newId) => {
  if (newId) {
    sellerId.value = newId
    page.value = 1
    fetchSeller()
  }
})
</script>

<style scoped>
.seller-profile {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.profile-card {
  padding: 32px;
  margin-bottom: 32px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 36px;
  font-weight: 600;
  color: var(--color-primary);
}

.info {
  flex: 1;
}

.nickname {
  margin: 0 0 8px;
  font-size: 24px;
}

.join-time {
  font-size: 14px;
  color: var(--text-secondary);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-lighter);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.star-icon {
  color: #ffc107;
  font-size: 24px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.products-section {
  padding: 24px;
}

.products-section h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px 0 0;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.loading-page,
.error-page {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-secondary);
  font-size: 16px;
}
</style>
