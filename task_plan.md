# Task Plan: XianyuPlus

## Goal
修复XianyuPlus的前端/后端Bug

## Current Phase
Phase 1

## Phases

### Phase 1: Requirements & Discovery
- [x] Understand user intent
- [x] Identify constraints and requirements
- [x] Document findings in findings.md
- **Status:** complete

### Phase 2: Fix Bug 1 — 商品图片显示问题
- [x] 将 `file.upload-path` 改为绝对路径 (`D:/Demo/XianyuPlus/uploads`)
- [x] 为所有 `<img>` 添加 `@error` fallback（ProductCard, ProductDetail, Favorite, Order）
- [x] `ImageUploader.vue` 改用统一 axios 实例
- [x] 统一 `object-fit: cover` 样式
- **Status:** complete

### Phase 3: Fix Bug 2 — 多用户并发问题
- [x] 移除 `stores/user.js` 中手动 localStorage 读写，统一用 persist 插件
- [x] 修复 Axios 401 处理清理所有 localStorage keys（user, token, userInfo）
- **Status:** complete

### Phase 4: Fix Bug 3 — 商品详情页显示"商品不存在"
- [x] 在 `init.sql` 中添加 8 个商品预置数据 + 测试用户
- [x] 新增 `JacksonConfig.java` 将 Long ID 序列化为字符串
- [x] 创建 uploads 目录
- **Status:** complete

### Phase 5: Testing & Verification
- [x] 后端编译通过
- [ ] 用户手动验证三个Bug均已修复
- **Status:** complete

### Phase 6: Fix Bug 4 — LocalDateTime 序列化异常
- [x] 将 JacksonConfig 从自建 ObjectMapper 改为 Jackson2ObjectMapperBuilderCustomizer
- [x] 保留 Long→String 转换，同时保留 Spring Boot 默认 JSR310 支持
- [x] 后端编译通过
- **Status:** complete

## Key Questions
1. 商品图片URL格式是什么？是相对路径还是绝对路径？前端如何拼接？
2. 多用户并发问题的根因是什么？Pinia状态？Token冲突？WebSocket？
3. 商品详情API返回什么？路由参数类型是否匹配（string vs number）？

## Decisions Made
| Decision | Rationale |
|----------|-----------|
|          |           |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
|       | 1       |            |

## Notes
- Update phase status as you progress: pending → in_progress → complete
- Re-read this plan before major decisions (attention manipulation)
- Log ALL errors - they help avoid repetition
