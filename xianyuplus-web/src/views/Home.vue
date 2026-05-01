<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-text">
        <h1 class="hero-title">📦 校园二手，让闲置流动起来</h1>
        <p class="hero-subtitle">发现身边的宝藏好物，节约又环保 🌱</p>
      </div>
      <div class="hero-icon">🔄</div>
    </div>

    <!-- Search & Filter Row -->
    <div class="search-row">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索你想要的宝贝..."
          class="search-input"
          @keyup.enter="search"
        />
      </div>
      <div class="filter-select" @click="showConditionDropdown = !showConditionDropdown" v-click-outside="() => showConditionDropdown = false">
        <span>✨ {{ conditionLabel }}</span>
        <span class="arrow">▾</span>
        <div v-if="showConditionDropdown" class="filter-dropdown">
          <div class="filter-option" :class="{ active: condition === null }" @click.stop="selectCondition(null)">全部成色</div>
          <div class="filter-option" :class="{ active: condition === 1 }" @click.stop="selectCondition(1)">全新</div>
          <div class="filter-option" :class="{ active: condition === 2 }" @click.stop="selectCondition(2)">几乎全新</div>
          <div class="filter-option" :class="{ active: condition === 3 }" @click.stop="selectCondition(3)">轻微使用</div>
          <div class="filter-option" :class="{ active: condition === 4 }" @click.stop="selectCondition(4)">明显痕迹</div>
        </div>
      </div>
      <div class="filter-select" @click="showSortDropdown = !showSortDropdown" v-click-outside="() => showSortDropdown = false">
        <span>🕐 {{ sortLabel }}</span>
        <span class="arrow">▾</span>
        <div v-if="showSortDropdown" class="filter-dropdown">
          <div class="filter-option" :class="{ active: sort === 'newest' }" @click.stop="selectSort('newest')">最新发布</div>
          <div class="filter-option" :class="{ active: sort === 'price_asc' }" @click.stop="selectSort('price_asc')">价格从低到高</div>
          <div class="filter-option" :class="{ active: sort === 'price_desc' }" @click.stop="selectSort('price_desc')">价格从高到低</div>
        </div>
      </div>
    </div>

    <!-- Category Pills -->
    <div class="category-pills">
      <button
        :class="['cat-pill', { active: selectedCategory === null }]"
        @click="selectCategory(null)"
      >📋 全部</button>
      <button
        v-for="cat in categories"
        :key="cat.id"
        :class="['cat-pill', { active: selectedCategory === cat.id }]"
        @click="selectCategory(cat.id)"
      >
        {{ cat.emoji || '📦' }} {{ cat.name }}
      </button>
    </div>

    <!-- Product Grid -->
    <div class="product-grid">
      <template v-if="loading">
        <div v-for="n in 8" :key="n" class="skeleton-card">
          <div class="skeleton skeleton-img"></div>
          <div class="skeleton-body">
            <div class="skeleton skeleton-line" style="width: 80%;"></div>
            <div class="skeleton skeleton-line" style="width: 40%;"></div>
            <div class="skeleton skeleton-line" style="width: 60%;"></div>
          </div>
        </div>
      </template>
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
      <div v-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无商品</p>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > size" class="pagination">
      <button :disabled="page === 1" @click="goPage(page - 1)">上一页</button>
      <button
        v-for="p in totalPages"
        :key="p"
        :class="{ active: p === page }"
        @click="goPage(p)"
      >{{ p }}</button>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import ProductCard from '@/components/ProductCard.vue'

const keyword = ref('')
const categories = ref([])
const selectedCategory = ref(null)
const sort = ref('newest')
const condition = ref(null)
const products = ref([])
const loading = ref(true)
const page = ref(1)
const size = ref(12)
const total = ref(0)
const showConditionDropdown = ref(false)
const showSortDropdown = ref(false)

const conditionLabel = computed(() => {
  const map = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显痕迹' }
  return condition.value ? map[condition.value] : '全部成色'
})

