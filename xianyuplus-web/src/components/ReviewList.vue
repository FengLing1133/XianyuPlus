<template>
  <div class="review-list">
    <!-- 评分汇总 -->
    <div class="review-summary" v-if="showSummary">
      <div class="summary-left">
        <div class="avg-rating">{{ stats.avgRating || 0 }}</div>
        <div class="rating-stars">
          <span v-for="i in 5" :key="i">{{ i <= Math.round(stats.avgRating || 0) ? '★' : '☆' }}</span>
        </div>
        <div class="review-count">{{ stats.reviewCount || 0 }} 条评价</div>
      </div>
      <div class="summary-right">
        <div v-for="(count, index) in (stats.ratingDistribution || [0,0,0,0,0])" :key="index" class="rating-bar">
          <span class="bar-label">{{ index + 1 }}星</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: getBarWidth(count) + '%' }"></div>
          </div>
          <span class="bar-count">{{ count }}</span>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <div class="reviewer-info">
            <div class="reviewer-avatar">{{ getInitial(review.buyerNickname) }}</div>
            <div>
              <div class="reviewer-name">{{ review.buyerNickname || '匿名用户' }}</div>
              <div class="review-rating">
                <span v-for="i in 5" :key="i" class="star-small" :class="{ 'star-filled': i <= review.rating }">{{ i <= review.rating ? '★' : '☆' }}</span>
                <span class="review-time">{{ formatTime(review.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="review-content" v-if="review.content">
          {{ review.content }}
        </div>

        <!-- 卖家回复 -->
        <div class="seller-reply" v-if="review.sellerReply">
          <div class="reply-header">
            <div class="reply-label">卖家回复：</div>
            <!-- 卖家删除回复按钮 -->
            <button v-if="showDeleteButton && String(userId) === String(review.sellerId)" class="btn-delete-reply" @click="handleDeleteReply(review)">删除回复</button>
          </div>
          <div class="reply-content">{{ review.sellerReply }}</div>
          <div class="reply-time">{{ formatTime(review.replyAt) }}</div>
        </div>

        <!-- 操作按钮 -->
        <div class="review-actions">
          <!-- 回复按钮（卖家视角） -->
          <button v-if="showReplyButton && !review.sellerReply" class="btn-reply" @click="startReply(review)">回复</button>
          <!-- 删除按钮（买家删除自己的评价） -->
          <button v-if="showDeleteButton && String(userId) === String(review.buyerId)" class="btn-delete" @click="handleDelete(review)">删除</button>
        </div>
      </div>

      <div v-if="reviews.length === 0" class="no-reviews">
        暂无评价
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
      <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
      <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
    </div>

    <!-- 回复弹窗 -->
    <div v-if="replyingReview" class="reply-modal" @click.self="cancelReply">
      <div class="reply-form">
        <h4>回复评价</h4>
        <textarea v-model="replyContent" placeholder="输入回复内容..." maxlength="500" rows="3"></textarea>
        <div class="reply-actions">
          <button @click="cancelReply">取消</button>
          <button @click="submitReply" :disabled="!replyContent.trim()">提交回复</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getProductReviews, getSellerReviews, getSellerStats, replyReview, deleteReview, deleteReply } from '@/api/review'
import { Toast } from '@/utils/toast'
import { Dialog } from '@/utils/dialog'

const props = defineProps({
  productId: Number,
  sellerId: Number,
  userId: Number,
  showSummary: { type: Boolean, default: true },
  showReplyButton: { type: Boolean, default: false },
  showDeleteButton: { type: Boolean, default: false },
  pageSize: { type: Number, default: 10 }
})

const reviews = ref([])
const stats = ref({})
const total = ref(0)
const currentPage = ref(1)
const replyingReview = ref(null)
const replyContent = ref('')

async function fetchReviews() {
  const res = props.productId
    ? await getProductReviews(props.productId, currentPage.value, props.pageSize)
    : await getSellerReviews(props.sellerId, currentPage.value, props.pageSize)

  if (res.code === 200) {
    reviews.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

async function fetchStats() {
  if (!props.showSummary || !props.sellerId) return
  const res = await getSellerStats(props.sellerId)
  if (res.code === 200) {
    stats.value = res.data
  }
}

function getBarWidth(count) {
  const max = Math.max(...(stats.value.ratingDistribution || [1]))
  return max > 0 ? (count / max) * 100 : 0
}

function getInitial(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

function changePage(page) {
  currentPage.value = page
  fetchReviews()
}

function startReply(review) {
  replyingReview.value = review
  replyContent.value = ''
}

function cancelReply() {
  replyingReview.value = null
  replyContent.value = ''
}

async function submitReply() {
  if (!replyContent.value.trim()) return

  await replyReview(replyingReview.value.id, replyContent.value)
  Toast.success('回复成功')
  cancelReply()
  fetchReviews()
}

async function handleDelete(review) {
  const ok = await Dialog.confirm({
    title: '删除评价',
    message: '确定要删除这条评价吗？'
  })
  if (!ok) return

  await deleteReview(review.id)
  Toast.success('删除成功')
  fetchReviews()
  fetchStats()
}

async function handleDeleteReply(review) {
  const ok = await Dialog.confirm({
    title: '删除回复',
    message: '确定要删除这条回复吗？'
  })
  if (!ok) return

  await deleteReply(review.id)
  Toast.success('删除成功')
  fetchReviews()
}

onMounted(() => {
  fetchReviews()
  fetchStats()
})

watch(() => props.productId, () => {
  currentPage.value = 1
  fetchReviews()
})
</script>

<style scoped>
.review-summary {
  display: flex;
  gap: 40px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 24px;
}

.summary-left {
  text-align: center;
  min-width: 120px;
}

.avg-rating {
  font-size: 48px;
  font-weight: 700;
  color: #333;
}

.rating-stars {
  font-size: 20px;
  color: #ffc107;
}

.review-count {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.summary-right {
  flex: 1;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bar-label {
  font-size: 13px;
  color: #666;
  width: 30px;
}

.bar-track {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #ffc107;
  border-radius: 4px;
  transition: width 0.3s;
}

.bar-count {
  font-size: 13px;
  color: #666;
  width: 24px;
  text-align: right;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--green-500);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 500;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

.star-small {
  font-size: 14px;
  color: #ddd;
}
.star-small.star-filled {
  color: #ffc107;
}

.review-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
}

.seller-reply {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.reply-label {
  font-size: 13px;
  color: #666;
}

.btn-delete-reply {
  padding: 4px 12px;
  border: none;
  background: none;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  border-radius: 4px;
}

.btn-delete-reply:hover {
  background: #fff;
  color: #ff4d4f;
}

.reply-content {
  font-size: 14px;
  color: #333;
}

.reply-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.review-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.btn-reply {
  padding: 6px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-reply:hover {
  border-color: var(--green-500);
  color: var(--green-500);
}

.btn-delete {
  padding: 6px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #999;
}

.btn-delete:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
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
  border-color: var(--green-500);
  color: var(--green-500);
}

.reply-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.reply-form {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
  max-width: 90vw;
}

.reply-form h4 {
  margin: 0 0 16px;
}

.reply-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
}

.reply-form textarea:focus {
  outline: none;
  border-color: var(--green-500);
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.reply-actions button {
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.reply-actions button:first-child {
  border: 1px solid #ddd;
  background: #fff;
}

.reply-actions button:last-child {
  border: none;
  background: var(--green-500);
  color: #fff;
}

.reply-actions button:last-child:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
