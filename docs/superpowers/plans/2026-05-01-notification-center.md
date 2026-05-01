# 通知中心实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现通知中心功能，支持订单状态、新消息、商品相关、系统通知，通过铃铛角标+下拉面板展示，WebSocket实时推送。

**Architecture:** 新增 notification 表存储通知，后端提供 CRUD API，前端新增 NotificationBell 组件集成到 Layout，复用现有 WebSocket 连接推送新通知。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3, WebSocket

---

## 文件结构

### 后端文件

| 文件 | 职责 |
|------|------|
| `common/.../entity/Notification.java` | 通知实体类 |
| `common/.../mapper/NotificationMapper.java` | MyBatis-Plus Mapper |
| `service/.../controller/NotificationController.java` | 通知 API 控制器 |
| `service/.../service/NotificationService.java` | 通知服务接口 |
| `service/.../service/impl/NotificationServiceImpl.java` | 通知服务实现 |
| `service/.../dto/NotificationDTO.java` | 通知数据传输对象 |

### 前端文件

| 文件 | 职责 |
|------|------|
| `src/components/NotificationBell.vue` | 铃铛图标+下拉面板组件 |
| `src/api/notification.js` | 通知 API 封装 |
| `src/stores/notification.js` | 通知状态管理 (Pinia) |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `init.sql` | 添加 notification 表 |
| `OrderServiceImpl.java` | 订单状态变更时创建通知 |
| `ChatWebSocketHandler.java` | 收到消息时创建通知 |
| `FavoriteServiceImpl.java` | 收藏商品时创建通知 |
| `Layout.vue` | 导航栏集成 NotificationBell |

---

## Task 1: 数据库 — 创建 notification 表

**Files:**
- Modify: `xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 1: 添加 notification 表定义**

在 `message` 表之后添加：

```sql
-- --------------------------------------------
-- 通知表
-- --------------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '接收者',
    `type` TINYINT NOT NULL COMMENT '1订单状态 2新消息 3商品相关 4系统通知',
    `title` VARCHAR(100) NOT NULL,
    `content` VARCHAR(500) DEFAULT NULL,
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(订单ID/商品ID/消息ID)',
    `is_read` TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
```

- [ ] **Step 2: 执行数据库更新**

Run: `mysql -u root -p xianyu_plus < xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 3: 验证表创建**

Run: `mysql -u root -p xianyu_plus -e "DESCRIBE notification;"`

Expected: 显示 id, user_id, type, title, content, related_id, is_read, created_at 字段

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/resources/db/init.sql
git commit -m "feat(db): 添加 notification 通知表"
```

---

## Task 2: 后端 — 创建 Notification 实体和 Mapper

**Files:**
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Notification.java`
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/NotificationMapper.java`

- [ ] **Step 1: 创建 Notification 实体类**

```java
package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建 NotificationMapper 接口**

