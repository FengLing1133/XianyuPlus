<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-text">
        <h1 class="hero-title">发现校园好物</h1>
        <p class="hero-subtitle">闲置交易，让物品焕发新生</p>
      </div>
      <div class="hero-icon">
        <RefreshCw :size="24" color="#EC4899" />
      </div>
    </div>

    <!-- Search & Filter Row -->
    <div class="search-row">
      <div class="search-box">
        <Search :size="15" class="search-icon-svg" />
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索你想要的宝贝..."
          class="search-input"
          @keyup.enter="search"
        />
      </div>
      <div class="filter-select" @click="showConditionDropdown = !showConditionDropdown" v-click-outside="() => showConditionDropdown = false">
        <Sparkles :size="14" class="filter-icon-pink" />
        <span>{{ conditionLabel }}</span>
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
        <Clock :size="14" class="filter-icon-cyan" />
        <span>{{ sortLabel }}</span>
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
      >全部</button>
      <button
        v-for="cat in categories"
        :key="cat.id"
        :class="['cat-pill', { active: selectedCategory === cat.id }]"
        @click="selectCategory(cat.id)"
      >
        {{ cat.name }}
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
        <Package :size="48" class="empty-icon-svg" />
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
import { RefreshCw, Search, Sparkles, Clock, Package } from 'lucide-vue-next'

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

onMounted(() => {
  fetchCategories()
  fetchData(true)
})

async function fetchCategories() {
  try {
    const res = await request.get('/category')
    categories.value = res.data || []
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
  background: var(--gradient-hero);
  border-radius: var(--radius-lg);
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-foreground);
  margin: 0 0 4px;
}

.hero-subtitle {
  font-size: 13px;
  color: var(--color-muted-foreground);
  margin: 0;
}

.hero-icon {
  width: 50px;
  height: 50px;
  background: rgba(236, 72, 153, 0.08);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Search Row */
.search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-pill);
  padding: 10px 18px;
  box-shadow: var(--shadow-card);
}

.search-icon-svg {
  color: var(--color-primary);
  margin-right: 8px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  color: var(--text-primary);
  background: transparent;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.filter-select {
  position: relative;
  padding: 9px 16px;
  border-radius: var(--radius-pill);
  background: #fff;
  border: 1.5px solid var(--border-light);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  user-select: none;
  box-shadow: var(--shadow-card);
  transition: border-color var(--transition-fast);
}

.filter-select:hover {
  border-color: var(--color-primary);
}

.filter-icon-pink {
  color: var(--color-primary);
}

.filter-icon-cyan {
  color: var(--color-accent);
}

.arrow {
  font-size: 10px;
  color: var(--text-muted);
  margin-left: 2px;
}

.filter-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 14px;
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
  background: var(--color-background);
  color: var(--color-primary);
  font-weight: 500;
}

/* Category Pills */
.category-pills {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.cat-pill {
  padding: 6px 16px;
  border-radius: var(--radius-pill);
  border: 1.5px solid var(--border-light);
  background: #fff;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.cat-pill:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.cat-pill.active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  min-height: 300px;
  padding-bottom: 24px;
}

.product-grid .empty-state {
  grid-column: 1 / -1;
}

.empty-icon-svg {
  color: var(--text-muted);
  opacity: 0.4;
  margin-bottom: 12px;
}

/* Skeleton */
.skeleton-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.skeleton-img {
  aspect-ratio: 1 / 1;
}

.skeleton-body {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
    gap: 12px;
    padding: 24px 20px;
  }
  .hero-title { font-size: 20px; }
  .search-row { flex-direction: column; }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
