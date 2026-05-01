# Findings & Decisions

## Requirements
- 修复商品图片部分不能显示的问题
- 修复多用户不能同时使用前端的问题
- 修复商品详情页显示"商品不存在"的问题

## Research Findings

### Bug 1: 商品图片显示问题
- `file.upload-path: ./uploads` 使用相对路径，JVM 工作目录变化会导致图片 404
- Vite 代理 `/uploads` 仅开发环境生效，生产环境需要 Nginx 配置
- `ImageUploader.vue` 使用原生 fetch 而非统一 axios 实例
- 所有 `<img>` 标签无 `@error` fallback 处理
- `ProductDetail.vue` 主图用 `object-fit: contain`，`ProductCard.vue` 用 `cover`，风格不一致

### Bug 2: 多用户并发问题
- **根因**: `stores/user.js` 同时使用手动 localStorage + pinia-plugin-persistedstate 双重持久化，后登录用户覆盖先登录用户的 token
- `localStorage` 在同一浏览器同一 origin 下全局共享，不同标签页登录不同账号会互相覆盖
- WebSocket 按 userId 做单例映射，同用户多设备连接会互相踢掉
- Axios 401 处理只清 localStorage 未同步清 Pinia store
- JWT 无吊销机制，7 天有效期

### Bug 3: 商品详情页"商品不存在"
- **根因**: `init.sql` 中没有任何商品 INSERT 语句，数据库中无商品数据
- MyBatis-Plus 雪花 ID (18-19位) 超出 JS `Number.MAX_SAFE_INTEGER` (16位)，存在精度丢失风险
- 前端 catch 块只 console.error，无用户提示

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| 待决定 | 待决定 |

### Bug 4: LocalDateTime 序列化异常
- **根因**: `JacksonConfig.java` 自建 `ObjectMapper` bean 覆盖了 Spring Boot 自动配置的 ObjectMapper，丢失了默认注册的 JSR310 模块
- `jackson-datatype-jsr310` 通过 Spring Boot starter 已在 classpath 上，但被自定义 bean 覆盖
- 所有 Entity 的 `createdAt`/`updatedAt` (LocalDateTime 类型) 都无法序列化

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| LocalDateTime 序列化失败 | 改用 Jackson2ObjectMapperBuilderCustomizer 定制而非替换 ObjectMapper |

## Resources
- 前端: `xianyuplus-web/src/`
- 后端: `xianyu-plus/`
- 数据库初始化: `xianyu-plus/service/src/main/resources/db/init.sql`

---
*Update this file after every 2 view/browser/search operations*
