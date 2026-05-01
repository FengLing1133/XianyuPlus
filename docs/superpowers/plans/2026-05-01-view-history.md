# 浏览历史实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现浏览历史功能，自动记录用户浏览过的商品，按日期分组展示，支持清空。

**Architecture:** 新增 view_history 表存储浏览记录，后端提供 CRUD API，前端新增 ViewHistory.vue 页面，商品详情页自动调用记录接口。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3

---

## 文件结构

### 后端文件

| 文件 | 职责 |
|------|------|
| `common/.../entity/ViewHistory.java` | 浏览历史实体类 |
| `common/.../mapper/ViewHistoryMapper.java` | MyBatis-Plus Mapper |
| `service/.../controller/ViewHistoryController.java` | 浏览历史 API 控制器 |
| `service/.../service/ViewHistoryService.java` | 浏览历史服务接口 |
| `service/.../service/impl/ViewHistoryServiceImpl.java` | 浏览历史服务实现 |

### 前端文件

| 文件 | 职责 |
|------|------|
| `src/views/ViewHistory.vue` | 浏览历史页面 |
| `src/api/viewHistory.js` | 浏览历史 API 封装 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `init.sql` | 添加 view_history 表 |
| `router/index.js` | 添加浏览历史路由 |
| `ProductDetail.vue` | 自动调用记录接口 |
| `Profile.vue` | 添加浏览历史入口 |

---

## Task 1: 数据库 — 创建 view_history 表

**Files:**
- Modify: `xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 1: 添加 view_history 表定义**

在 `report` 表之后添加：

```sql
-- --------------------------------------------
-- 浏览历史表
-- --------------------------------------------
DROP TABLE IF EXISTS `view_history`;
CREATE TABLE `view_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `created_at`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';
```

- [ ] **Step 2: 执行数据库更新**

Run: `mysql -u root -p xianyu_plus < xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/resources/db/init.sql
git commit -m "feat(db): 添加 view_history 浏览历史表"
```

---

## Task 2: 后端 — 创建 ViewHistory 实体和 Mapper

**Files:**
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/ViewHistory.java`
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ViewHistoryMapper.java`

- [ ] **Step 1: 创建 ViewHistory 实体类**

