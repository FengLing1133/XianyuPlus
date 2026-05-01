# Progress Log

## Session: 2026-05-01 (第二轮)

### Phase 7: 修复聊天功能
- **Status:** complete
- Actions taken:
  - `chat.js`: 乐观更新 — send() 发送后立即将消息 push 到本地 messages 数组
  - `chat.js`: WebSocket URL 改为相对路径，走 Vite 代理
  - `chat.js`: 复用已有连接，不再每次重建
  - `chat.js`: send() 返回 boolean，失败时通知用户
  - `chat.js`: 新增 setMessages() 方法，避免引用混乱
  - `ChatWindow.vue`: 重构消息管理，使用 chatStore.setMessages() 同步
  - `ChatWindow.vue`: 发送失败时 Toast 提示
  - `ChatWebSocketHandler.java`: 消息回传给发送方（包含数据库生成的 id 和 createdAt）
  - 移除了无用的 Redis Pub/Sub 广播
- Files modified:
  - xianyuplus-web/src/stores/chat.js
  - xianyuplus-web/src/views/ChatWindow.vue
  - xianyu-plus/chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java

### Phase 8: 订单确认付款功能
- **Status:** complete
- Actions taken:
  - `OrderServiceImpl.java`: 允许买家 PENDING→PAID 状态转换
  - `Order.vue`: 买家视角添加"确认付款"按钮
- Files modified:
  - xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/OrderServiceImpl.java
  - xianyuplus-web/src/views/Order.vue

### Phase 9: 修复商品缩略图显示不完全
- **Status:** complete
- Actions taken:
  - `ProductCard.vue`: `.card-image` 高度从固定 180px 改为 `aspect-ratio: 4/3`
  - `ProductCard.vue`: `.real-img` 从 `object-fit: cover` 改为 `contain`，不再裁切
- Files modified:
  - xianyuplus-web/src/components/ProductCard.vue

### Phase 10: 修复分类筛选功能
- **Status:** complete
- Actions taken:
  - `ProductServiceImpl.java`: 查询时先检查是否为父分类，若有子分类则用 IN 查询包含所有子分类
- Files modified:
  - xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ProductServiceImpl.java

### Phase 11: "暂无商品"居中
- **Status:** complete
- Actions taken:
  - `Home.vue`: 添加 `.product-grid .empty-state { grid-column: 1 / -1; }` 横跨所有列
- Files modified:
  - xianyuplus-web/src/views/Home.vue

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| 后端编译 | `mvn compile` | 无错误 | 编译通过 | ✓ |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
|           |       |         |            |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | 所有 5 个问题已修复 |
| Where am I going? | 等待用户验证 |
| What's the goal? | 修复聊天、订单付款、缩略图、分类筛选、居中 |
| What have I learned? | 见 findings.md |
| What have I done? | 见上方各阶段记录 |
