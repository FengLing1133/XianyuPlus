<template>
  <div class="product-card card" @click="$router.push(`/product/${product.id}`)">
    <div class="card-image" :style="{ background: bgColor }">
      <img v-if="product.images?.[0]?.url && !imgError" :src="product.images[0].url" class="real-img" @error="imgError = true" />
      <span v-if="condition" class="condition-tag pill-tag pill-tag-green">{{ condition }}</span>
    </div>
    <div class="card-body">
      <div class="card-title" :title="product.title">{{ product.title }}</div>
      <div class="card-price-row">
        <span class="card-price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="card-original">¥{{ product.originalPrice }}</span>
      </div>
      <div class="card-footer">
        <span class="card-seller">👤 {{ product.sellerName || '匿名' }}</span>
        <span class="card-views">👁 {{ product.viewCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getProductColor, conditionText } from '@/utils/category'

const props = defineProps({
  product: { type: Object, required: true }
})

const imgError = ref(false)
watch(() => props.product?.id, () => { imgError.value = false })

const bgColor = computed(() => getProductColor(props.product.id))
const condition = computed(() => conditionText(props.product.condition))
</script>

<style scoped>
.product-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.card-image {
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.real-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.condition-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  font-size: 12px;
}

.card-body {
  padding: 14px 16px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.card-price-row {
  margin-bottom: 8px;
}

.card-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--price-red);
}

.card-original {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: line-through;
  margin-left: 6px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
