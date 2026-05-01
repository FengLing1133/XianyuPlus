# 商品评价系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现商品评价系统，买家完成订单后可评分+评论，卖家可回复评价。

**Architecture:** 新增 review 表存储评价，后端提供 CRUD API，前端在商品详情页展示评价列表，订单页添加评价入口。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3

---

## 文件结构

### 后端文件

| 文件 | 职责 |
|------|------|
| `common/.../entity/Review.java` | 评价实体类 |
| `common/.../mapper/ReviewMapper.java` | MyBatis-Plus Mapper |
| `service/.../controller/ReviewController.java` | 评价 API 控制器 |
| `service/.../service/ReviewService.java` | 评价服务接口 |
| `service/.../service/impl/ReviewServiceImpl.java` | 评价服务实现 |

### 前端文件

| 文件 | 职责 |
|------|------|
| `src/components/ReviewForm.vue` | 评价表单弹窗组件 |
| `src/components/ReviewList.vue` | 评价列表组件 |
| `src/api/review.js` | 评价 API 封装 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `init.sql` | 添加 review 表 |
| `ProductDetail.vue` | 集成 ReviewList 组件 |
| `Order.vue` | 添加"去评价"按钮 |

---

## Task 1: 数据库 — 创建 review 表

**Files:**
- Modify: `xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 1: 添加 review 表定义**

在 `notification` 表之后添加：

```sql
-- --------------------------------------------
-- 评价表
-- --------------------------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '关联订单',
    `product_id` BIGINT NOT NULL COMMENT '关联商品',
    `buyer_id` BIGINT NOT NULL COMMENT '评价者(买家)',
    `seller_id` BIGINT NOT NULL COMMENT '被评价者(卖家)',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容(可选)',
    `seller_reply` VARCHAR(500) DEFAULT NULL COMMENT '卖家回复',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `reply_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';
```

- [ ] **Step 2: 执行数据库更新**

Run: `mysql -u root -p xianyu_plus < xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/resources/db/init.sql
git commit -m "feat(db): 添加 review 评价表"
```

---

## Task 2: 后端 — 创建 Review 实体和 Mapper

**Files:**
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Review.java`
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ReviewMapper.java`

- [ ] **Step 1: 创建 Review 实体类**

```java
package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private Integer rating;
    private String content;
    private String sellerReply;
    private LocalDateTime createdAt;
    private LocalDateTime replyAt;
}
```

- [ ] **Step 2: 创建 ReviewMapper 接口**

```java
package com.xianyuplus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyuplus.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl common`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Review.java
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ReviewMapper.java
git commit -m "feat: 添加 Review 实体和 Mapper"
```

---

## Task 3: 后端 — 创建 ReviewService

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ReviewService.java`
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ReviewServiceImpl.java`

- [ ] **Step 1: 创建 ReviewService 接口**

```java
package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

import java.math.BigDecimal;

