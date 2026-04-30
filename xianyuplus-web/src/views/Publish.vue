<template>
  <div class="publish-page">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑商品' : '发布商品' }}</span>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="publish-form">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader v-model="form.categoryId" :options="categoryOptions" :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: false, emitPath: false }" placeholder="请选择分类" />
        </el-form-item>
        <el-form-item label="成色" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择成色">
            <el-option label="全新" :value="1" />
            <el-option label="几乎全新" :value="2" />
            <el-option label="轻微使用" :value="3" />
            <el-option label="明显痕迹" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" />
          <span class="unit">元</span>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" placeholder="选填" />
          <span class="unit">元</span>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="描述商品的使用情况、购买时间等" />
        </el-form-item>
        <el-form-item label="商品图片">
          <ImageUploader v-model="form.images" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
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

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

onMounted(async () => {
  const res = await request.get('/category')
  categoryOptions.value = res.data

  if (isEdit.value) {
    const productRes = await request.get(`/product/${route.params.id}`)
    const p = productRes.data
    form.title = p.title
    form.categoryId = p.categoryId
    form.condition = p.condition
    form.price = p.price
    form.originalPrice = p.originalPrice
    form.description = p.description || ''
    form.images = (p.images || []).map(img => img.url)
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/product/${route.params.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/product', form)
      ElMessage.success('发布成功')
    }
    router.push('/')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-page { max-width: 700px; margin: 0 auto; }
.publish-form { margin-top: 10px; }
.unit { margin-left: 8px; color: #909399; }
</style>
