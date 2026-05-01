<template>
  <div class="image-uploader">
    <div class="image-list">
      <div v-for="(url, index) in modelValue" :key="index" class="image-item">
        <img :src="url" class="preview-img" />
        <button class="remove-btn" @click="remove(index)" title="移除">✕</button>
      </div>
      <div v-if="modelValue.length < max" class="upload-box" @click="triggerUpload">
        <span class="upload-icon">+</span>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFile" />
      </div>
    </div>
    <div class="tip">最多上传{{ max }}张，建议尺寸 800x800</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Toast } from '@/utils/toast'
import request from '@/api/request'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 6 }
})
const emit = defineEmits(['update:modelValue'])
const fileInput = ref(null)

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/file/upload', formData)
    emit('update:modelValue', [...props.modelValue, res.data])
  } catch {
    // Toast already shown by axios interceptor
  } finally {
    fileInput.value.value = ''
  }
}

function remove(index) {
  const list = [...props.modelValue]
  list.splice(index, 1)
  emit('update:modelValue', list)
}
</script>

<style scoped>
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--price-red);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  line-height: 1;
}
.remove-btn:hover { background: #c62828; }

.upload-box {
  width: 100px;
  height: 100px;
  border: 2px dashed var(--border-light);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color var(--transition-fast);
}
.upload-box:hover {
  border-color: var(--color-primary);
}
.upload-icon {
  font-size: 32px;
  color: var(--text-muted);
}
.upload-box:hover .upload-icon {
  color: var(--color-primary);
}

.tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}
</style>