public interface ReviewService {
    Result<?> create(Long orderId, Integer rating, String content);
    Result<?> reply(Long reviewId, String reply);
    Result<?> getByProduct(Long productId, Integer page, Integer size);
    Result<?> getBySeller(Long sellerId, Integer page, Integer size);
    Result<?> getSellerStats(Long sellerId);
    Result<?> checkCanReview(Long orderId);
}
```

- [ ] **Step 2: 创建 ReviewServiceImpl 实现类**

```java
package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.entity.Review;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.OrderStatus;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.mapper.ReviewMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Result<?> create(Long orderId, Integer rating, String content) {
        Long userId = getCurrentUserId();

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 验证是否是买家
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("只有买家可以评价");
        }

        // 验证订单状态是否为已完成
        if (!order.getStatus().equals(OrderStatus.COMPLETED.getCode())) {
            return Result.error("只能评价已完成的订单");
        }

        // 验证是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        Long count = reviewMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("该订单已评价");
        }

        // 验证评分范围
        if (rating < 1 || rating > 5) {
            return Result.error("评分必须在1-5之间");
        }

        // 创建评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(order.getProductId());
        review.setBuyerId(userId);
        review.setSellerId(order.getSellerId());
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.insert(review);

        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> reply(Long reviewId, String reply) {
        Long userId = getCurrentUserId();

        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }

        // 验证是否是卖家
        if (!review.getSellerId().equals(userId)) {
            return Result.error("只有卖家可以回复");
        }

        // 验证是否已回复
        if (review.getSellerReply() != null) {
            return Result.error("已回复过该评价");
        }

        review.setSellerReply(reply);
        review.setReplyAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        return Result.ok();
    }

    @Override
    public Result<?> getByProduct(Long productId, Integer page, Integer size) {
        Page<Review> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);

        // 补充买家昵称
        result.getRecords().forEach(review -> {
            User buyer = userMapper.selectById(review.getBuyerId());
            if (buyer != null) {
                review.setBuyerId(buyer.getId()); // 临时存储，前端需要
            }
        });

        return Result.ok(result);
    }

    @Override
    public Result<?> getBySeller(Long sellerId, Integer page, Integer size) {
        Page<Review> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSellerId, sellerId)
               .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);
        return Result.ok(result);
    }

    @Override
    public Result<?> getSellerStats(Long sellerId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSellerId, sellerId);

        java.util.List<Review> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("avgRating", 0);
            stats.put("reviewCount", 0);
            stats.put("ratingDistribution", new int[]{0, 0, 0, 0, 0});
            return Result.ok(stats);
        }

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        // 计算评分分布
        int[] distribution = new int[5];
        reviews.forEach(r -> {
            if (r.getRating() >= 1 && r.getRating() <= 5) {
                distribution[r.getRating() - 1]++;
            }
        });

        Map<String, Object> stats = new HashMap<>();
        stats.put("avgRating", BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        stats.put("reviewCount", reviews.size());
        stats.put("ratingDistribution", distribution);

        return Result.ok(stats);
    }

    @Override
    public Result<?> checkCanReview(Long orderId) {
        Long userId = getCurrentUserId();

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 验证是否是买家
        if (!order.getBuyerId().equals(userId)) {
            return Result.ok(false);
        }

        // 验证订单状态
        if (!order.getStatus().equals(OrderStatus.COMPLETED.getCode())) {
            return Result.ok(false);
        }

        // 验证是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        Long count = reviewMapper.selectCount(wrapper);

        return Result.ok(count == 0);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ReviewService.java
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ReviewServiceImpl.java
git commit -m "feat: 添加 ReviewService 服务层"
```

---

## Task 4: 后端 — 创建 ReviewController

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ReviewController.java`

- [ ] **Step 1: 创建 ReviewController**

```java
package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long orderId = Long.parseLong(body.get("orderId").toString());
        Integer rating = Integer.parseInt(body.get("rating").toString());
        String content = (String) body.get("content");
        return reviewService.create(orderId, rating, content);
    }

    @PutMapping("/{id}/reply")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return reviewService.reply(id, body.get("reply"));
    }

    @GetMapping("/product/{productId}")
    public Result<?> getByProduct(@PathVariable Long productId,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getByProduct(productId, page, size);
    }

    @GetMapping("/seller/{sellerId}")
    public Result<?> getBySeller(@PathVariable Long sellerId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getBySeller(sellerId, page, size);
    }

    @GetMapping("/seller/{sellerId}/stats")
    public Result<?> getSellerStats(@PathVariable Long sellerId) {
        return reviewService.getSellerStats(sellerId);
    }

    @GetMapping("/check/{orderId}")
    public Result<?> checkCanReview(@PathVariable Long orderId) {
        return reviewService.checkCanReview(orderId);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ReviewController.java
git commit -m "feat: 添加 ReviewController API 控制器"
```

---

## Task 5: 前端 — 创建评价 API 封装

**Files:**
- Create: `xianyuplus-web/src/api/review.js`

- [ ] **Step 1: 创建评价 API 文件**

```javascript
import request from './request'

export function createReview(orderId, rating, content) {
  return request.post('/review', { orderId, rating, content })
}

export function replyReview(id, reply) {
  return request.put(`/review/${id}/reply`, { reply })
}

export function getProductReviews(productId, page = 1, size = 10) {
  return request.get(`/review/product/${productId}`, { params: { page, size } })
}

export function getSellerReviews(sellerId, page = 1, size = 10) {
  return request.get(`/review/seller/${sellerId}`, { params: { page, size } })
}

export function getSellerStats(sellerId) {
  return request.get(`/review/seller/${sellerId}/stats`)
}

export function checkCanReview(orderId) {
  return request.get(`/review/check/${orderId}`)
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/api/review.js
git commit -m "feat: 添加评价 API 封装"
```

---

## Task 6: 前端 — 创建 ReviewForm 组件

**Files:**
- Create: `xianyuplus-web/src/components/ReviewForm.vue`

- [ ] **Step 1: 创建 ReviewForm 组件**

```vue
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
  border-color: var(--primary);
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
  background: var(--primary);
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
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/components/ReviewForm.vue
git commit -m "feat: 添加 ReviewForm 评价表单组件"
```

---

## Task 7: 前端 — 创建 ReviewList 组件

**Files:**
- Create: `xianyuplus-web/src/components/ReviewList.vue`

- [ ] **Step 1: 创建 ReviewList 组件**

```vue
<template>
  <div class="review-list">
    <!-- 评分汇总 -->
    <div class="review-summary" v-if="showSummary">
      <div class="summary-left">
        <div class="avg-rating">{{ stats.avgRating || 0 }}</div>
        <div class="rating-stars">
          <span v-for="i in 5" :key="i">{{ i <= Math.round(stats.avgRating || 0) ? '★' : '☆' }}</span>
        </div>
        <div class="review-count">{{ stats.reviewCount || 0 }} 条评价</div>
      </div>
      <div class="summary-right">
        <div v-for="(count, index) in (stats.ratingDistribution || [0,0,0,0,0])" :key="index" class="rating-bar">
          <span class="bar-label">{{ 5 - index }}星</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: getBarWidth(count) + '%' }"></div>
          </div>
          <span class="bar-count">{{ count }}</span>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <div class="reviewer-info">
            <div class="reviewer-avatar">{{ getInitial(review.buyerNickname) }}</div>
            <div>
              <div class="reviewer-name">{{ review.buyerNickname || '匿名用户' }}</div>
              <div class="review-rating">
                <span v-for="i in 5" :key="i" class="star-small">{{ i <= review.rating ? '★' : '☆' }}</span>
                <span class="review-time">{{ formatTime(review.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="review-content" v-if="review.content">
          {{ review.content }}
        </div>

        <!-- 卖家回复 -->
        <div class="seller-reply" v-if="review.sellerReply">
          <div class="reply-label">卖家回复：</div>
          <div class="reply-content">{{ review.sellerReply }}</div>
          <div class="reply-time">{{ formatTime(review.replyAt) }}</div>
        </div>

        <!-- 回复按钮（卖家视角） -->
        <div class="reply-action" v-if="showReplyButton && !review.sellerReply">
          <button class="btn-reply" @click="startReply(review)">回复</button>
        </div>
      </div>

      <div v-if="reviews.length === 0" class="no-reviews">
        暂无评价
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
      <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
      <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
    </div>

    <!-- 回复弹窗 -->
    <div v-if="replyingReview" class="reply-modal" @click.self="cancelReply">
      <div class="reply-form">
        <h4>回复评价</h4>
        <textarea v-model="replyContent" placeholder="输入回复内容..." maxlength="500" rows="3"></textarea>
        <div class="reply-actions">
          <button @click="cancelReply">取消</button>
          <button @click="submitReply" :disabled="!replyContent.trim()">提交回复</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getProductReviews, getSellerReviews, getSellerStats, replyReview } from '@/api/review'
import { Toast } from '@/utils/toast'

const props = defineProps({
  productId: Number,
  sellerId: Number,
  showSummary: { type: Boolean, default: true },
  showReplyButton: { type: Boolean, default: false },
  pageSize: { type: Number, default: 10 }
})

const reviews = ref([])
const stats = ref({})
const total = ref(0)
const currentPage = ref(1)
const replyingReview = ref(null)
const replyContent = ref('')

async function fetchReviews() {
  const res = props.productId
    ? await getProductReviews(props.productId, currentPage.value, props.pageSize)
    : await getSellerReviews(props.sellerId, currentPage.value, props.pageSize)

  if (res.code === 200) {
    reviews.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

async function fetchStats() {
  if (!props.showSummary || !props.sellerId) return
  const res = await getSellerStats(props.sellerId)
  if (res.code === 200) {
    stats.value = res.data
  }
}

function getBarWidth(count) {
  const max = Math.max(...(stats.value.ratingDistribution || [1]))
  return max > 0 ? (count / max) * 100 : 0
}

function getInitial(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

function changePage(page) {
  currentPage.value = page
  fetchReviews()
}

function startReply(review) {
  replyingReview.value = review
  replyContent.value = ''
}

function cancelReply() {
  replyingReview.value = null
  replyContent.value = ''
}

async function submitReply() {
  if (!replyContent.value.trim()) return

  await replyReview(replyingReview.value.id, replyContent.value)
  Toast.success('回复成功')
  cancelReply()
  fetchReviews()
}

onMounted(() => {
  fetchReviews()
  fetchStats()
})

watch(() => props.productId, () => {
  currentPage.value = 1
  fetchReviews()
})
</script>

<style scoped>
.review-summary {
  display: flex;
  gap: 40px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 24px;
}

.summary-left {
  text-align: center;
  min-width: 120px;
}

.avg-rating {
  font-size: 48px;
  font-weight: 700;
  color: #333;
}

.rating-stars {
  font-size: 20px;
  color: #ffc107;
}

.review-count {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.summary-right {
  flex: 1;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bar-label {
  font-size: 13px;
  color: #666;
  width: 30px;
}

.bar-track {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #ffc107;
  border-radius: 4px;
  transition: width 0.3s;
}

.bar-count {
  font-size: 13px;
  color: #666;
  width: 24px;
  text-align: right;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 500;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

.star-small {
  font-size: 14px;
  color: #ffc107;
}

.review-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
}

.seller-reply {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.reply-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 14px;
  color: #333;
}

.reply-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.reply-action {
  margin-top: 8px;
}

.btn-reply {
  padding: 6px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-reply:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
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

.reply-modal {
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

.reply-form {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
  max-width: 90vw;
}

.reply-form h4 {
  margin: 0 0 16px;
}

.reply-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
}

.reply-form textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.reply-actions button {
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.reply-actions button:first-child {
  border: 1px solid #ddd;
  background: #fff;
}

.reply-actions button:last-child {
  border: none;
  background: var(--primary);
  color: #fff;
}

.reply-actions button:last-child:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/components/ReviewList.vue
git commit -m "feat: 添加 ReviewList 评价列表组件"
```

---

## Task 8: 前端 — 集成到商品详情页

**Files:**
- Modify: `xianyuplus-web/src/views/ProductDetail.vue`

- [ ] **Step 1: 导入 ReviewList 组件**

在 `<script setup>` 中添加：

```javascript
import ReviewList from '@/components/ReviewList.vue'
```

- [ ] **Step 2: 添加评价区域**

在商品详情下方添加：

```html
<div class="review-section">
  <h3>商品评价</h3>
  <ReviewList :product-id="product.id" :seller-id="product.userId" />
</div>
```

- [ ] **Step 3: 添加样式**

```css
.review-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.review-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
}
```

- [ ] **Step 4: Commit**

```bash
git add xianyuplus-web/src/views/ProductDetail.vue
git commit -m "feat: 商品详情页集成评价列表"
```

---

## Task 9: 前端 — 订单页添加评价入口

**Files:**
- Modify: `xianyuplus-web/src/views/Order.vue`

- [ ] **Step 1: 导入组件**

```javascript
import ReviewForm from '@/components/ReviewForm.vue'
import { checkCanReview } from '@/api/review'
```

- [ ] **Step 2: 添加状态变量**

```javascript
const showReviewForm = ref(false)
const reviewOrderId = ref(null)
const canReviewMap = ref({})
```

- [ ] **Step 3: 检查订单是否可评价**

在 `fetchData` 方法中，对已完成订单检查是否可评价：

```javascript
// 在获取订单列表后
for (const order of orders.value) {
  if (order.status === 4 && type.value === 'buy') {
    const res = await checkCanReview(order.id)
    if (res.code === 200) {
      canReviewMap.value[order.id] = res.data
    }
  }
}
```

- [ ] **Step 4: 添加"去评价"按钮**

在订单操作区域添加：

```html
<button
  v-if="type === 'buy' && order.status === 4 && canReviewMap[order.id]"
  class="btn-pill btn-pill-primary"
  @click="openReview(order)"
>
  去评价
</button>
```

- [ ] **Step 5: 添加评价相关方法**

```javascript
function openReview(order) {
  reviewOrderId.value = order.id
  showReviewForm.value = true
}

function handleReviewSuccess() {
  canReviewMap.value[reviewOrderId.value] = false
  fetchData()
}
```

- [ ] **Step 6: 添加 ReviewForm 组件**

在模板底部添加：

```html
<ReviewForm
  :visible="showReviewForm"
  :order-id="reviewOrderId"
  @close="showReviewForm = false"
  @success="handleReviewSuccess"
/>
```

- [ ] **Step 7: Commit**

```bash
git add xianyuplus-web/src/views/Order.vue
git commit -m "feat: 订单页添加评价入口"
```

---

## Task 10: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端和前端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`
Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 2: 测试评价流程**

1. 用 test1 下单并完成订单（付款→发货→收货）
2. 在订单页点击"去评价"
3. 选择 5 星，输入评价内容
4. 提交评价
5. 验证评价显示在商品详情页

- [ ] **Step 3: 测试卖家回复**

1. 用 test1（卖家）登录
2. 在商品详情页找到评价
3. 点击"回复"，输入回复内容
4. 提交回复
5. 验证回复显示在评价下方

- [ ] **Step 4: 测试评分汇总**

1. 查看商品详情页的评分汇总
2. 验证平均评分和分布显示正确

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "test: 商品评价系统完整验证通过"
```

---

## 完成

商品评价系统实施完成。下一步：商品举报。
