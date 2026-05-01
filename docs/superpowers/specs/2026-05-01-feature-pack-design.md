# XianyuPlus 功能包设计文档

## 概述

为校园二手交易平台添加6个核心功能模块，提升用户体验和平台完整性。

**实施顺序（按用户优先级）：**
1. 通知中心
2. 搜索增强
3. 商品评价系统
4. 商品举报
5. 卖家主页
6. 浏览历史

---

## 模块 1：通知中心

### 数据库

```sql
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

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/notification` | GET | 分页获取通知列表 |
| `/api/notification/unread-count` | GET | 获取未读数量 |
| `/api/notification/{id}/read` | PUT | 标记单条已读 |
| `/api/notification/read-all` | PUT | 全部标记已读 |
| `/api/notification/{id}` | DELETE | 删除通知 |

### 通知触发时机

| 事件 | 接收者 | 通知类型 |
|------|--------|----------|
| 买家确认付款 | 卖家 | 订单状态 |
| 卖家确认发货 | 买家 | 订单状态 |
| 买家确认收货 | 卖家 | 订单状态 |
| 订单取消 | 对方 | 订单状态 |
| 收到聊天消息 | 接收者 | 新消息 |
| 商品被收藏 | 卖家 | 商品相关 |
| 管理员发送公告 | 全部用户 | 系统通知 |

### 前端组件

- **NotificationBell** — 导航栏铃铛图标，显示未读角标
- **NotificationPanel** — 下拉面板，展示最近20条通知
- 通过 WebSocket 实时推送新通知（复用现有 `/ws/chat` 连接）

---

## 模块 2：搜索增强

### 接口扩展

扩展现有 `GET /api/product` 接口，新增查询参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| `minPrice` | Decimal | 最低价 |
| `maxPrice` | Decimal | 最高价 |
| `condition` | Integer | 成色筛选(1全新 2几乎全新 3轻微使用 4明显痕迹) |
| `categoryIds` | String | 多分类ID，逗号分隔(如 "1,7,8") |
| `sort` | String | 排序方式: `price_asc`, `price_desc`, `newest`, `views` |

### 后端实现

- `ProductQueryDTO` 新增对应字段
- `ProductServiceImpl` 动态拼接 MyBatis-Plus 查询条件
- 多分类筛选使用 `IN` 语句

### 前端改造

**Home.vue 筛选栏增强：**
- 价格区间：两个输入框，支持小数
- 成色下拉：全部/全新/几乎全新/轻微使用/明显痕迹
- 排序下拉：默认排序/价格低到高/价格高到低/最新发布/最多浏览
- 多分类：支持 Ctrl 多选或复选框

---

## 模块 3：商品评价系统

### 数据库

```sql
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

### API 端点

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/review` | POST | 买家提交评价 | 买家 |
| `/api/review/{id}/reply` | PUT | 卖家回复评价 | 卖家 |
| `/api/review/product/{productId}` | GET | 获取商品评价列表 | 公开 |
| `/api/review/seller/{sellerId}` | GET | 获取卖家评价汇总 | 公开 |

### 业务规则

1. **评价条件**：订单状态为"已完成"(status=4)，且未评价过
2. **评分范围**：1-5星，必填
3. **评价内容**：可选，最多500字
4. **卖家回复**：每条评价只能回复一次，最多500字
5. **不可修改**：评价提交后不可修改，回复也不可修改

### 前端页面

**商品详情页 (ProductDetail.vue)：**
- 评分汇总：平均评分 + 星级分布柱状图
- 评价列表：分页展示

**订单页 (Order.vue)：**
- 已完成订单显示"去评价"按钮（未评价时）
- 点击弹出评价表单弹窗

---

## 模块 4：商品举报

### 数据库

```sql
CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL COMMENT '被举报商品',
    `reporter_id` BIGINT NOT NULL COMMENT '举报者',
    `reason` TINYINT NOT NULL COMMENT '1虚假信息 2违禁品 3价格异常 4恶意欺诈 5其他',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '补充说明(可选)',
    `status` TINYINT DEFAULT 0 COMMENT '0待处理 1已处理 2已驳回',
    `admin_note` VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `handled_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';
```

### API 端点

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/report` | POST | 提交举报 | 登录用户 |
| `/api/report/product/{productId}/check` | GET | 检查是否已举报过 | 登录用户 |
| `/api/admin/report` | GET | 管理员获取举报列表 | 管理员 |
| `/api/admin/report/{id}/handle` | PUT | 管理员处理举报 | 管理员 |

### 举报原因枚举

| Code | 原因 |
|------|------|
| 1 | 虚假信息 |
| 2 | 违禁品 |
| 3 | 价格异常 |
| 4 | 恶意欺诈 |
| 5 | 其他 |

### 业务规则

1. 每个用户对同一商品只能举报一次
2. 不可举报自己的商品
3. 管理员可标记为"已处理"（商品下架）或"已驳回"

### 前端页面

**商品详情页 (ProductDetail.vue)：**
- "举报"按钮，点击弹出表单
- 已举报过则按钮变灰

**管理员后台新增 `admin/Reports.vue`：**
- 举报列表 + 处理/驳回操作

---

## 模块 5：卖家主页

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/user/{id}/profile` | GET | 获取卖家公开资料 |
| `/api/user/{id}/stats` | GET | 获取卖家交易统计 |
| `/api/user/{id}/products` | GET | 获取卖家在售商品列表(分页) |

### 返回数据

**卖家资料：**
```json
{
  "id": 1,
  "nickname": "张三",
  "avatar": "/uploads/avatar/xxx.png",
  "createdAt": "2026-01-15",
  "avgRating": 4.5,
  "reviewCount": 12
}
```

**交易统计：**
```json
{
  "completedOrders": 28,
  "positiveRate": 96.4,
  "totalSold": 35
}
```

### 前端页面

**新增 `SellerProfile.vue` 路由：`/seller/:id`**

布局：卖家资料卡片 + 信用评分 + 交易统计 + 商品列表（分页）

**入口位置：**
- 商品详情页点击卖家昵称/头像
- 评价列表点击卖家昵称
- 订单页面点击对方昵称

---

## 模块 6：浏览历史

### 数据库

```sql
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

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/view-history` | GET | 获取浏览历史(按日期分组) |
| `/api/view-history` | POST | 记录浏览(商品详情页自动调用) |
| `/api/view-history` | DELETE | 清空浏览历史 |
| `/api/view-history/{id}` | DELETE | 删除单条记录 |

### 业务规则

1. **自动记录**：用户访问商品详情页时自动记录（已登录状态）
2. **去重更新**：同一用户浏览同一商品，更新时间
3. **上限100条**：超出时删除最早的记录
4. **日期分组**：今天、昨天、更早
5. **清空功能**：一键清空所有浏览历史

### 前端页面

**新增 `ViewHistory.vue` 路由：`/history`**

布局：按日期分组展示商品卡片，支持清空和删除单条

**入口位置：**
- 个人中心 (Profile.vue) 添加"浏览历史"入口

---

## 实施顺序

| 阶段 | 功能 | 预估工作量 |
|------|------|-----------|
| Phase 23-25 | 通知中心 | 后端+前端+WebSocket集成 |
| Phase 26-27 | 搜索增强 | 后端查询扩展+前端筛选栏 |
| Phase 28-30 | 商品评价系统 | 新表+后端API+前端页面 |
| Phase 31-32 | 商品举报 | 新表+后端API+前端+管理端 |
| Phase 33-34 | 卖家主页 | 后端API+新前端页面 |
| Phase 35-36 | 浏览历史 | 新表+后端API+新前端页面 |
