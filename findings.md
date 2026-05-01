# Findings & Decisions

## Requirements
1. 聊天功能消息发送不了
2. 订单页面添加确认付款功能（不接真实支付）
3. 商品缩略图显示不完全
4. 分类筛选缺陷：父分类下找不到子分类商品
5. "暂无商品"图片和文本居中

## Research Findings

### Bug 5: 聊天功能
- **根因**: `chatStore.send()` 发送后未将消息 push 到本地 messages 数组
- 后端 `ChatWebSocketHandler` 只发给接收方，不回传发送方
- WebSocket URL 硬编码 `ws://localhost:8080`，绕过 Vite 代理
- 每次进入聊天窗口都重建 WebSocket 连接
- 发送失败时无任何用户反馈
- productId 固定传 null

### Bug 6: 订单确认付款
- 当前状态流转：PENDING(0) → 卖家标记PAID(1) → 卖家标记COMPLETED(2)
- 买家只能取消订单，没有"确认付款"按钮
- 需要给买家添加"确认付款"按钮（PENDING→PAID）

### Bug 7: 缩略图显示不完全
- `ProductCard.vue` `.card-image` 高度固定 180px + `overflow: hidden` + `object-fit: cover`
- 非标准比例图片被裁切

### Bug 8: 分类筛选缺陷
- 后端 `ProductServiceImpl` 用 `eq` 精确匹配 `category_id`
- 商品关联到子分类(id=7,8,9,10)，父分类(id=1)查不到任何商品
- 前端只展示一级分类按钮，看不到子分类

### Bug 9: "暂无商品"居中
- `.empty-state` 在 `.product-grid`(4列Grid) 内只占1个单元格
- 需要 `grid-column: 1 / -1` 横跨所有列

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| 聊天：乐观更新 + 后端回传 | 发送方立即看到消息，后端确认后补充 id/createdAt |
| 分类：后端 IN 查询 | 前端传父分类ID，后端自动查所有子分类 |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
|       |            |

## Resources
- 前端: `xianyuplus-web/src/`
- 后端: `xianyu-plus/`
- 数据库初始化: `xianyu-plus/service/src/main/resources/db/init.sql`
