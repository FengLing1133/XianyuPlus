<template>
  <div v-if="visible" class="review-modal" @click.self="close">
    <div class="review-form">
      <div class="form-header">
        <h3>评价订单</h3>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="form-body">
        <div class="rating-section">
          <label>评分</label>
          <div class="star-rating">
            <span
              v-for="i in 5"
              :key="i"
              class="star"
              :class="{ active: i <= rating }"
              @click="rating = i"
              @mouseenter="hoverRating = i"
              @mouseleave="hoverRating = 0"
            >
              {{ (hoverRating >= i || rating >= i) ? '★' : '☆' }}
            </span>
          </div>
          <span class="rating-text">{{ ratingText }}</span>
        </div>

        <div class="content-section">
          <label>评价内容（可选）</label>
          <textarea
            v-model="content"
            placeholder="分享你的购物体验..."
            maxlength="500"
            rows="4"
          ></textarea>
          <span class="char-count">{{ content.length }}/500</span>
        </div>
      </div>

      <div class="form-footer">
        <button class="btn-cancel" @click="close">取消</button>
        <button class="btn-submit" :disabled="rating === 0 || submitting" @click="submit">
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { createReview } from '@/api/review'
import { Toast } from '@/utils/toast'

const props = defineProps({
  visible: Boolean,
  orderId: Number
})

const emit = defineEmits(['close', 'success'])

const rating = ref(0)
const hoverRating = ref(0)
const content = ref('')
const submitting = ref(false)

const ratingText = computed(() => {
  const texts = ['', '很差', '较差', '一般', '不错', '很好']
  return texts[rating.value] || ''
})

function close() {
  emit('close')
  rating.value = 0
  content.value = ''
}

async function submit() {
  if (rating.value === 0) {
    Toast.warning('请选择评分')
    return
  }

  submitting.value = true
  try {
    await createReview(props.orderId, rating.value, content.value)
    Toast.success('评价成功')
    emit('success')
    close()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-modal {
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

.review-form {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.form-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.form-body {
  padding: 24px;
}

.rating-section {
  margin-bottom: 20px;
}

.rating-section label,
.content-section label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.star-rating {
  display: inline-flex;
  gap: 4px;
}

.star {
  font-size: 32px;
  cursor: pointer;
  color: #ddd;
  transition: color 0.2s;
}

.star.active {
  color: #ffc107;
}

.star:hover {
  color: #ffc107;
}

.rating-text {
  margin-left: 12px;
  font-size: 14px;
  color: #666;
}

.content-section {
  position: relative;
}

.content-section textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
}

.content-section textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.char-count {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 12px;
  color: #999;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel:hover {
  border-color: #999;
}

.btn-submit {
  padding: 10px 24px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