```java
package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("view_history")
public class ViewHistory {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建 ViewHistoryMapper 接口**

```java
package com.xianyuplus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyuplus.common.entity.ViewHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ViewHistoryMapper extends BaseMapper<ViewHistory> {
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl common`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/ViewHistory.java
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ViewHistoryMapper.java
git commit -m "feat: 添加 ViewHistory 实体和 Mapper"
```

---

## Task 3: 后端 — 创建 ViewHistoryService

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ViewHistoryService.java`
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ViewHistoryServiceImpl.java`

- [ ] **Step 1: 创建 ViewHistoryService 接口**

```java
package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface ViewHistoryService {
    Result<?> record(Long productId);
    Result<?> getList(Integer page, Integer size);
    Result<?> clear();
    Result<?> delete(Long id);
}
```

- [ ] **Step 2: 创建 ViewHistoryServiceImpl 实现类**

```java
package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.entity.ViewHistory;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.mapper.ViewHistoryMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ViewHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewHistoryServiceImpl implements ViewHistoryService {

    private final ViewHistoryMapper viewHistoryMapper;
    private final UserMapper userMapper;

    private static final int MAX_HISTORY = 100;

    @Override
    public Result<?> record(Long productId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.ok(); // 未登录不记录
        }

        // 检查是否已存在
        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId)
               .eq(ViewHistory::getProductId, productId);
        ViewHistory existing = viewHistoryMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新时间
            existing.setCreatedAt(LocalDateTime.now());
            viewHistoryMapper.updateById(existing);
        } else {
            // 检查是否超过上限
            LambdaQueryWrapper<ViewHistory> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ViewHistory::getUserId, userId);
            Long count = viewHistoryMapper.selectCount(countWrapper);

            if (count >= MAX_HISTORY) {
                // 删除最早的记录
                LambdaQueryWrapper<ViewHistory> oldestWrapper = new LambdaQueryWrapper<>();
                oldestWrapper.eq(ViewHistory::getUserId, userId)
                             .orderByAsc(ViewHistory::getCreatedAt)
                             .last("LIMIT 1");
                viewHistoryMapper.delete(oldestWrapper);
            }

            // 插入新记录
            ViewHistory history = new ViewHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            viewHistoryMapper.insert(history);
        }

        return Result.ok();
    }

    @Override
    public Result<?> getList(Integer page, Integer size) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        Page<ViewHistory> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId)
               .orderByDesc(ViewHistory::getCreatedAt);
        Page<ViewHistory> result = viewHistoryMapper.selectPage(pageObj, wrapper);

        return Result.ok(result);
    }

    @Override
    public Result<?> clear() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId);
        viewHistoryMapper.delete(wrapper);

        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getId, id)
               .eq(ViewHistory::getUserId, userId);
        viewHistoryMapper.delete(wrapper);

        return Result.ok();
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) {
            return null;
        }
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
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ViewHistoryService.java
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ViewHistoryServiceImpl.java
git commit -m "feat: 添加 ViewHistoryService 服务层"
```

---

## Task 4: 后端 — 创建 ViewHistoryController

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ViewHistoryController.java`

- [ ] **Step 1: 创建 ViewHistoryController**

```java
package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ViewHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/view-history")
@RequiredArgsConstructor
public class ViewHistoryController {

    private final ViewHistoryService viewHistoryService;

    @PostMapping
    public Result<?> record(@RequestBody java.util.Map<String, Long> body) {
        return viewHistoryService.record(body.get("productId"));
    }

    @GetMapping
    public Result<?> getList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size) {
        return viewHistoryService.getList(page, size);
    }

    @DeleteMapping
    public Result<?> clear() {
        return viewHistoryService.clear();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return viewHistoryService.delete(id);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ViewHistoryController.java
git commit -m "feat: 添加 ViewHistoryController API 控制器"
```

---

## Task 5: 前端 — 创建浏览历史 API 封装

**Files:**
- Create: `xianyuplus-web/src/api/viewHistory.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from './request'

export function recordView(productId) {
  return request.post('/view-history', { productId })
}

export function getViewHistory(page = 1, size = 20) {
  return request.get('/view-history', { params: { page, size } })
}

export function clearViewHistory() {
  return request.delete('/view-history')
}

export function deleteViewHistory(id) {
  return request.delete(`/view-history/${id}`)
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/api/viewHistory.js
git commit -m "feat: 添加浏览历史 API 封装"
```

---

## Task 6: 前端 — 创建 ViewHistory 页面

**Files:**
- Create: `xianyuplus-web/src/views/ViewHistory.vue`

- [ ] **Step 1: 创建 ViewHistory.vue**

```vue
<template>
  <div class="view-history">
    <div class="page-header">
      <h2>浏览历史</h2>
      <button v-if="historyList.length > 0" class="btn-clear" @click="handleClear">
        清空历史
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="historyList.length === 0" class="empty">
      <div class="empty-icon">📖</div>
      <div class="empty-text">暂无浏览记录</div>
      <router-link to="/" class="btn-go">去逛逛</router-link>
    </div>
    <div v-else class="history-content">
      <!-- 按日期分组 -->
      <div v-for="group in groupedHistory" :key="group.date" class="date-group">
        <div class="date-label">{{ group.date }}</div>
        <div class="product-grid">
          <div v-for="item in group.items" :key="item.id" class="history-item">
            <router-link :to="`/product/${item.productId}`" class="product-link">
              <ProductCard :product="item.product" />
            </router-link>
            <button class="btn-delete" @click="handleDelete(item.id)" title="删除">×</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 20" class="pagination">
        <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / 20) }}</span>
        <button :disabled="page >= Math.ceil(total / 20)" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getViewHistory, clearViewHistory, deleteViewHistory } from '@/api/viewHistory'
import { getProduct } from '@/api/product'
import { Dialog } from '@/utils/dialog'
import { Toast } from '@/utils/toast'
import ProductCard from '@/components/ProductCard.vue'

const historyList = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(true)

// 按日期分组
const groupedHistory = computed(() => {
  const groups = {}
  const today = new Date().toLocaleDateString('zh-CN')
  const yesterday = new Date(Date.now() - 86400000).toLocaleDateString('zh-CN')

  historyList.value.forEach(item => {
    const date = new Date(item.createdAt).toLocaleDateString('zh-CN')
    let label = date
    if (date === today) label = '今天'
    else if (date === yesterday) label = '昨天'

    if (!groups[label]) {
      groups[label] = { date: label, items: [] }
    }
    groups[label].items.push(item)
  })

  return Object.values(groups)
})

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getViewHistory(page.value, 20)
    if (res.code === 200) {
      const records = res.data.records || []
      total.value = res.data.total || 0

      // 获取每个商品的详细信息
      const enriched = await Promise.all(
        records.map(async (item) => {
          try {
            const productRes = await getProduct(item.productId)
            return {
              ...item,
              product: productRes.code === 200 ? productRes.data : null
            }
          } catch {
            return { ...item, product: null }
          }
        })
      )

      historyList.value = enriched.filter(item => item.product)
    }
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  page.value = p
  fetchHistory()
  window.scrollTo(0, 0)
}

