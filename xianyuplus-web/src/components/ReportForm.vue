<template>
  <div v-if="visible" class="report-modal" @click.self="close">
    <div class="report-form">
      <div class="form-header">
        <h3>举报商品</h3>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="form-body">
        <div class="reason-section">
          <label>举报原因</label>
          <div class="reason-options">
            <label
              v-for="option in reasonOptions"
              :key="option.value"
              class="reason-option"
              :class="{ active: reason === option.value }"
            >
              <input
                type="radio"
                :value="option.value"
                v-model="reason"
              >
              <span>{{ option.label }}</span>
            </label>
          </div>
        </div>

        <div class="desc-section">
          <label>补充说明（可选）</label>
          <textarea
            v-model="description"
            placeholder="请详细描述举报原因..."
            maxlength="500"
            rows="3"
          ></textarea>
          <span class="char-count">{{ description.length }}/500</span>
        </div>
      </div>

      <div class="form-footer">
        <button class="btn-cancel" @click="close">取消</button>
        <button class="btn-submit" :disabled="!reason || submitting" @click="submit">
          {{ submitting ? '提交中...' : '提交举报' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createReport } from '@/api/report'
import { Toast } from '@/utils/toast'

const props = defineProps({
  visible: Boolean,
  productId: Number
})

const emit = defineEmits(['close', 'success'])

const reason = ref(null)
const description = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 1, label: '虚假信息' },
  { value: 2, label: '违禁品' },
  { value: 3, label: '价格异常' },
  { value: 4, label: '恶意欺诈' },
  { value: 5, label: '其他' }
]

function close() {
  emit('close')
  reason.value = null
  description.value = ''
}

async function submit() {
  if (!reason.value) {
    Toast.warning('请选择举报原因')
    return
  }

  submitting.value = true
  try {
    await createReport(props.productId, reason.value, description.value)
    Toast.success('举报已提交，我们会尽快处理')
    emit('success')
    close()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-modal {
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

.report-form {
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

.reason-section {
  margin-bottom: 20px;
}

.reason-section label,
.desc-section label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.reason-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.reason-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.reason-option:hover {
  border-color: var(--color-primary);
}

.reason-option.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.reason-option input {
  display: none;
}

.desc-section {
  position: relative;
}

.desc-section textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.desc-section textarea:focus {
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
  background: var(--color-destructive);
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
