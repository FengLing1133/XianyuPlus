<template>
  <div class="publish-page">
    <h2 class="page-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>

    <div class="card publish-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label">商品标题 <span class="required">*</span></label>
          <input v-model="form.title" class="form-input" placeholder="请输入商品标题" maxlength="50" />
          <span v-if="errors.title" class="form-error">{{ errors.title }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">商品分类 <span class="required">*</span></label>
          <select v-model="form.categoryId" class="form-input form-select">
            <option :value="null" disabled>请选择分类</option>
            <optgroup v-for="cat in categoryOptions" :key="cat.id" :label="cat.name">
              <option :value="cat.id">{{ cat.name }}</option>
              <option v-for="child in cat.children" :key="child.id" :value="child.id">&nbsp;&nbsp;{{ child.name }}</option>
            </optgroup>
          </select>
          <span v-if="errors.categoryId" class="form-error">{{ errors.categoryId }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">成色 <span class="required">*</span></label>
          <select v-model="form.condition" class="form-input form-select">
            <option :value="null" disabled>请选择成色</option>
            <option :value="1">全新</option>
            <option :value="2">几乎全新</option>
            <option :value="3">轻微使用</option>
            <option :value="4">明显痕迹</option>
          </select>
          <span v-if="errors.condition" class="form-error">{{ errors.condition }}</span>
        </div>

        <div class="form-row">
          <div class="form-group" style="flex:1;">
            <label class="form-label">售价 <span class="required">*</span></label>
            <div class="input-with-unit">
              <input v-model.number="form.price" type="number" class="form-input" placeholder="0.00" min="0" step="0.01" />
              <span class="unit">元</span>
            </div>
            <span v-if="errors.price" class="form-error">{{ errors.price }}</span>
          </div>
          <div class="form-group" style="flex:1;">
            <label class="form-label">原价</label>
            <div class="input-with-unit">
              <input v-model.number="form.originalPrice" type="number" class="form-input" placeholder="选填" min="0" step="0.01" />
              <span class="unit">元</span>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">商品描述</label>
          <textarea v-model="form.description" class="form-input" rows="5" placeholder="描述商品的使用情况、购买时间等..."></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">商品图片</label>
          <ImageUploader v-model="form.images" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-pill btn-pill-primary submit-btn" :disabled="submitting">
            <span v-if="submitting" class="spinner"></span>
            <span v-else>{{ isEdit ? '保存修改' : '立即发布' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { Toast } from '@/utils/toast'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const categoryOptions = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  categoryId: null,
  condition: null,
  price: null,
  originalPrice: null,
  description: '',
  images: []
})

const errors = reactive({
  title: '',
  categoryId: '',
  condition: '',
  price: ''
})

function validate() {
  errors.title = ''
  errors.categoryId = ''
  errors.condition = ''
  errors.price = ''

  if (!form.title.trim()) { errors.title = '请输入商品标题'; return false }
  if (!form.categoryId) { errors.categoryId = '请选择商品分类'; return false }
  if (!form.condition) { errors.condition = '请选择成色'; return false }
  if (!form.price && form.price !== 0) { errors.price = '请输入售价'; return false }
  if (form.price < 0) { errors.price = '售价不能为负数'; return false }
  return true
}

onMounted(async () => {
  try {
    const res = await request.get('/category')
    categoryOptions.value = res.data
  } catch {}

  if (isEdit.value) {
    try {
      const res = await request.get(`/product/${route.params.id}`)
      const p = res.data
      form.title = p.title
      form.categoryId = p.categoryId
      form.condition = p.condition
      form.price = p.price
      form.originalPrice = p.originalPrice
      form.description = p.description || ''
      form.images = (p.images || []).map(img => img.url)
    } catch {}
  }
})

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/product/${route.params.id}`, form)
      Toast.success('修改成功')
    } else {
      await request.post('/product', form)
      Toast.success('发布成功')
    }
    router.push('/')
  } catch {} finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-page {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.publish-card {
  padding: 28px 32px;
  max-width: 700px;
  margin: 0 auto;
}

.required {
  color: var(--price-red);
}

.form-error {
  color: var(--price-red);
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23999' d='M6 8L2 4h8z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
  cursor: pointer;
}

.form-row {
  display: flex;
  gap: 16px;
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 8px;
}
.input-with-unit .form-input {
  flex: 1;
}
.unit {
  color: var(--text-secondary);
  font-size: 14px;
  flex-shrink: 0;
}

.form-actions {
  margin-top: 24px;
}

.submit-btn {
  padding: 12px 36px;
  font-size: 15px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
