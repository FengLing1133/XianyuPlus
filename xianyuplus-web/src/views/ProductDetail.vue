<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="product">
      <div class="detail-content">
        <!-- Images -->
        <div class="image-section">
          <el-carousel height="400px" v-if="product.images && product.images.length > 0">
            <el-carousel-item v-for="img in product.images" :key="img.id">
              <img :src="img.url" class="carousel-img" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="no-image">暂无图片</div>
        </div>

        <!-- Info -->
        <div class="info-section">
          <h1 class="title">{{ product.title }}</h1>
          <div class="price-row">
            <span class="price">¥{{ product.price }}</span>
            <span class="original" v-if="product.originalPrice">原价 ¥{{ product.originalPrice }}</span>
          </div>
          <div class="tags">
            <el-tag>{{ conditionText(product.condition) }}</el-tag>
            <el-tag type="info">{{ product.categoryName }}</el-tag>
          </div>
          <div class="meta">
            <el-avatar :size="32" :src="sellerAvatar" />
            <span>{{ product.sellerName }}</span>
          </div>
          <div class="stats">
            <span>{{ product.viewCount }}次浏览</span>
            <span>{{ product.createdAt?.substring(0, 10) }}发布</span>
          </div>

          <div class="actions" v-if="!isOwner">
            <el-button type="danger" size="large" @click="handleBuy">立即购买</el-button>
            <el-button :type="favorited ? 'warning' : 'default'" size="large" @click="handleFavorite">
              <el-icon><Star /></el-icon> {{ favorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button size="large" @click="handleChat">联系卖家</el-button>
          </div>
          <div class="actions" v-else>
            <el-button type="primary" size="large" @click="$router.push(`/edit/${product.id}`)">编辑</el-button>
            <el-button :type="product.status === 1 ? 'warning' : 'success'" size="large" @click="handleToggleStatus">
              {{ product.status === 1 ? '下架' : '上架' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Description -->
      <el-card class="desc-card">
        <template #header>商品描述</template>
        <div class="desc-content">{{ product.description || '暂无描述' }}</div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const loading = ref(true)
const favorited = ref(false)

const isOwner = computed(() => userStore.userInfo?.id === product.value?.userId)
const sellerAvatar = computed(() => '')

function conditionText(c) {
  const map = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显痕迹' }
  return map[c] || '未知'
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await request.get(`/product/${id}`)
    product.value = res.data
  } finally {
    loading.value = false
  }
})

async function handleBuy() {
  try {
    await ElMessageBox.confirm(`确认购买「${product.value.title}」？`, '确认购买', { confirmButtonText: '确认', type: 'warning' })
    await request.post('/order', { productId: product.value.id })
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch {}
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const res = await request.post(`/favorite/${product.value.id}`)
  favorited.value = res.data.favorited
  ElMessage.success(res.data.favorited ? '收藏成功' : '已取消收藏')
}

function handleChat() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push(`/chat/${product.value.userId}`)
}

async function handleToggleStatus() {
  const newStatus = product.value.status === 1 ? 3 : 1
  await request.put(`/product/${product.value.id}/status`, null, { params: { status: newStatus } })
  product.value.status = newStatus
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
}
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; }
.detail-content { display: flex; gap: 30px; margin-bottom: 30px; }
.image-section { width: 440px; flex-shrink: 0; }
.carousel-img { width: 100%; height: 400px; object-fit: cover; }
.no-image { width: 100%; height: 400px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #999; border-radius: 8px; }
.info-section { flex: 1; }
.title { font-size: 22px; margin-bottom: 15px; }
.price-row { margin-bottom: 15px; }
.price { font-size: 28px; font-weight: bold; color: #f56c6c; }
.original { font-size: 14px; color: #c0c4cc; text-decoration: line-through; margin-left: 10px; }
.tags { margin-bottom: 15px; display: flex; gap: 8px; }
.meta { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; font-size: 14px; }
.stats { color: #909399; font-size: 13px; margin-bottom: 20px; display: flex; gap: 15px; }
.actions { display: flex; gap: 10px; }
.desc-card { margin-bottom: 30px; }
.desc-content { white-space: pre-wrap; line-height: 1.8; color: #606266; }
</style>