```java
package com.xianyuplus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyuplus.common.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl common`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Notification.java
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/NotificationMapper.java
git commit -m "feat: 添加 Notification 实体和 Mapper"
```

---

## Task 3: 后端 — 创建 NotificationService

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/NotificationService.java`
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/NotificationServiceImpl.java`

- [ ] **Step 1: 创建 NotificationService 接口**

```java
package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface NotificationService {
    Result<?> getList(Long userId, Integer page, Integer size);
    Result<?> getUnreadCount(Long userId);
    Result<?> markAsRead(Long id, Long userId);
    Result<?> markAllAsRead(Long userId);
    Result<?> delete(Long id, Long userId);
    void createNotification(Long userId, Integer type, String title, String content, Long relatedId);
}
```

- [ ] **Step 2: 创建 NotificationServiceImpl 实现类**

```java
package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Notification;
import com.xianyuplus.common.mapper.NotificationMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public Result<?> getList(Long userId, Integer page, Integer size) {
        Page<Notification> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(pageObj, wrapper);
        return Result.ok(result);
    }

    @Override
    public Result<?> getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        Long count = notificationMapper.selectCount(wrapper);
        return Result.ok(count);
    }

    @Override
    public Result<?> markAsRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return Result.ok();
    }

    @Override
    public Result<?> markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId);
        notificationMapper.delete(wrapper);
        return Result.ok();
    }

    @Override
    public void createNotification(Long userId, Integer type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/NotificationService.java
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/NotificationServiceImpl.java
git commit -m "feat: 添加 NotificationService 服务层"
```

---

## Task 4: 后端 — 创建 NotificationController

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/NotificationController.java`

- [ ] **Step 1: 创建 NotificationController**

```java
package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<?> getList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        return notificationService.getList(userId, page, size);
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.markAsRead(id, userId);
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = getCurrentUserId();
        return notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.delete(id, userId);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 需要注入 UserService 或 UserMapper 来获取 userId
        // 这里简化处理，实际应该查询数据库
        return null; // TODO: 实现获取当前用户ID
    }
}
```

- [ ] **Step 2: 修改 getCurrentUserId 方法实现**

需要注入 UserMapper 来获取当前用户ID：

```java
package com.xianyuplus.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @GetMapping
    public Result<?> getList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        return notificationService.getList(userId, page, size);
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.markAsRead(id, userId);
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = getCurrentUserId();
        return notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.delete(id, userId);
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
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/NotificationController.java
git commit -m "feat: 添加 NotificationController API 控制器"
```

---

## Task 5: 后端 — 订单状态变更时创建通知

**Files:**
- Modify: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/OrderServiceImpl.java`

- [ ] **Step 1: 注入 NotificationService**

在 OrderServiceImpl 类中添加：

```java
private final NotificationService notificationService;
```

- [ ] **Step 2: 在 updateStatus 方法中添加通知逻辑**

在 `orderMapper.updateById(order)` 之后添加：

```java
// 创建通知
createOrderNotification(order, target);
```

- [ ] **Step 3: 添加 createOrderNotification 私有方法**

```java
private void createOrderNotification(Order order, int targetStatus) {
    String title;
    String content;
    Long notifyUserId;

    switch (OrderStatus.fromCode(targetStatus)) {
        case PAID:
            title = "买家已付款";
            content = "您的商品已被买家付款，等待您发货";
            notifyUserId = order.getSellerId();
            break;
        case SHIPPED:
            title = "卖家已发货";
            content = "您购买的商品已发货，请注意查收";
            notifyUserId = order.getBuyerId();
            break;
        case COMPLETED:
            title = "订单已完成";
            content = "交易已完成，感谢您的使用";
            notifyUserId = order.getSellerId();
            break;
        case CANCELLED:
            title = "订单已取消";
            content = "订单已被取消";
            notifyUserId = order.getSellerId().equals(getCurrentUserId()) 
                          ? order.getBuyerId() 
                          : order.getSellerId();
            break;
        default:
            return;
    }

    notificationService.createNotification(
        notifyUserId, 
        1, // 订单状态类型
        title, 
        content, 
        order.getId()
    );
}
```

- [ ] **Step 4: 添加 getCurrentUserId 辅助方法**

```java
private Long getCurrentUserId() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, username);
    User user = userMapper.selectOne(wrapper);
    return user != null ? user.getId() : null;
}
```

- [ ] **Step 5: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/OrderServiceImpl.java
git commit -m "feat: 订单状态变更时自动创建通知"
```

---

## Task 6: 后端 — 收到消息时创建通知

**Files:**
- Modify: `xianyu-plus/chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java`

- [ ] **Step 1: 注入 NotificationService**

在 ChatWebSocketHandler 类中添加：

```java
private final NotificationService notificationService;
```

- [ ] **Step 2: 在消息保存后创建通知**

在 `messageMapper.insert(message)` 之后添加：

