<template>
  <div class="product-card card" @click="$router.push(`/product/${product.id}`)">
    <div class="card-image" :style="{ background: bgColor }">
      <img v-if="product.images?.[0]?.url && !imgError" :src="product.images[0].url" class="real-img" @error="imgError = true" />
      <component v-else :is="placeholderIcon" :size="36" class="placeholder-icon" />
      <span v-if="condition" class="condition-tag" :class="conditionClass">{{ condition }}</span>
    </div>
    <div class="card-body">
      <div class="card-title" :title="product.title">{{ product.title }}</div>
      <div class="card-price-row">
        <span class="card-price">¥{{ product.price }}</span>
      </div>
      <div class="card-footer">
        <div class="card-seller">
          <span class="seller-avatar-placeholder"></span>
          <span class="seller-name">{{ product.sellerName || '匿名' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Smartphone, BookOpen, ShoppingBag, Laptop, Gamepad2, Armchair, Watch, Headphones, Camera, Gift, Package } from 'lucide-vue-next'

const props = defineProps({
  product: { type: Object, required: true }
})

const imgError = ref(false)
watch(() => props.product?.id, () => { imgError.value = false })

const placeholderIcons = [Smartphone, BookOpen, ShoppingBag, Laptop, Gamepad2, Armchair, Watch, Headphones, Camera, Gift, Package]

const placeholderIcon = computed(() => {
  const id = props.product.id
  if (typeof id === 'string') {
    let hash = 0
    for (let i = 0; i < id.length; i++) {
      hash = id.charCodeAt(i) + ((hash << 5) - hash)
    }
    return placeholderIcons[Math.abs(hash) % placeholderIcons.length]
  }
  return placeholderIcons[id % placeholderIcons.length]
})

const placeholderColors = [
  'var(--placeholder-pink)', 'var(--placeholder-cyan)', 'var(--placeholder-orange)',
  'var(--placeholder-purple)', 'var(--placeholder-green)', 'var(--placeholder-red)',
  'var(--placeholder-blue)', 'var(--placeholder-yellow)'
]

const bgColor = computed(() => {
  const id = props.product.id
  if (typeof id === 'string') {
    let hash = 0
    for (let i = 0; i < id.length; i++) {
      hash = id.charCodeAt(i) + ((hash << 5) - hash)
    }
    return placeholderColors[Math.abs(hash) % placeholderColors.length]
  }
  return placeholderColors[id % placeholderColors.length]
})

const conditionTextMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显痕迹' }
const condition = computed(() => conditionTextMap[props.product.condition] || '')
const conditionClass = computed(() => {
  const map = { 1: 'condition-new', 2: 'condition-like-new', 3: 'condition-lightly-used', 4: 'condition-visible-wear' }
  return map[props.product.condition] || ''
})
</script>

<style scoped>
.product-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.card-image {
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.real-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-icon {
  color: var(--color-primary);
  opacity: 0.3;
}

.condition-tag {
  position: absolute;
  top: 7px;
  right: 7px;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 9px;
  font-weight: 500;
  color: #fff;
}

.condition-new { background: var(--condition-new); }
.condition-like-new { background: var(--condition-like-new); }
.condition-lightly-used { background: var(--condition-lightly-used); }
.condition-visible-wear { background: var(--condition-visible-wear); }

.card-body {
  padding: 10px 12px;
}

.card-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.card-price-row {
  margin-bottom: 8px;
}

.card-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-destructive);
}

.card-footer {
  display: flex;
  align-items: center;
}

.card-seller {
  display: flex;
  align-items: center;
  gap: 5px;
}

.seller-avatar-placeholder {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--gradient-primary);
}

.seller-name {
  font-size: 11px;
  color: #6B7280;
}
</style>