async function handleDelete(id) {
  await deleteViewHistory(id)
  historyList.value = historyList.value.filter(item => item.id !== id)
  total.value--
  Toast.success('已删除')
}

async function handleClear() {
  const ok = await Dialog.confirm({
    title: '清空浏览历史',
    message: '确定要清空所有浏览记录吗？此操作不可恢复。'
  })
  if (!ok) return

  await clearViewHistory()
  historyList.value = []
  total.value = 0
  Toast.success('已清空')
}

onMounted(fetchHistory)
</script>

<style scoped>
.view-history {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.btn-clear {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.btn-clear:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.btn-go {
  display: inline-block;
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
}

.btn-go:hover {
  opacity: 0.9;
}

.date-group {
  margin-bottom: 32px;
}

.date-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.history-item {
  position: relative;
}

.product-link {
  text-decoration: none;
  color: inherit;
}

.btn-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .btn-delete {
  opacity: 1;
}

.btn-delete:hover {
  background: rgba(239, 68, 68, 0.8);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px 0;
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
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/views/ViewHistory.vue
git commit -m "feat: 添加浏览历史页面"
```

---

## Task 7: 前端 — 添加路由

**Files:**
- Modify: `xianyuplus-web/src/router/index.js`

- [ ] **Step 1: 添加浏览历史路由**

在路由配置中添加：

```javascript
{
  path: '/history',
  name: 'ViewHistory',
  component: () => import('@/views/ViewHistory.vue'),
  meta: { title: '浏览历史', auth: true }
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/router/index.js
git commit -m "feat: 添加浏览历史路由"
```

---

## Task 8: 前端 — 商品详情页自动记录浏览

**Files:**
- Modify: `xianyuplus-web/src/views/ProductDetail.vue`

- [ ] **Step 1: 导入 API**

```javascript
import { recordView } from '@/api/viewHistory'
import { useUserStore } from '@/stores/user'
```

- [ ] **Step 2: 在获取商品详情后记录浏览**

在 `fetchProduct` 方法成功后添加：

```javascript
const userStore = useUserStore()
if (userStore.token) {
  recordView(route.params.id).catch(() => {}) // 静默失败
}
```

- [ ] **Step 3: Commit**

```bash
git add xianyuplus-web/src/views/ProductDetail.vue
git commit -m "feat: 商品详情页自动记录浏览历史"
```

---

## Task 9: 前端 — 个人中心添加入口

**Files:**
- Modify: `xianyuplus-web/src/views/Profile.vue`

- [ ] **Step 1: 添加浏览历史入口**

在个人中心页面合适位置添加：

```html
<router-link to="/history" class="profile-menu-item">
  <span class="menu-icon">📖</span>
  <span class="menu-text">浏览历史</span>
  <span class="menu-arrow">›</span>
</router-link>
```

- [ ] **Step 2: 添加样式**

```css
.profile-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: background 0.2s;
}

.profile-menu-item:hover {
  background: #f8f9fa;
}

.menu-icon {
  font-size: 20px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
}

.menu-arrow {
  color: #999;
  font-size: 18px;
}
```

- [ ] **Step 3: Commit**

```bash
git add xianyuplus-web/src/views/Profile.vue
git commit -m "feat: 个人中心添加浏览历史入口"
```

---

## Task 10: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端和前端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`
Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 2: 测试浏览记录**

1. 用 test1 登录
2. 浏览几个商品详情页
3. 进入"浏览历史"页面
4. 验证浏览记录正确显示
5. 验证按日期分组正确

- [ ] **Step 3: 测试删除和清空**

1. 删除单条浏览记录
2. 点击"清空历史"
3. 确认后验证所有记录被清空

- [ ] **Step 4: 测试去重更新**

1. 浏览商品 A
2. 再次浏览商品 A
3. 验证只有一条记录，时间更新为最新

- [ ] **Step 5: 测试上限**

1. 浏览超过 100 个商品
2. 验证最早的记录被自动删除

- [ ] **Step 6: Commit**

```bash
git add .
git commit -m "test: 浏览历史功能完整验证通过"
```

---

## 完成

浏览历史功能实施完成。所有6个功能模块全部实现。