```java
// 创建消息通知
notificationService.createNotification(
    message.getReceiverId(),
    2, // 新消息类型
    "收到新消息",
    message.getContent().length() > 50 ? message.getContent().substring(0, 50) + "..." : message.getContent(),
    message.getId()
);
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl chat`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java
git commit -m "feat: 收到聊天消息时自动创建通知"
```

---

## Task 7: 后端 — 商品被收藏时创建通知

**Files:**
- Modify: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/FavoriteServiceImpl.java`

- [ ] **Step 1: 注入 NotificationService**

在 FavoriteServiceImpl 类中添加：

```java
private final NotificationService notificationService;
```

- [ ] **Step 2: 在 addFavorite 方法中添加通知逻辑**

在 `favoriteMapper.insert(favorite)` 之后添加：

```java
// 创建收藏通知
Product product = productMapper.selectById(productId);
if (product != null && !product.getUserId().equals(user.getId())) {
    notificationService.createNotification(
        product.getUserId(),
        3, // 商品相关类型
        "商品被收藏",
        "您发布的商品 \"" + product.getTitle() + "\" 被人收藏了",
        productId
    );
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/FavoriteServiceImpl.java
git commit -m "feat: 商品被收藏时自动创建通知"
```

---

## Task 8: 前端 — 创建通知 API 封装

**Files:**
- Create: `xianyuplus-web/src/api/notification.js`

- [ ] **Step 1: 创建通知 API 文件**

```javascript
import request from './request'

export function getNotifications(page = 1, size = 20) {
  return request.get('/notification', { params: { page, size } })
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markAsRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/notification/read-all')
}

export function deleteNotification(id) {
  return request.delete(`/notification/${id}`)
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/api/notification.js
git commit -m "feat: 添加通知 API 封装"
```

---

## Task 9: 前端 — 创建通知状态管理

**Files:**
- Create: `xianyuplus-web/src/stores/notification.js`

- [ ] **Step 1: 创建通知 Pinia Store**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)

  async function fetchNotifications(page = 1, size = 20) {
    loading.value = true
    try {
      const res = await getNotifications(page, size)
      if (res.code === 200) {
        notifications.value = res.data.records || []
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      if (res.code === 200) {
        unreadCount.value = res.data || 0
      }
    } catch (e) {
      console.error('获取未读数量失败:', e)
    }
  }

  async function markItemAsRead(id) {
    await markAsRead(id)
    const item = notifications.value.find(n => n.id === id)
    if (item) {
      item.isRead = 1
    }
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  async function markAllItemsAsRead() {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  }

  async function deleteItem(id) {
    await deleteNotification(id)
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      const item = notifications.value[index]
      if (item.isRead === 0 && unreadCount.value > 0) {
        unreadCount.value--
      }
      notifications.value.splice(index, 1)
    }
  }

  function incrementUnread() {
    unreadCount.value++
  }

  return {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    fetchUnreadCount,
    markItemAsRead,
    markAllItemsAsRead,
    deleteItem,
    incrementUnread
  }
})
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/stores/notification.js
git commit -m "feat: 添加通知状态管理 Store"
```

---

## Task 10: 前端 — 创建 NotificationBell 组件

**Files:**
- Create: `xianyuplus-web/src/components/NotificationBell.vue`

- [ ] **Step 1: 创建 NotificationBell 组件**

```vue
<template>
  <div class="notification-bell" @click.stop="togglePanel">
    <div class="bell-icon">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
      </svg>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <transition name="fade">
      <div v-if="showPanel" class="panel" @click.stop>
        <div class="panel-header">
          <span class="panel-title">通知</span>
          <button v-if="unreadCount > 0" class="mark-all-btn" @click="handleMarkAllRead">
            全部已读
          </button>
        </div>

        <div v-if="loading" class="panel-loading">加载中...</div>
        <div v-else-if="notifications.length === 0" class="panel-empty">暂无通知</div>
        <div v-else class="panel-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleClick(item)"
          >
            <div class="item-icon" :class="'type-' + item.type">
              {{ typeIcons[item.type] }}
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.content }}</div>
              <div class="item-time">{{ formatTime(item.createdAt) }}</div>
            </div>
            <button class="item-delete" @click.stop="handleDelete(item.id)">×</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { storeToRefs } from 'pinia'

