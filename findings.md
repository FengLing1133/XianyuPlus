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
| Jackson Long→String 序列化导致 senderId 比较失败 | 模板和 onmessage 中用 String() 包装比较 |
| 乐观消息 senderId 为 null 导致全部显示在左边 | send() 增加 senderId 参数，由调用方传入 |
| 切换分类骨架屏闪烁 | 仅首次加载显示骨架屏，后续切换不显示 |
| "我的发布"数据隔离 | 后端 myProducts() 已按 user.getId() 过滤，前端 /product/my 需认证，逻辑正确 |

### Bug 10: 聊天消息方向 + 接收
- 乐观消息 senderId=null → 全部显示在左边
- JacksonConfig 将 Long 序列化为 String → === 比较失败
- onmessage 中 currentUserId 未在 store 作用域内

### Bug 11: 分类切换骨架屏闪烁
- 每次 fetchData() 都 loading=true → 骨架屏闪现
- 仅首次加载需要骨架屏

### Bug 12: "我的发布"数据隔离
- 后端 myProducts() 用 SecurityContext 获取当前用户，按 user.getId() 过滤 → 正确
- 前端 /profile 路由需认证，request.get('/product/my') 带 JWT → 正确
- 无 keep-alive 缓存，router-view 每次导航重建组件
- 结论：后端逻辑正确，用户可能误报或存在其他未发现的缓存问题

### Bug 13: 聊天消息重复显示（用户111发消息后两边都显示）
- **现象**: 用户111给test1发消息，111界面显示双方都发了相同内容；test1只收到消息
- **根因**: WebSocket Handler 用 Hutool JSONUtil 将 Long (senderId/id) 序列化为 number，雪花算法 ID 超出 JS Number.MAX_SAFE_INTEGER → JSON.parse 精度丢失 → senderId 不匹配 → 乐观消息替换失败 → 服务端回传被当作新消息 push
- **数据**: senderId `2050084962427498498` → JSON.parse → `2050084962427498500` (末3位丢失)
- **为什么 REST API 没问题**: JacksonConfig 将 Long 序列化为 String，但 WebSocket Handler 用 Hutool JSON 不受 Jackson 影响
- **修复**: ChatWebSocketHandler.java 中将 Long 字段 `.toString()` 后再 set 到 JSON

### Bug 14: Pinia persist 多标签页 token 覆盖（潜在问题）
- **现象**: 同浏览器不同标签页登录不同用户，后登录覆盖 localStorage → 先登录用户的 API 请求用错误 token
- **修复**: 
  1. 新建 `pinia.js` 独立导出 pinia 实例
  2. `user.js` 移除 `persist: true`，改为手动 `init()`/`persist()` 管理 localStorage
  3. `request.js` 的 `getToken()` 从 Pinia 内存状态读 token
  4. `main.js` 在 app 挂载前调用 `userStore.init()` 恢复登录状态

## Resources
- 前端: `xianyuplus-web/src/`
- 后端: `xianyu-plus/`
- 数据库初始化: `xianyu-plus/service/src/main/resources/db/init.sql`
