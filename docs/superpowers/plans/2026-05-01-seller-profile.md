# 卖家主页实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现卖家主页功能，展示卖家基本资料、信用评分、交易统计、在售商品列表。

**Architecture:** 后端新增卖家资料/统计/商品 API，前端新增 SellerProfile.vue 页面，从商品详情页、评价列表、订单页可跳转。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3

---

## 文件结构

### 后端文件

| 文件 | 职责 |
|------|------|
| `service/.../controller/UserController.java` | 扩展用户 API，新增卖家资料端点 |

### 前端文件

| 文件 | 职责 |
|------|------|
| `src/views/SellerProfile.vue` | 卖家主页页面 |
| `src/api/user.js` | 扩展用户 API 封装 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `router/index.js` | 添加卖家主页路由 |
| `ProductDetail.vue` | 卖家昵称可点击跳转 |
| `ReviewList.vue` | 评价列表中卖家昵称可点击 |
| `Order.vue` | 订单中对方昵称可点击 |

---

## Task 1: 后端 — 扩展 UserController

**Files:**
- Modify: `xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/UserController.java`

- [ ] **Step 1: 添加卖家资料端点**

```java
@GetMapping("/{id}/profile")
public Result<?> getSellerProfile(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        return Result.error("用户不存在");
    }

    Map<String, Object> profile = new HashMap<>();
    profile.put("id", user.getId());
    profile.put("nickname", user.getNickname());
    profile.put("avatar", user.getAvatar());
    profile.put("createdAt", user.getCreatedAt());

    // 获取评价统计
    LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
    reviewWrapper.eq(Review::getSellerId, id);
    List<Review> reviews = reviewMapper.selectList(reviewWrapper);

    double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0);

    profile.put("avgRating", BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
    profile.put("reviewCount", reviews.size());

    return Result.ok(profile);
}

@GetMapping("/{id}/stats")
public Result<?> getSellerStats(@PathVariable Long id) {
    // 已完成订单数
    LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
    orderWrapper.eq(Order::getSellerId, id)
                .eq(Order::getStatus, OrderStatus.COMPLETED.getCode());
    Long completedOrders = orderMapper.selectCount(orderWrapper);

    // 总销量
    LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
    productWrapper.eq(Product::getUserId, id)
                  .eq(Product::getStatus, ProductStatus.SOLD.getCode());
    Long totalSold = productMapper.selectCount(productWrapper);

    // 好评率
    LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
    reviewWrapper.eq(Review::getSellerId, id);
    List<Review> reviews = reviewMapper.selectList(reviewWrapper);

    long positiveCount = reviews.stream()
            .filter(r -> r.getRating() >= 4)
            .count();

    BigDecimal positiveRate = reviews.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(positiveCount * 100.0 / reviews.size())
                    .setScale(1, RoundingMode.HALF_UP);

    Map<String, Object> stats = new HashMap<>();
    stats.put("completedOrders", completedOrders);
    stats.put("totalSold", totalSold);
    stats.put("positiveRate", positiveRate);

    return Result.ok(stats);
}

@GetMapping("/{id}/products")
public Result<?> getSellerProducts(@PathVariable Long id,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "12") Integer size) {
    Page<Product> pageObj = new Page<>(page, size);
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Product::getUserId, id)
           .eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
           .orderByDesc(Product::getCreatedAt);
    Page<Product> result = productMapper.selectPage(pageObj, wrapper);

    // 转换为 DTO
    List<ProductDTO> records = result.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

    PageResult<ProductDTO> pageResult = new PageResult<>();
    pageResult.setTotal(result.getTotal());
    pageResult.setPage(page);
    pageResult.setSize(size);
    pageResult.setRecords(records);

    return Result.ok(pageResult);
}
```

- [ ] **Step 2: 确保导入必要的类**

```java
import com.xianyuplus.common.entity.Review;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.mapper.ReviewMapper;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.enums.OrderStatus;
import com.xianyuplus.common.enums.ProductStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
```

- [ ] **Step 3: 注入新的 Mapper**

```java
private final ReviewMapper reviewMapper;
private final OrderMapper orderMapper;
```

- [ ] **Step 4: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/UserController.java
git commit -m "feat: UserController 新增卖家资料/统计/商品 API"
```

---

## Task 2: 前端 — 扩展用户 API 封装

**Files:**
- Modify: `xianyuplus-web/src/api/user.js`

- [ ] **Step 1: 添加卖家相关 API**

```javascript
export function getSellerProfile(id) {
  return request.get(`/user/${id}/profile`)
}

export function getSellerStats(id) {
  return request.get(`/user/${id}/stats`)
}

