# 校园二手物品交易平台 — 设计文档

## 项目概述

**项目名称**：XianyuPlus（闲鱼Plus）

**定位**：课程设计/毕设项目，以功能展示和技术完整性为主。

**目标用户**：校内学生，用于发布和购买二手物品。

## 技术选型

| 层面   | 技术                             |
| ------ | -------------------------------- |
| 前端   | Vue 3 + Element Plus + Pinia + Vue Router + Axios |
| 后端   | Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 + Redis                 |
| 聊天   | WebSocket（Spring WebSocket） + Redis Pub/Sub |
| 图片   | 本地文件系统，预留 OSS 接口       |
| 构建   | Vite（前端） + Maven（后端）      |

## 功能范围

完整版：用户注册登录、商品发布/浏览/搜索、商品详情、收藏、下单购买、订单管理、买家卖家即时通讯（WebSocket）、管理后台（用户管理、商品审核、数据统计）。

## 系统架构

### 模块划分（Maven 多模块）

```
xianyu-plus/
├── common/        # 公共类：实体、DTO、枚举、异常、工具类
├── framework/     # 基础设施：Security+JWT、Redis配置、WebSocket配置、全局异常、跨域
├── service/       # 核心业务：用户、商品、收藏、订单、文件上传
├── chat/          # 即时通讯：WebSocket + Redis Pub/Sub
└── admin/         # 管理后台接口：用户管理、商品审核、数据统计
```

### 架构图

```
浏览器 ─── Vue 3 前端 ─── HTTP/WS ─── Spring Boot 多模块 ─── MySQL + Redis
```

## 数据库设计

### 表清单

**user** — 用户表：id, username, password(BCrypt), nickname, avatar, phone, role(0用户/1管理员), status(0正常/1封禁)

**category** — 分类表：id, name, parent_id（支持两级分类）

**product** — 商品表：id, title, description, price, original_price, category_id, user_id(卖家), condition(1全新/2几乎全新/3轻微使用/4明显痕迹), status(1在售/2已售出/3已下架), view_count

**product_image** — 商品图片表：id, product_id, url, sort_order（首图为封面）

**favorite** — 收藏表：id, user_id, product_id, created_at

**orders** — 订单表：id, buyer_id, seller_id, product_id, amount, status(待付款/已付款/已完成/已取消), created_at

**message** — 消息表：id, sender_id, receiver_id, product_id, content, is_read, created_at

## API 设计

统一响应格式：`{ "code": 200, "message": "success", "data": {} }`

### 认证模块 — /api/auth
- POST `/api/auth/register` — 注册
- POST `/api/auth/login` — 登录，返回 JWT
- GET `/api/auth/info` — 当前用户信息（需认证）
- PUT `/api/auth/password` — 修改密码（需认证）

### 用户模块 — /api/user
- GET `/api/user/{id}` — 用户详情
- PUT `/api/user/profile` — 编辑资料（需认证）
- POST `/api/user/avatar` — 上传头像（需认证）

### 商品模块 — /api/product
- GET `/api/product` — 列表（分页+搜索+分类+排序），公开
- GET `/api/product/{id}` — 详情，公开
- POST `/api/product` — 发布（需认证）
- PUT `/api/product/{id}` — 编辑（仅发布者）
- PUT `/api/product/{id}/status` — 上下架（仅发布者）
- GET `/api/product/my` — 我的发布（需认证）

### 收藏 — /api/favorite
- POST `/api/favorite/{productId}` — 收藏/取消切换（需认证）
- GET `/api/favorite` — 我的收藏（需认证）

### 订单 — /api/order
- POST `/api/order` — 创建订单（需认证）
- GET `/api/order?type=buy|sell` — 订单列表（需认证）
- PUT `/api/order/{id}/status` — 更新状态（需认证）

### 聊天 — /api/message + WebSocket
- ws `/ws/chat/{userId}` — WebSocket 连接（需 token 参数鉴权）
- GET `/api/message/{userId}` — 历史消息
- GET `/api/message/unread` — 未读消息数

### 文件 — /api/file
- POST `/api/file/upload` — 上传图片，返回 URL

### 分类 — /api/category
- GET `/api/category` — 分类树

### 管理后台 — /api/admin/**
需要 ADMIN 角色，含用户管理、商品审核、数据概览。

### 认证说明
匿名接口：登录、注册、商品列表、商品详情、分类列表。其余接口在请求头携带 `Authorization: Bearer <token>`，由 Spring Security 过滤器统一校验。

## 前端设计

### 页面清单

**用户端**：首页（商品列表+搜索+分类）、商品详情、发布/编辑商品、聊天（会话列表+聊天窗口）、收藏、订单管理（我买到/我卖出）、个人中心、登录/注册

**管理端**（同一项目，`/admin/*` 路由，独立布局）：仪表盘、用户管理、商品管理、订单管理

### Pinia 状态管理
- `user` — 用户信息 + token（pinia-plugin-persistedstate 持久化）
- `chat` — WebSocket 连接管理 + 消息实时接收
- `notification` — 未读消息数

### 路由守卫
- beforeEach：无 token 跳转登录页
- /admin/*：额外校验 role === 1（管理员）
- /login、/register：已登录则跳转首页

## 聊天系统

消息流转：发送 → 存 MySQL → Redis Pub/Sub 广播 → 在线用户实时接收

- 连接鉴权：WebSocket 握手阶段校验 JWT
- 在线状态：Redis 维护 userId → sessionId 映射
- 离线消息：上线后从 MySQL 拉取未读消息
- 未读数：前端轮询 + WebSocket 推送更新事件

## 非功能设计

- **ID 生成**：MyBatis-Plus 雪花算法（ASSIGN_ID）
- **密码加密**：BCryptPasswordEncoder
- **分页**：MyBatis-Plus PageHelper，统一 PageResult 返回
- **异常处理**：@RestControllerAdvice 全局捕获
- **跨域**：Spring CORS + Vite 开发代理
- **数据初始化**：SQL 脚本预置分类数据和 admin 账号

## 项目启动

| 组件   | 启动方式                                         |
| ------ | ------------------------------------------------ |
| MySQL  | 本地安装，执行建表 SQL，创建数据库 xianyu_plus    |
| Redis  | 本地安装，默认 6379                               |
| 后端   | IDEA 导入 Maven 项目，运行主启动类，端口 8080     |
| 前端   | `npm install && npm run dev`，Vite 端口 5173      |
