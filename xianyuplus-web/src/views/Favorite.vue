<template>
  <div class="favorite-page">
    <el-card>
      <template #header>我的收藏</template>
      <div class="fav-grid" v-loading="loading">
        <div v-for="item in favorites" :key="item.id" class="fav-item" @click="$router.push(`/product/${item.productId}`)">
          <img :src="item.productImage || defaultImg" class="fav-img" />
          <div class="fav-info">
            <div class="fav-title">{{ item.productTitle }}</div>
            <div class="fav-price">¥{{ item.productPrice }}</div>
          </div>
          <el-button link type="danger" @click.stop="removeFav(item)">取消收藏</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏" />
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

const favorites = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const defaultImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YwZjBmMCIvPjwvc3ZnPg=='

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/favorite', { params: { page: page.value, size: 10 } })
    favorites.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function removeFav(item) {
  await request.post(`/favorite/${item.productId}`)
  ElMessage.success('已取消收藏')
  fetchData()
}
</script>

<style scoped>
.favorite-page { max-width: 800px; margin: 0 auto; }
.fav-grid { display: flex; flex-direction: column; gap: 12px; }
.fav-item { display: flex; align-items: center; gap: 15px; padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; cursor: pointer; }
.fav-item:hover { background: #fafafa; }
.fav-img { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; }
.fav-info { flex: 1; }
.fav-title { font-size: 14px; margin-bottom: 6px; }
.fav-price { font-size: 16px; font-weight: bold; color: #f56c6c; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
