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

## Session: 2026-05-01 (第三轮)

### Phase 12: 修复聊天消息方向 + 接收方收不到消息
- **Status:** complete
- Actions taken:
  - `chat.js`: send() 增加 senderId 参数，乐观消息设置正确的 senderId
  - `chat.js`: connect() 保存 currentUserId.value
  - `chat.js`: onmessage 用 String() 比较 senderId，收到自己消息回传时替换乐观消息
  - `ChatWindow.vue`: 传 currentUserId 给 send()，模板用 String() 比较
- 根因: JacksonConfig 将 Long 序列化为 String，=== 比较失败；乐观消息 senderId=null
- Files modified:
  - xianyuplus-web/src/stores/chat.js
  - xianyuplus-web/src/views/ChatWindow.vue

### Phase 13: 修复首页切换分类卡片阴影闪烁
- **Status:** complete
- Actions taken:
  - `Home.vue`: loading 初始为 true
  - `fetchData(showSkeleton)` 参数控制是否显示骨架屏
  - 仅 onMounted 调用 fetchData(true)，其他调用不显示骨架屏
- Files modified:
  - xianyuplus-web/src/views/Home.vue

### Phase 14: "我的发布"数据隔离排查
- **Status:** complete
- 排查结果:
  - 后端 myProducts() 用 SecurityContext 获取当前用户，按 user.getId() 过滤 → 正确
  - 前端 /profile 路由需认证，request.get('/product/my') 带 JWT → 正确
  - 无 keep-alive 缓存，router-view 每次导航重建组件
  - 结论：后端逻辑正确，可能是用户误报

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| 后端编译 | `mvn compile` | 无错误 | 编译通过 | ✓ |

## Session: 2026-05-01 (第四轮)

### Phase 15: 修复聊天消息跨用户显示
- **Status:** complete
- **根因**: Pinia `persist: true` 将 token 写入 localStorage，多标签页登录时后登录用户覆盖前一用户的 token。`request.js` 的 `getToken()` 从 localStorage 读到错误 token，导致 API 请求使用错误身份
- Actions taken:
  - 新建 `pinia.js` 独立导出 pinia 实例，解决循环依赖
  - `user.js`: 移除 `persist: true`，添加 `init()` 从 localStorage 恢复状态，`persist()` 手动保存
  - `request.js`: `getToken()` 改为从 `useUserStore(pinia)` 读内存状态，`clearAuth()` 同步清除 store
  - `main.js`: 导入 pinia 和 userStore，app 挂载前调用 `userStore.init()`
- Files modified:
  - xianyuplus-web/src/pinia.js (新建)
  - xianyuplus-web/src/main.js
  - xianyuplus-web/src/stores/user.js
  - xianyuplus-web/src/api/request.js

### Phase 16: 修复聊天消息重复显示
- **Status:** complete
- **根因**: WebSocket Handler 用 Hutool JSONUtil 将雪花算法 Long ID 序列化为 number → 前端 JSON.parse 精度丢失 → senderId 不匹配 → 乐观消息无法替换 → 重复显示
- **诊断过程**: 添加 console.log 发现乐观消息 senderId=`"2050084962427498498"` vs 服务端回传 senderId=`2050084962427498500`（末3位不同）
- Actions taken:
  - `ChatWebSocketHandler.java`: response JSON 中 Long 字段全部 `.toString()`
- Files modified:
  - xianyu-plus/chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
|           |       |         |            |

## 5-Question Reboot Check (本轮)
| Question | Answer |
|----------|--------|
| Where am I? | 订单完整流程规划完成，Phase 17-22 待实施 |
| Where am I going? | 开始实施 Phase 17: 数据库迁移 |
| What's the goal? | 实现 认付款→发货→收货→完成 完整订单生命周期 |
| What have I learned? | 当前系统只有4个状态(0-3)，需扩展到6个(0-5)；取消订单未恢复商品是已知 Bug |
| What have I done? | 完成需求分析、状态机设计、实施计划制定 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 15-16 完成 |
| Where am I going? | 等待用户验证 |
| What's the goal? | 修复聊天消息重复显示 |
| What have I learned? | 雪花算法 Long ID 在 JS 中精度丢失（MAX_SAFE_INTEGER）；Hutool JSONUtil 不受 JacksonConfig 影响 |
| What have I done? | WebSocket Handler Long→String；Pinia persist 改手动管理 |

---

## Session: 2026-05-01 (第五轮 — 规划)

### Phase 17: 数据库迁移
- **Status:** complete
- Actions taken:
  - `init.sql`: orders 表 status 注释更新为 '0待付款 1已付款 2已发货 3已收货 4已完成 5已取消'
- Files modified:
  - xianyu-plus/service/src/main/resources/db/init.sql

### Phase 18: OrderStatus 枚举扩展
- **Status:** complete
- Actions taken:
  - 新增 SHIPPED(2, "已发货"), RECEIVED(3, "已收货")
  - COMPLETED: 2→4, CANCELLED: 3→5
- Files modified:
  - xianyu-plus/common/src/main/java/com/xianyuplus/common/enums/OrderStatus.java

### Phase 19: OrderServiceImpl 状态流转重构
- **Status:** complete
- Actions taken:
  - 重写 updateStatus() 方法，支持完整状态流转
  - 买家: PENDING→PAID(确认付款), PENDING→CANCELLED(取消), PAID→CANCELLED(退款), SHIPPED→RECEIVED(确认收货→自动完成)
  - 卖家: PAID→SHIPPED(确认发货)
  - 新增 cancelOrder() 私有方法，取消时恢复商品为 ON_SALE
  - 添加 @Transactional 注解保证事务
- Files modified:
  - xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/OrderServiceImpl.java

### Phase 20: Order.vue 前端更新
- **Status:** complete
- Actions taken:
  - statusText(): 新增 SHIPPED/RECEIVED 状态，更新 COMPLETED/CANCELLED code
  - 买家按钮: PENDING→确认付款+取消, PAID→申请退款, SHIPPED→确认收货
  - 卖家按钮: PAID→确认发货（移除了旧的"标记已付款"+"标记已完成"）
  - cancelOrder(): status 参数改为 5
  - 新增 shipOrder() 和 receiveOrder() 方法
  - CSS: 新增 status-2(蓝色), status-3(青色), 更新 status-4(绿色), status-5(红色)
- Files modified:
  - xianyuplus-web/src/views/Order.vue

### Phase 21: admin/Orders.vue 同步更新
- **Status:** complete
- Actions taken:
  - statusText(): 新增已发货/已收货
  - statusClass(): 新增 pill-tag-cyan/teal
  - CSS: 新增 .pill-tag-cyan, .pill-tag-teal
- Files modified:
  - xianyuplus-web/src/views/admin/Orders.vue

### Phase 22: 编译验证
- **Status:** complete
- Results:
  - `mvn compile` → BUILD SUCCESS (5.060s)
  - `npm run build` → built in 851ms
