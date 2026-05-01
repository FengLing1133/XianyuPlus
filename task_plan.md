# Task Plan: XianyuPlus — 第二轮修复

## Goal
修复XianyuPlus第二轮5个问题：聊天功能、订单付款、商品缩略图、分类筛选、暂无商品居中

## Current Phase
Phase 14 abgeschlossen — auf neue Aufgaben warten

## Phases

### Phase 1-6: 第一轮Bug修复 (已完成)
- **Status:** complete

### Phase 7: 修复对话功能 — 消息发送不了
- [x] chat.js: 乐观更新 + WebSocket URL 相对路径 + 连接复用
- [x] ChatWindow.vue: 重构消息管理 + 发送失败提示
- [x] ChatWebSocketHandler.java: 消息回传发送方 + null 安全
- **Status:** complete

### Phase 8: 订单确认付款功能
- [x] 后端 OrderServiceImpl: 允许买家 PENDING→PAID
- [x] 前端 Order.vue: 买家视角添加"确认付款"按钮
- **Status:** complete

### Phase 9: 修复商品缩略图显示不完全
- [x] ProductCard.vue: aspect-ratio: 4/3 + object-fit: contain
- **Status:** complete

### Phase 10: 修复分类筛选功能缺陷
- [x] ProductServiceImpl: 父分类查询时自动包含子分类 IN 查询
- **Status:** complete

### Phase 11: "暂无商品"图片和文本居中
- [x] Home.vue: grid-column: 1 / -1
- **Status:** complete

## Key Questions
1. WebSocket 连接是否建立成功？→ 已改为相对路径，走 Vite 代理
2. 订单状态有哪些？→ PENDING(0), PAID(1), COMPLETED(2), CANCELLED(3)
3. 分类是父子层级关系吗？→ 是，后端已支持 IN 查询

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 聊天乐观更新 | 发送方立即看到消息，后端回传后补充真实 id/createdAt |
| 分类 IN 查询 | 前端传父分类ID，后端自动查所有子分类，无需改前端 |
| object-fit: contain | 不裁切图片内容，可能有留白但信息完整 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
|       | 1       |            |

### Phase 12: 修复聊天消息方向 + 接收方收不到消息
- [x] chat.js: send() 增加 senderId 参数，乐观消息不再设 null
- [x] chat.js: connect() 保存 currentUserId，onmessage 用 String 比较
- [x] chat.js: onmessage 收到自己消息的回传时替换乐观消息（避免重复）
- [x] ChatWindow.vue: 传 currentUserId 给 send()，模板用 String() 比较
- **Status:** complete

### Phase 13: 修复首页切换分类卡片阴影闪烁
- [x] Home.vue: loading 初始为 true，fetchData 增加 showSkeleton 参数
- [x] 仅首次加载显示骨架屏，切换分类/筛选/翻页不显示
- **Status:** complete

### Phase 14: 修复个人中心"我的发布"数据隔离
- [x] 排查结果：后端 myProducts() 用 SecurityContext 按 user.getId() 过滤 → 正确
- [x] 前端 /profile 路由需认证，request.get('/product/my') 带 JWT → 正确
- [x] 结论：后端逻辑正确，无 keep-alive 缓存问题
- **Status:** complete

### Phase 15: 修复 Pinia persist 多标签页 token 覆盖
- [x] 新建 pinia.js 独立导出 pinia 实例
- [x] user.js: 移除 persist:true，改为手动 init()/persist()
- [x] request.js: getToken() 从 Pinia 内存状态读 token
- [x] main.js: app 挂载前调用 userStore.init()
- **Status:** complete

### Phase 16: 修复聊天消息重复显示（JS大整数精度丢失）
- [x] ChatWebSocketHandler.java: Long 字段 (id/senderId/receiverId/productId) 转 String 输出
- [x] 后端编译通过，前端构建通过
- **Status:** complete

## Notes
- Phase 7-16 全部完成