export function getSellerProducts(id, page = 1, size = 12) {
  return request.get(`/user/${id}/products`, { params: { page, size } })
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/api/user.js
git commit -m "feat: 添加卖家资料 API 封装"
```

---

## Task 3: 前端 — 创建 SellerProfile 页面

**Files:**
- Create: `xianyuplus-web/src/views/SellerProfile.vue`

- [ ] **Step 1: 创建 SellerProfile.vue**

```vue
<template>
  <div class="seller-profile" v-if="seller">
    <!-- 卖家资料卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar">
          <img v-if="seller.avatar" :src="seller.avatar" alt="头像">
          <span v-else class="avatar-placeholder">{{ seller.nickname?.charAt(0) || '?' }}</span>
        </div>
        <div class="info">
          <h2 class="nickname">{{ seller.nickname || '未设置昵称' }}</h2>
          <div class="join-time">注册于 {{ formatDate(seller.createdAt) }}</div>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">
            <span class="star-icon">★</span>
            {{ seller.avgRating || 0 }}
          </div>
          <div class="stat-label">信用评分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.completedOrders || 0 }}</div>
          <div class="stat-label">完成交易</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.positiveRate || 0 }}%</div>
          <div class="stat-label">好评率</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ seller.reviewCount || 0 }}</div>
          <div class="stat-label">评价数</div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-section">
      <h3>TA的商品 ({{ total }})</h3>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="products.length === 0" class="empty">暂无在售商品</div>
      <div v-else class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 12" class="pagination">
        <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / 12) }}</span>
        <button :disabled="page >= Math.ceil(total / 12)" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="loading-page">加载中...</div>
  <div v-else class="error-page">卖家不存在</div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getSellerProfile, getSellerStats, getSellerProducts } from '@/api/user'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const sellerId = ref(route.params.id)

const seller = ref(null)
const stats = ref({})
const products = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(true)

async function fetchSeller() {
  loading.value = true
  try {
    const [profileRes, statsRes] = await Promise.all([
      getSellerProfile(sellerId.value),
      getSellerStats(sellerId.value)
    ])

    if (profileRes.code === 200) {
      seller.value = profileRes.data
    }
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }

    await fetchProducts()
  } finally {
    loading.value = false
  }
}

async function fetchProducts() {
  const res = await getSellerProducts(sellerId.value, page.value, 12)
  if (res.code === 200) {
    products.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function goPage(p) {
  page.value = p
  fetchProducts()
  window.scrollTo(0, 0)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(fetchSeller)

watch(() => route.params.id, (newId) => {
  if (newId) {
    sellerId.value = newId
    page.value = 1
    fetchSeller()
  }
})
</script>

<style scoped>
.seller-profile {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 36px;
  font-weight: 600;
  color: var(--primary);
}

.info {
  flex: 1;
}

.nickname {
  margin: 0 0 8px;
  font-size: 24px;
}

.join-time {
  font-size: 14px;
  color: #666;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.star-icon {
  color: #ffc107;
  font-size: 24px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.products-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.products-section h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px 0 0;
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
  border-color: var(--primary);
  color: var(--primary);
}

.loading-page,
.error-page {
  text-align: center;
  padding: 80px 20px;
  color: #666;
  font-size: 16px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/views/SellerProfile.vue
git commit -m "feat: 添加卖家主页页面"
```

---

## Task 4: 前端 — 添加路由

**Files:**
- Modify: `xianyuplus-web/src/router/index.js`

- [ ] **Step 1: 添加卖家主页路由**

在路由配置中添加：

```javascript
{
  path: '/seller/:id',
  name: 'SellerProfile',
  component: () => import('@/views/SellerProfile.vue'),
  meta: { title: '卖家主页' }
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/router/index.js
git commit -m "feat: 添加卖家主页路由"
```

---

## Task 5: 前端 — 添加跳转入口

**Files:**
- Modify: `xianyuplus-web/src/views/ProductDetail.vue`
- Modify: `xianyuplus-web/src/components/ReviewList.vue`
- Modify: `xianyuplus-web/src/views/Order.vue`

- [ ] **Step 1: 商品详情页卖家昵称可点击**

在 ProductDetail.vue 中，找到显示卖家昵称的地方，改为：

```html
<router-link :to="`/seller/${product.userId}`" class="seller-link">
  {{ product.userNickname || '卖家' }}
</router-link>
```

添加样式：

```css
.seller-link {
  color: var(--primary);
  text-decoration: none;
}

.seller-link:hover {
  text-decoration: underline;
}
```

- [ ] **Step 2: 评价列表中卖家昵称可点击**

在 ReviewList.vue 中，如果有显示卖家昵称的地方，添加跳转：

```html
<router-link v-if="review.sellerId" :to="`/seller/${review.sellerId}`" class="seller-link">
  {{ review.sellerNickname || '卖家' }}
</router-link>
```

- [ ] **Step 3: 订单页对方昵称可点击**

在 Order.vue 中，找到显示对方昵称的地方，改为：

```html
<router-link :to="`/seller/${order.sellerId}`" v-if="type === 'buy'" class="seller-link">
  {{ order.sellerNickname || '卖家' }}
</router-link>
<router-link :to="`/seller/${order.buyerId}`" v-else class="seller-link">
  {{ order.buyerNickname || '买家' }}
</router-link>
```

- [ ] **Step 4: Commit**

```bash
git add xianyuplus-web/src/views/ProductDetail.vue
git add xianyuplus-web/src/components/ReviewList.vue
git add xianyuplus-web/src/views/Order.vue
git commit -m "feat: 添加卖家主页跳转入口"
```

---

## Task 6: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端和前端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`
Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 2: 测试卖家主页**

1. 从商品详情页点击卖家昵称
2. 验证跳转到卖家主页
3. 验证显示卖家资料、信用评分、交易统计
4. 验证商品列表正确显示
5. 测试分页功能

- [ ] **Step 3: 测试其他入口**

1. 从评价列表点击卖家昵称
2. 从订单页点击对方昵称
3. 验证都能正确跳转

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "test: 卖家主页功能完整验证通过"
```

---

## 完成

卖家主页功能实施完成。下一步：浏览历史。
