<template>
  <div class="home-page">
    <!-- Search bar -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品..." size="large" clearable @clear="search" @keyup.enter="search">
        <template #append>
          <el-button @click="search" :icon="Search">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="content">
      <!-- Category sidebar -->
      <div class="sidebar">
        <div class="sidebar-title">商品分类</div>
        <div class="category-item" :class="{ active: selectedCategory === null }" @click="selectCategory(null)">全部</div>
        <div v-for="cat in categories" :key="cat.id">
          <div class="category-item" :class="{ active: selectedCategory === cat.id }" @click="selectCategory(cat.id)">
            {{ cat.name }}
          </div>
          <div v-for="child in cat.children" :key="child.id"
               class="category-child"
               :class="{ active: selectedCategory === child.id }"
               @click="selectCategory(child.id)">
            {{ child.name }}
          </div>
        </div>
      </div>

      <!-- Main area -->
      <div class="main-area">
        <!-- Filters -->
        <div class="filters">
          <div class="sort-btns">
            <el-radio-group v-model="sort" size="small" @change="fetchData">
              <el-radio-button value="newest">最新</el-radio-button>
              <el-radio-button value="price_asc">价格↑</el-radio-button>
              <el-radio-button value="price_desc">价格↓</el-radio-button>
            </el-radio-group>
          </div>
          <div class="condition-filters">
            <el-select v-model="condition" placeholder="成色" clearable size="small" @change="fetchData" style="width: 130px;">
              <el-option label="全新" :value="1" />
              <el-option label="几乎全新" :value="2" />
              <el-option label="轻微使用" :value="3" />
              <el-option label="明显痕迹" :value="4" />
            </el-select>
          </div>
        </div>

        <!-- Product grid -->
        <div class="product-grid" v-loading="loading">
          <ProductCard v-for="item in products" :key="item.id" :product="item" />
          <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
        </div>

        <!-- Pagination -->
        <div class="pagination" v-if="total > 0">
          <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="fetchData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/api/request'
import ProductCard from '@/components/ProductCard.vue'

const keyword = ref('')
const categories = ref([])
const selectedCategory = ref(null)
const sort = ref('newest')
const condition = ref(null)
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => {
  fetchCategories()
  fetchData()
})

async function fetchCategories() {
  const res = await request.get('/category')
  categories.value = res.data
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      sort: sort.value
    }
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
</script>

<style scoped>
.home-page { max-width: 1200px; margin: 0 auto; }
.search-bar { margin-bottom: 20px; }
.search-bar .el-input { max-width: 600px; }
.content { display: flex; gap: 20px; }
.sidebar { width: 180px; flex-shrink: 0; background: #fff; border-radius: 8px; padding: 15px; }
.sidebar-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid #ebeef5; }
.category-item { padding: 8px 10px; cursor: pointer; border-radius: 4px; font-size: 14px; }
.category-item:hover, .category-item.active { color: #409eff; background: #ecf5ff; }
.category-child { padding: 6px 10px 6px 24px; cursor: pointer; border-radius: 4px; font-size: 13px; color: #606266; }
.category-child:hover, .category-child.active { color: #409eff; background: #ecf5ff; }
.main-area { flex: 1; }
.filters { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; background: #fff; padding: 12px 15px; border-radius: 8px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; min-height: 300px; }
.pagination { display: flex; justify-content: center; margin-top: 30px; }
</style>
