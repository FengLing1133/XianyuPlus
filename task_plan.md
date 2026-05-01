# Task Plan: XianyuPlus — 订单完整流程

## Goal
实现订单完整生命周期：买家确认付款 → 卖家发货 → 买家确认收货 → 订单完成。同时修复取消订单不恢复商品的 Bug。

## Current Phase
Phase 17 — 待开始

## Phases (历史)

### Phase 1-16: 历史修复 (已完成)
- **Status:** complete

## Phases (新需求)

### Phase 17: 数据库迁移 — 更新订单状态枚举
- [x] 修改 `init.sql`: 更新 orders 表 status 注释为 '0待付款 1已付款 2已发货 3已收货 4已完成 5已取消'
- **Status:** complete

### Phase 18: 后端 — 扩展 OrderStatus 枚举
- [x] `OrderStatus.java`: 新增 SHIPPED(2,"已发货"), RECEIVED(3,"已收货"), 修改 COMPLETED(4), CANCELLED(5)
- **Status:** complete

### Phase 19: 后端 — 更新 OrderServiceImpl 状态流转逻辑
- [x] `updateStatus()`: 新增状态转换规则
  - 买家: PENDING→PAID, PENDING→CANCELLED, PAID→CANCELLED(退款), SHIPPED→RECEIVED→COMPLETED(自动)
  - 卖家: PAID→SHIPPED
- [x] 新增 `cancelOrder()` 私有方法：取消/退款时恢复商品状态为 ON_SALE
- **Status:** complete

### Phase 20: 前端 — 更新 Order.vue 状态显示和按钮
- [x] `statusText()`: 新增 SHIPPED(已发货), RECEIVED(已收货), 更新 COMPLETED/CANCELLED code
- [x] 买家按钮: PENDING→确认付款+取消, PAID→申请退款, SHIPPED→确认收货
- [x] 卖家按钮: PAID→确认发货
- [x] 状态标签样式: 新增 status-2(蓝色已发货), status-3(青色已收货), status-4(绿色已完成), status-5(红色已取消)
- **Status:** complete

### Phase 21: 前端 — 更新 admin/Orders.vue 状态映射
- [x] 状态标签 code 和文本与用户端同步
- [x] 新增 pill-tag-cyan, pill-tag-teal 样式
- **Status:** complete

### Phase 22: 编译验证
- [x] `mvn compile` 后端编译通过 (BUILD SUCCESS)
- [x] `npm run build` 前端构建通过 (851ms)
- **Status:** complete

## Key Questions
1. COMPLETED 是否自动触发？→ 可选：买家确认收货后自动变为已完成，或增加延时
2. 退款场景是否需要？→ PAID 状态允许买家取消（模拟退款）
3. 已发货后卖家能否取消？→ 不允许，只有买家可以操作

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 新增 SHIPPED/RECEIVED 两状态 | 完整模拟真实交易流程 |
| 取消恢复商品状态 | 避免商品被锁定无法再售 |
| RECEIVED→COMPLETED 自动 | 简化流程，确认收货即完成 |
| 买家 PAID→CANCELLED 允许 | 模拟退款场景 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
|       |         |            |

## Notes
- 状态 code 变化: COMPLETED 2→4, CANCELLED 3→5, 新增 SHIPPED=2, RECEIVED=3
- 需要数据库迁移脚本处理已有数据
