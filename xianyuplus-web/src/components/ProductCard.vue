<template>
  <el-card shadow="hover" class="product-card" @click="$router.push(`/product/${product.id}`)">
    <img :src="product.images?.[0]?.url || defaultImg" class="product-img" />
    <div class="product-info">
      <div class="title">{{ product.title }}</div>
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <span class="original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
      </div>
      <div class="meta">{{ product.sellerName }} · {{ product.viewCount }}次浏览</div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  product: { type: Object, required: true }
})
const defaultImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjEwMCIgeT0iMTAwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgZmlsbD0iIzk5OSIgZm9udC1zaXplPSIxNiI+5Zu+54mH5Lyg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg=='
</script>

<style scoped>
.product-card { cursor: pointer; border-radius: 8px; overflow: hidden; }
.product-card:hover { transform: translateY(-2px); transition: transform 0.2s; }
.product-img { width: 100%; height: 180px; object-fit: cover; }
.product-info { padding: 10px 0; }
.title { font-size: 14px; color: #303133; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { margin-bottom: 6px; }
.price { font-size: 18px; font-weight: bold; color: #f56c6c; }
.original { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 6px; }
.meta { font-size: 12px; color: #909399; }
</style>
