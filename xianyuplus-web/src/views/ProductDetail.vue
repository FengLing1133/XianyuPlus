<template>
  <div class="detail-page" v-if="!loading">
    <template v-if="product">
      <div class="detail-content">
        <!-- Image Gallery -->
        <div class="image-section">
          <div v-if="product.images && product.images.length > 0 && !mainImgError" class="image-gallery">
            <img :src="currentImage" class="main-image" @error="mainImgError = true" />
            <div class="thumb-list" v-if="product.images.length > 1">
              <img
                v-for="(img, i) in product.images"
                :key="img.id"
                :src="img.url"
                class="thumb"
                :class="{ active: i === currentIndex }"
                @click="currentIndex = i; mainImgError = false"
              />
            </div>
          </div>
          <div v-else class="no-image" :style="{ background: placeholderColor }">
            <Package :size="48" class="placeholder-icon-svg" />
          </div>
        </div>

        <!-- Info -->
        <div class="info-section">
          <h1 class="title">{{ product.title }}</h1>

          <div class="price-row">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original">原价 ¥{{ product.originalPrice }}</span>
          </div>

          <div class="tags-row">
            <span class="pill-tag pill-tag-green">{{ conditionText(product.condition) }}</span>
            <span class="pill-tag" style="background:#f5f5f5;color:#666;">{{ product.categoryName }}</span>
          </div>

          <div class="seller-row">
            <span class="seller-avatar"></span>
            <router-link :to="`/seller/${product.userId}`" class="seller-link">{{ product.sellerName || '卖家' }}</router-link>
          </div>

          <div class="stats-row">
            <span>{{ product.viewCount }}次浏览</span>
            <span>{{ product.createdAt?.substring(0, 10) }}发布</span>
          </div>

          <div class="action-row" v-if="!isOwner">
            <button class="btn-pill btn-buy" @click="handleBuy">立即购买</button>
            <button class="btn-pill btn-fav" :class="{ favored: favorited }" @click="handleFavorite">
              {{ favorited ? '已收藏' : '收藏' }}
            </button>
            <button class="btn-pill btn-chat" @click="handleChat">联系卖家</button>
            <button
              v-if="userStore.token"
              class="btn-pill btn-report"
              :class="{ reported: hasReported }"
              :disabled="hasReported"
              @click="showReportForm = true"
            >
              {{ hasReported ? '已举报' : '举报' }}
            </button>
          </div>
          <div class="action-row" v-else>
            <button class="btn-pill btn-pill-primary" @click="$router.push(`/edit/${product.id}`)">编辑</button>
            <button class="btn-pill" :style="product.status === 1 ? 'background:#fff3e0;color:#e65100;' : 'background:var(--color-background);color:#EC4899;'" @click="handleToggleStatus">
              {{ product.status === 1 ? '下架' : '上架' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="desc-card card">
        <h3 class="desc-title">商品描述</h3>
        <div class="desc-body">{{ product.description || '暂无描述' }}</div>
      </div>

      <!-- Reviews -->
      <div class="review-section">
        <h3>商品评价</h3>
        <ReviewList
          :product-id="Number(product.id)"
          :seller-id="Number(product.userId)"
          :user-id="Number(userStore.userInfo?.id)"
          :show-reply-button="isOwner"
          :show-delete-button="!!userStore.userInfo"
        />
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-icon"></div>
      <p>商品不存在或已下架</p>
    </div>

    <ReportForm
      :visible="showReportForm"
      :product-id="product?.id"
      @close="showReportForm = false"
      @success="hasReported = true"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { Toast } from '@/utils/toast'
import { Dialog } from '@/utils/dialog'
import { conditionText, getProductColor } from '@/utils/category'
import { Package } from 'lucide-vue-next'
import ReviewList from '@/components/ReviewList.vue'
import ReportForm from '@/components/ReportForm.vue'
import { checkReported } from '@/api/report'
import { recordView } from '@/api/viewHistory'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const loading = ref(true)
const favorited = ref(false)
const currentIndex = ref(0)
const mainImgError = ref(false)
const showReportForm = ref(false)
const hasReported = ref(false)

const isOwner = computed(() => userStore.userInfo?.id === product.value?.userId)
const currentImage = computed(() => product.value?.images?.[currentIndex.value]?.url)
const placeholderColor = computed(() => product.value ? getProductColor(product.value.id) : 'var(--placeholder-pink)')

onMounted(async () => {
  const id = String(route.params.id)
  try {
    const res = await request.get(`/product/${id}`)
    product.value = res.data

    // 记录浏览历史
    if (userStore.token) {
      recordView(route.params.id).catch(() => {}) // 静默失败
    }

    // 检查是否已举报
    if (userStore.token && product.value.userId !== userStore.userInfo?.id) {
      const reportRes = await checkReported(product.value.id)
      if (reportRes.code === 200) {
        hasReported.value = reportRes.data
      }
    }
  } catch (e) {
    // Toast already shown by axios interceptor
  } finally {
    loading.value = false
  }
})

async function handleBuy() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const ok = await Dialog.confirm({
    title: '确认购买',
    message: `确认购买「${product.value.title}」？`
  })
  if (!ok) return
  try {
    await request.post('/order', { productId: product.value.id })
    Toast.success('下单成功')
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
  Toast.success(res.data.favorited ? '收藏成功' : '已取消收藏')
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
  Toast.success(newStatus === 1 ? '已上架' : '已下架')
}
</script>

<style scoped>
.detail-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.detail-content {
  display: flex;
  gap: 32px;
  margin-bottom: 28px;
}

.image-section {
  width: 440px;
  flex-shrink: 0;
}

.image-gallery {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 440px;
  object-fit: cover;
  display: block;
}

.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color var(--transition-fast);
}

.thumb.active {
  border-color: var(--color-primary);
}

.no-image {
  width: 100%;
  height: 400px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-icon-svg {
  color: var(--color-primary);
  opacity: 0.3;
}

.info-section {
  flex: 1;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  line-height: 1.4;
}

.price-row {
  margin-bottom: 16px;
}

.price {
  font-size: 30px;
  font-weight: 700;
  color: var(--price-red);
}

.original {
  font-size: 14px;
  color: var(--text-muted);
  text-decoration: line-through;
  margin-left: 10px;
}

.tags-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.seller-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  color: var(--text-primary);
}

.seller-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.stats-row {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.action-row {
  display: flex;
  gap: 10px;
}

.btn-buy {
  background: var(--price-red);
  color: #fff;
  padding: 12px 28px;
  border-radius: var(--radius-pill);
  font-size: 15px;
  font-weight: 600;
  transition: opacity var(--transition-fast);
}
.btn-buy:hover { opacity: 0.9; }

.btn-fav {
  background: #f5f5f5;
  color: var(--text-primary);
  padding: 12px 20px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  transition: all var(--transition-fast);
}
.btn-fav:hover { background: #eee; }
.btn-fav.favored { background: #fff8e1; color: #f9a825; }

.btn-chat {
  background: #f5f5f5;
  color: var(--text-primary);
  padding: 12px 20px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  transition: all var(--transition-fast);
}
.btn-chat:hover { background: #eee; }

.desc-card {
  padding: 28px 32px;
  margin-bottom: 32px;
}

.desc-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
}

.desc-body {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }
  .image-section {
    width: 100%;
  }
}

.review-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.review-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
}

.btn-report {
  background: #f5f5f5;
  color: var(--text-secondary);
  padding: 12px 20px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  transition: all var(--transition-fast);
}

.btn-report:hover:not(:disabled) {
  background: #fef0f0;
  color: #ef4444;
}

.btn-report.reported {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.seller-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 14px;
}

.seller-link:hover {
  text-decoration: underline;
}
</style>