const sortLabel = computed(() => {
  const map = { newest: '最新发布', price_asc: '价格↑', price_desc: '价格↓' }
  return map[sort.value] || '最新发布'
})

const totalPages = computed(() => Math.ceil(total.value / size.value))

// Category emoji mapping
const categoryEmojis = {
  '教材教辅': '📚', '教材': '📚', '教辅': '📚',
  '电子产品': '📱', '电子': '📱', '数码': '📱', '手机': '📱', '电脑': '💻',
  '生活用品': '🎒', '生活': '🎒', '日用': '🎒',
  '服饰鞋包': '👗', '服饰': '👗', '衣服': '👗', '鞋': '👟',
  '运动娱乐': '🎮', '运动': '🎮', '娱乐': '🎮',
  '图书': '📖',
}

onMounted(() => {
  fetchCategories()
  fetchData(true)
})

async function fetchCategories() {
  try {
    const res = await request.get('/category')
    categories.value = (res.data || []).map(cat => ({
      ...cat,
      emoji: categoryEmojis[cat.name] || '📦'
    }))
  } catch {}
}

async function fetchData(showSkeleton = false) {
  if (showSkeleton) loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: sort.value }
    if (keyword.value) params.keyword = keyword.value
    if (selectedCategory.value) params.categoryId = selectedCategory.value
    if (condition.value) params.condition = condition.value

    const res = await request.get('/product', { params })
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchData()
}

function selectCategory(catId) {
  selectedCategory.value = catId
  page.value = 1
  fetchData()
}

function selectCondition(val) {
  condition.value = val
  showConditionDropdown.value = false
  page.value = 1
  fetchData()
}

function selectSort(val) {
  sort.value = val
  showSortDropdown.value = false
  page.value = 1
  fetchData()
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  fetchData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.home-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Hero */
.hero-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 40%, #e0f2f1 100%);
  border-radius: var(--radius-lg);
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--green-500);
  margin: 0 0 6px;
}

.hero-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.hero-icon {
  width: 60px;
  height: 60px;
  background: rgba(43, 153, 57, 0.08);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

/* Search Row */
.search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-pill);
  padding: 10px 20px;
  box-shadow: var(--shadow-card);
}

.search-icon {
  font-size: 16px;
  color: var(--text-secondary);
  margin-right: 10px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--text-primary);
  background: transparent;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.filter-select {
  position: relative;
  padding: 10px 18px;
  border-radius: var(--radius-pill);
  background: #fff;
  border: 1.5px solid var(--border-light);
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  user-select: none;
  box-shadow: var(--shadow-card);
  transition: border-color var(--transition-fast);
}

.filter-select:hover {
  border-color: var(--green-500);
}

.arrow {
  font-size: 10px;
  color: var(--text-secondary);
  margin-left: 2px;
}

.filter-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-hover);
  padding: 6px;
  z-index: 50;
  min-width: 140px;
}

.filter-option {
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  white-space: nowrap;
  transition: background var(--transition-fast);
}

.filter-option:hover {
  background: var(--bg-page);
}

.filter-option.active {
  background: var(--green-50);
  color: var(--green-500);
  font-weight: 500;
}

/* Category Pills */
.category-pills {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.cat-pill {
  padding: 8px 20px;
  border-radius: var(--radius-pill);
  border: 1.5px solid var(--border-light);
  background: #fff;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.cat-pill:hover {
  border-color: var(--green-500);
  color: var(--green-500);
}

.cat-pill.active {
  background: var(--green-500);
  color: #fff;
  border-color: var(--green-500);
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 300px;
}

.product-grid .empty-state {
  grid-column: 1 / -1;
}

/* Skeleton */
.skeleton-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.skeleton-img {
  height: 180px;
}

.skeleton-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 14px;
}

/* Responsive */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-banner {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 24px 20px;
  }
  .hero-title { font-size: 20px; }
  .search-row { flex-direction: column; }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
