<template>
  <div class="order-page">
    <h2 class="page-title">我的订单</h2>

    <div class="card">
      <!-- Tabs -->
      <div class="order-tabs">
        <button :class="['tab-item', { active: type === 'buy' }]" @click="switchTab('buy')">我买到的</button>
        <button :class="['tab-item', { active: type === 'sell' }]" @click="switchTab('sell')">我卖出的</button>
      </div>

      <div v-if="loading" style="padding: 40px; text-align: center; color: var(--text-secondary);">加载中...</div>
      <template v-else>
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-head">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span :class="['status-tag', 'status-' + order.status]">{{ statusText(order.status) }}</span>
          </div>
          <div class="order-body" @click="$router.push(`/product/${order.productId}`)">
            <div class="order-img-wrap" :style="{ background: colors[order.id % colors.length] }">
              <component :is="[Smartphone, BookOpen, ShoppingBag, Laptop, Gamepad2, Armchair][order.id % 6]" :size="28" class="order-placeholder-icon" />
              <img v-if="order.productImage" :src="order.productImage" class="order-img" @error="$event.target.style.display='none'" />
            </div>
            <div class="order-info">
              <div class="order-title">{{ order.productTitle }}</div>
              <div class="order-price">¥{{ order.amount }}</div>
            </div>
          </div>
          <div class="order-foot">
            <span class="order-contact">
              {{ type === 'buy' ? '卖家' : '买家' }}：
              <router-link v-if="type === 'buy'" :to="`/seller/${order.sellerId}`" class="seller-link">{{ order.sellerName || '卖家' }}</router-link>
              <router-link v-else :to="`/seller/${order.buyerId}`" class="seller-link">{{ order.buyerName || '买家' }}</router-link>
            </span>
            <span class="order-time">{{ order.createdAt?.substring(0, 16) }}</span>
            <div class="order-actions">
              <button v-if="type === 'buy' && order.status === 0" class="btn-pill btn-pill-primary" @click="payOrder(order)">确认付款</button>
              <button v-if="type === 'buy' && order.status === 0" class="btn-pill btn-danger" @click="cancelOrder(order)">取消</button>
              <button v-if="type === 'buy' && order.status === 1" class="btn-pill btn-danger" @click="cancelOrder(order)">申请退款</button>
              <button v-if="type === 'buy' && order.status === 2" class="btn-pill btn-pill-primary" @click="receiveOrder(order)">确认收货</button>
              <button v-if="type === 'buy' && order.status === 4 && canReviewMap[order.id]" class="btn-pill btn-pill-primary" @click="openReview(order)">去评价</button>
              <button v-if="type === 'sell' && order.status === 1" class="btn-pill" style="background:var(--color-background);color:var(--color-primary);" @click="shipOrder(order)">确认发货</button>
            </div>
          </div>
        </div>

        <div v-if="orders.length === 0" class="empty-state">
          <div class="empty-icon"><ShoppingBag :size="48" /></div>
          <p>暂无订单</p>
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

    <!-- 评价表单 -->
    <ReviewForm
      :visible="showReviewForm"
      :order-id="reviewOrderId"
      @close="showReviewForm = false"
      @success="handleReviewSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import { Toast } from '@/utils/toast'
import { Dialog } from '@/utils/dialog'
import ReviewForm from '@/components/ReviewForm.vue'
import { checkCanReview } from '@/api/review'
import { ShoppingBag, Smartphone, BookOpen, Laptop, Gamepad2, Armchair } from 'lucide-vue-next'

const type = ref('buy')
const orders = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const colors = ['#fce4ec', '#e3f2fd', '#FDF2F8', '#f3e5f5', '#fff9c4', '#fff3e0']
const showReviewForm = ref(false)
const reviewOrderId = ref(null)
const canReviewMap = ref({})

const totalPages = computed(() => Math.ceil(total.value / 10))

function statusText(s) {
  const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已完成', 5: '已取消' }
  return map[s] || '未知'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/order', { params: { type: type.value, page: page.value, size: 10 } })
    orders.value = res.data.records
    total.value = res.data.total

    // 检查已完成订单是否可评价
    for (const order of orders.value) {
      if (order.status === 4 && type.value === 'buy') {
        const res = await checkCanReview(order.id)
        if (res.code === 200) {
          canReviewMap.value[order.id] = res.data
        }
      }
    }
  } finally {
    loading.value = false
  }
}

function switchTab(t) {
  type.value = t
  page.value = 1
  fetchData()
}

function goPage(p) {
  page.value = p
  fetchData()
}

async function cancelOrder(order) {
  const msg = order.status === 0 ? '确认取消此订单？' : '确认申请退款？取消后将恢复商品为在售状态。'
  const ok = await Dialog.confirm({ title: '取消订单', message: msg })
  if (!ok) return
  await request.put(`/order/${order.id}/status`, null, { params: { status: 5 } })
  Toast.success('订单已取消')
  fetchData()
}

async function payOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 1 } })
  Toast.success('已确认付款，等待卖家发货')
  fetchData()
}

async function shipOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 2 } })
  Toast.success('已确认发货')
  fetchData()
}

async function receiveOrder(order) {
  await request.put(`/order/${order.id}/status`, null, { params: { status: 3 } })
  Toast.success('已确认收货，订单完成')
  fetchData()
}

function openReview(order) {
  reviewOrderId.value = order.id
  showReviewForm.value = true
}

function handleReviewSuccess() {
  canReviewMap.value[reviewOrderId.value] = false
  fetchData()
}
</script>

<style scoped>
.order-page {
  max-width: 800px;
  margin: 0 auto;
}

.order-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-lighter);
}

.tab-item {
  padding: 12px 24px;
  background: none;
  font-size: 14px;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  transition: all var(--transition-fast);
}
.tab-item:hover { color: var(--text-primary); }
.tab-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 500;
}

.order-item {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-lighter);
}
.order-item:last-child { border-bottom: none; }

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-id { font-size: 13px; color: var(--text-secondary); }

.status-tag {
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 500;
}
.status-0 { background: #f0f4ff; color: #5b7fff; }
.status-1 { background: #fff3e0; color: #e65100; }
.status-2 { background: #e8f4fd; color: #0277bd; }
.status-3 { background: #e0f7fa; color: #00838f; }
.status-4 { background: var(--color-background); color: var(--color-primary); }
.status-5 { background: #fef0f0; color: var(--price-red); }

.order-body {
  display: flex;
  gap: 14px;
  cursor: pointer;
  margin-bottom: 12px;
}

.order-img-wrap {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.order-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.order-placeholder-icon { color: var(--color-primary); opacity: 0.4; }

.order-info { flex: 1; }
.order-title { font-size: 15px; color: var(--text-primary); margin-bottom: 6px; }
.order-price { font-size: 18px; font-weight: 700; color: var(--price-red); }

.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid var(--border-lighter);
  font-size: 13px;
}
.order-contact { color: var(--text-secondary); }
.order-time { color: var(--text-muted); }

.btn-danger {
  background: #fef0f0;
  color: var(--price-red);
  padding: 6px 16px;
  border-radius: var(--radius-pill);
  font-size: 13px;
}
.btn-danger:hover { background: #fde2e2; }

.seller-link {
  color: var(--color-primary);
  text-decoration: none;
}

.seller-link:hover {
  text-decoration: underline;
}
</style>