const router = useRouter()
const store = useNotificationStore()
const { notifications, unreadCount, loading } = storeToRefs(store)

const showPanel = ref(false)

const typeIcons = {
  1: '📦',
  2: '💬',
  3: '❤️',
  4: '📢'
}

function togglePanel() {
  showPanel.value = !showPanel.value
  if (showPanel.value) {
    store.fetchNotifications()
  }
}

function closePanel() {
  showPanel.value = false
}

async function handleClick(item) {
  if (item.isRead === 0) {
    await store.markItemAsRead(item.id)
  }
  closePanel()

  // 根据通知类型跳转
  switch (item.type) {
    case 1: // 订单状态
      router.push('/order')
      break
    case 2: // 新消息
      router.push('/chat')
      break
    case 3: // 商品相关
      if (item.relatedId) {
        router.push(`/product/${item.relatedId}`)
      }
      break
  }
}

async function handleMarkAllRead() {
  await store.markAllItemsAsRead()
}

async function handleDelete(id) {
  await store.deleteItem(id)
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString('zh-CN')
}

// 点击外部关闭面板
function handleOutsideClick(e) {
  if (showPanel.value && !e.target.closest('.notification-bell')) {
    closePanel()
  }
}

onMounted(() => {
  store.fetchUnreadCount()
  document.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  cursor: pointer;
}

.bell-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: background 0.2s;
}

.bell-icon:hover {
  background: rgba(0, 0, 0, 0.05);
}

.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: #ef4444;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  max-height: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
}

.mark-all-btn {
  font-size: 13px;
  color: var(--primary);
  background: none;
  border: none;
  cursor: pointer;
}

.mark-all-btn:hover {
  text-decoration: underline;
}

