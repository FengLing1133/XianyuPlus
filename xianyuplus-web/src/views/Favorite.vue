<template>
  <div class="favorite-page">
    <h2 class="page-title">我的收藏</h2>

    <div class="card fav-card">
      <div v-if="loading" style="padding: 40px; text-align: center; color: var(--text-secondary);">加载中...</div>
      <template v-else>
        <div v-for="item in favorites" :key="item.id" class="fav-item" @click="$router.push(`/product/${item.productId}`)">
          <div class="fav-img-wrap" :style="{ background: colors[item.id % colors.length] }">
            <span class="fav-placeholder">{{ emojis[item.id % emojis.length] }}</span>
            <img v-if="item.productImage" :src="item.productImage" class="fav-img" @error="$event.target.style.display='none'" />
          </div>
          <div class="fav-info">
            <div class="fav-title">{{ item.productTitle }}</div>
            <div class="fav-price">¥{{ item.productPrice }}</div>
          </div>
          <button class="fav-remove" @click.stop="removeFav(item)">取消收藏</button>
        </div>

        <div v-if="favorites.length === 0" class="empty-state">
          <div class="empty-icon">-</div>
          <p>暂无收藏</p>
        </div>

        <div v-if="total > 10" class="pagination">
          <button :disabled="page === 1" @click="goPage(page - 1)">上一页</button>
          <button
            v-for="p in totalPages"
            :key="p"
            :class="{ active: p === page }"
            @click="goPage(p)"
          >{{ p }}</button>
          <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import { Toast } from '@/utils/toast'

const favorites = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)

const emojis = ['书', '手', '电', '衣', '游', '椅', '耳', '表']
const colors = ['#fce4ec', '#e3f2fd', 'var(--color-background)', '#f3e5f5', '#fff9c4', '#fff3e0', '#e0f7fa', '#fbe9e7']
const totalPages = computed(() => Math.ceil(total.value / 10))

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
  Toast.success('已取消收藏')
  fetchData()
}

function goPage(p) {
  page.value = p
  fetchData()
}
</script>

<style scoped>
.favorite-page {
  max-width: 800px;
  margin: 0 auto;
}

.fav-card {
  overflow: hidden;
}

.fav-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-lighter);
}
.fav-item:last-child { border-bottom: none; }
.fav-item:hover { background: #fafafa; }

.fav-img-wrap {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.fav-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fav-placeholder {
  font-size: 32px;
}

.fav-info { flex: 1; }
.fav-title { font-size: 14px; color: var(--text-primary); margin-bottom: 6px; }
.fav-price { font-size: 18px; font-weight: 700; color: var(--price-red); }

.fav-remove {
  background: none;
  color: var(--text-muted);
  font-size: 13px;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  flex-shrink: 0;
}
.fav-remove:hover { color: var(--price-red); background: #fef0f0; }
</style>
