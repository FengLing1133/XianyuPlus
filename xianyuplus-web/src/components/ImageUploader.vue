<template>
  <div class="image-uploader">
    <div class="image-list">
      <div v-for="(url, index) in modelValue" :key="index" class="image-item">
        <img :src="url" class="preview-img" />
        <el-icon class="delete-icon" @click="remove(index)"><CircleClose /></el-icon>
      </div>
      <el-upload v-if="modelValue.length < max" :action="uploadUrl" :headers="headers"
        :show-file-list="false" :on-success="handleSuccess" class="upload-btn">
        <el-icon :size="28"><Plus /></el-icon>
      </el-upload>
    </div>
    <div class="tip">最多上传{{ max }}张，建议尺寸 800x800</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Plus, CircleClose } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 6 }
})
const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/file/upload'
const headers = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

function handleSuccess(response) {
  if (response.code === 200) {
    emit('update:modelValue', [...props.modelValue, response.data])
  }
}

function remove(index) {
  const list = [...props.modelValue]
  list.splice(index, 1)
  emit('update:modelValue', list)
}
</script>

<style scoped>
.image-list { display: flex; flex-wrap: wrap; gap: 10px; }
.image-item { position: relative; width: 100px; height: 100px; }
.preview-img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.delete-icon { position: absolute; top: -6px; right: -6px; color: #f56c6c; cursor: pointer; font-size: 18px; }
.upload-btn { width: 100px; height: 100px; border: 1px dashed #dcdfe6; border-radius: 6px; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.upload-btn:hover { border-color: #409eff; }
.tip { font-size: 12px; color: #909399; margin-top: 6px; }
</style>