.panel-loading,
.panel-empty {
  padding: 40px 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.panel-list {
  overflow-y: auto;
  flex: 1;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-item.unread:hover {
  background: #e8f2ff;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.item-icon.type-1 { background: #e8f4fd; }
.item-icon.type-2 { background: #e8f5e9; }
.item-icon.type-3 { background: #fce4ec; }
.item-icon.type-4 { background: #fff3e0; }

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.item-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #999;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .item-delete {
  opacity: 1;
}

.item-delete:hover {
  background: #fee2e2;
  color: #ef4444;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/components/NotificationBell.vue
git commit -m "feat: 添加 NotificationBell 铃铛组件"
```

---

## Task 11: 前端 — 集成 NotificationBell 到 Layout

**Files:**
- Modify: `xianyuplus-web/src/components/Layout.vue`

- [ ] **Step 1: 导入 NotificationBell 组件**

在 `<script setup>` 中添加：

```javascript
import NotificationBell from './NotificationBell.vue'
```

- [ ] **Step 2: 在导航栏添加 NotificationBell**

在导航栏合适位置（用户头像/昵称旁边）添加：

```html
<NotificationBell v-if="userStore.token" />
```

- [ ] **Step 3: 测试验证**

Run: `cd xianyuplus-web && npm run dev`

在浏览器中访问首页，确认：
1. 登录后导航栏显示铃铛图标
2. 点击铃铛展开通知面板
3. 未读数量显示在角标上

- [ ] **Step 4: Commit**

```bash
git add xianyuplus-web/src/components/Layout.vue
git commit -m "feat: 在导航栏集成通知铃铛组件"
```

---

## Task 12: 前端 — WebSocket 接收通知推送

**Files:**
- Modify: `xianyuplus-web/src/stores/chat.js`

- [ ] **Step 1: 导入通知 Store**

在 chat.js 中添加：

```javascript
import { useNotificationStore } from './notification'
```

- [ ] **Step 2: 在 onmessage 中处理通知消息**

修改 WebSocket onmessage 处理逻辑，区分聊天消息和通知消息：

```javascript
ws.onmessage = (event) => {
  const data = JSON.parse(event.data)

  // 处理通知推送
  if (data.type === 'notification') {
    const notificationStore = useNotificationStore()
    notificationStore.incrementUnread()
    notificationStore.fetchNotifications()
    return
  }

  // 原有的聊天消息处理逻辑...
}
```

- [ ] **Step 3: 测试验证**

启动后端和前端，测试：
1. 下单后卖家收到通知
2. 发消息后对方收到通知
3. 收藏商品后卖家收到通知

- [ ] **Step 4: Commit**

```bash
git add xianyuplus-web/src/stores/chat.js
git commit -m "feat: WebSocket 接收通知推送并更新角标"
```

---

## Task 13: 后端 — WebSocket 推送通知

**Files:**
- Modify: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/NotificationServiceImpl.java`

- [ ] **Step 1: 注入 WebSocket 会话管理**

需要获取在线用户的 WebSocket 会话来推送通知。在 NotificationServiceImpl 中添加：

```java
// 需要从 ChatWebSocketHandler 获取在线用户会话
// 这里通过 Spring 的 ApplicationEventPublisher 发布事件
private final ApplicationEventPublisher eventPublisher;
```

- [ ] **Step 2: 创建通知事件类**

```java
package com.xianyuplus.service.event;

import com.xianyuplus.common.entity.Notification;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final Notification notification;

    public NotificationEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }
}
```

- [ ] **Step 3: 在 createNotification 方法中发布事件**

```java
@Override
public void createNotification(Long userId, Integer type, String title, String content, Long relatedId) {
    Notification notification = new Notification();
    notification.setUserId(userId);
    notification.setType(type);
    notification.setTitle(title);
    notification.setContent(content);
    notification.setRelatedId(relatedId);
    notification.setIsRead(0);
    notificationMapper.insert(notification);

    // 发布通知事件
    eventPublisher.publishEvent(new NotificationEvent(this, notification));
}
```

- [ ] **Step 4: 在 ChatWebSocketHandler 中监听通知事件**

```java
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler implements ApplicationListener<NotificationEvent> {

    @Override
    public void onApplicationEvent(NotificationEvent event) {
        Notification notification = event.getNotification();
        WebSocketSession session = onlineSessions.get(notification.getUserId());
        if (session != null && session.isOpen()) {
            try {
                JSONObject msg = new JSONObject();
                msg.set("type", "notification");
                msg.set("data", JSONUtil.parse(notification));
                session.sendMessage(new TextMessage(msg.toString()));
            } catch (IOException e) {
                log.error("推送通知失败", e);
            }
        }
    }
}
```

- [ ] **Step 5: 编译验证**

Run: `cd xianyu-plus && mvn compile`

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/event/NotificationEvent.java
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/NotificationServiceImpl.java
git add xianyu-plus/chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java
git commit -m "feat: WebSocket 实时推送新通知到前端"
```

---

## Task 14: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`

- [ ] **Step 2: 启动前端**

Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 3: 测试订单通知**

1. 用 test1 登录，发布一个商品
2. 用 test2 登录，下单并确认付款
3. 检查 test1 是否收到"买家已付款"通知
4. test1 确认发货
5. 检查 test2 是否收到"卖家已发货"通知

- [ ] **Step 4: 测试消息通知**

1. 用 test1 给 test2 发送消息
2. 检查 test2 是否收到"收到新消息"通知

- [ ] **Step 5: 测试收藏通知**

1. 用 test2 收藏 test1 的商品
2. 检查 test1 是否收到"商品被收藏"通知

- [ ] **Step 6: 测试通知面板**

1. 点击铃铛图标，验证面板展开
2. 验证未读数量显示正确
3. 点击通知，验证跳转正确
4. 点击"全部已读"，验证角标清零
5. 删除通知，验证从列表移除

- [ ] **Step 7: Commit 测试完成**

```bash
git add .
git commit -m "test: 通知中心功能完整验证通过"
```

---

## 完成

通知中心功能实施完成。下一步：搜索增强。
