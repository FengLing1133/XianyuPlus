# Progress Log

## Session: 2026-05-01

### Phase 1: Requirements & Discovery
- **Status:** complete
- **Started:** 2026-05-01
- Actions taken:
  - 并行排查三个Bug的根因
  - 图片问题：相对路径 + 无 fallback
  - 多用户问题：双重 localStorage 持久化冲突
  - 商品详情：init.sql 无商品数据 + 雪花ID精度丢失
- Files created/modified:
  - task_plan.md (created)
  - findings.md (created)
  - progress.md (created)

### Phase 2: Fix Bug 1 — 商品图片显示问题
- **Status:** complete
- Actions taken:
  - `application.yml`: `file.upload-path` 改为 `D:/Demo/XianyuPlus/uploads`
  - `ProductCard.vue`: 添加 `imgError` ref + `@error` handler，placeholder 始终渲染在底层
  - `ProductDetail.vue`: 添加 `mainImgError` ref，`object-fit` 改为 `cover`，固定高度 440px
  - `Favorite.vue`: placeholder 始终渲染，img 加 `@error` 隐藏
  - `Order.vue`: 同上
  - `ImageUploader.vue`: 移除原生 fetch，改用统一 `request` (axios)
  - 创建 `uploads/products/` 目录
- Files modified:
  - xianyu-plus/service/src/main/resources/application.yml
  - xianyuplus-web/src/components/ProductCard.vue
  - xianyuplus-web/src/views/ProductDetail.vue
  - xianyuplus-web/src/views/Favorite.vue
  - xianyuplus-web/src/views/Order.vue
  - xianyuplus-web/src/components/ImageUploader.vue

### Phase 3: Fix Bug 2 — 多用户并发问题
- **Status:** complete
- Actions taken:
  - `stores/user.js`: 移除所有手动 `localStorage.getItem/setItem/removeItem`，完全依赖 `pinia-plugin-persistedstate`
  - `request.js`: 提取 `clearAuth()` 函数，同时清除 `user`、`token`、`userInfo` 三个 key
- Files modified:
  - xianyuplus-web/src/stores/user.js
  - xianyuplus-web/src/api/request.js

### Phase 4: Fix Bug 3 — 商品详情页"商品不存在"
- **Status:** complete
- Actions taken:
  - `init.sql`: 添加 8 个商品 + 2 个测试用户 + 8 条商品图片记录
  - 新增 `JacksonConfig.java`: Long/long 类型全局序列化为字符串，防止 JS 精度丢失
  - 后端编译验证通过
- Files modified:
  - xianyu-plus/service/src/main/resources/db/init.sql
  - xianyu-plus/framework/src/main/java/com/xianyuplus/framework/config/JacksonConfig.java (created)

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| 后端编译 | `mvn compile` | 无错误 | 编译通过 | ✓ |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
|           |       |         |            |

### Phase 6: Fix Bug 4 — LocalDateTime 序列化异常
- **Status:** complete
- Actions taken:
  - 将 `JacksonConfig.java` 从 `@Bean ObjectMapper` 改为 `@Bean Jackson2ObjectMapperBuilderCustomizer`
  - 这样保留 Spring Boot 自动配置的 ObjectMapper（含 JSR310 模块），仅追加 Long→String 转换
- Files modified:
  - xianyu-plus/framework/src/main/java/com/xianyuplus/framework/config/JacksonConfig.java

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 5 - 验证完成 |
| Where am I going? | 等待用户手动验证 |
| What's the goal? | 修复三个Bug：图片显示、多用户并发、商品详情页 |
| What have I learned? | 见 findings.md |
| What have I done? | 见上方各阶段记录 |

---
*Update after completing each phase or encountering errors*
