# Task Plan: XianyuPlus — 第二轮修复

## Goal
修复XianyuPlus第二轮5个问题：聊天功能、订单付款、商品缩略图、分类筛选、暂无商品居中

## Current Phase
Complete

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

## Notes
- 所有阶段已完成，等待用户验证
