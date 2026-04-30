# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XianyuPlus (闲鱼Plus) is a campus second-hand goods trading platform — a course/graduation project. It consists of a Spring Boot multi-module backend and a Vue 3 frontend.

## Development Commands

### Backend (Maven, Java 1.8)

```bash
# Build entire project
cd xianyu-plus && mvn clean package -DskipTests

# Run the service module (main class: com.xianyuplus.service.StartApplication, port 8080)
cd xianyu-plus && mvn -pl service spring-boot:run

# Compile only
mvn compile
```

### Frontend (Vite + Vue 3)

```bash
cd xianyuplus-web && npm install
npm run dev        # Vite dev server on port 5173, proxies /api → localhost:8080
npm run build      # Production build
```

### Database

- MySQL 8.0, database name `xianyu_plus`. Run `xianyu-plus/service/src/main/resources/db/init.sql` to initialize (creates all tables + default categories + admin user).
- Redis on localhost:6379.
- Default admin account: `admin` / `admin123`

## Architecture

### Backend Maven Multi-Module Structure

```
xianyu-plus/
├── common/       — Shared entities, DTOs, enums, exceptions, Result/PageResult utils
├── framework/    — Infrastructure: Spring Security + JWT, CORS, MyBatisPlus, Redis config, global exception handler
├── service/      — Core business logic: Auth, User, Product, Category, Favorite, Order, File upload APIs. Contains StartApplication (entry point)
├── chat/         — WebSocket chat (Spring WebSocket), ChatWebSocketHandler, ChatHandshakeInterceptor, ChatService
└── admin/        — Admin dashboard APIs (user/product/order management, requires ADMIN role)
```

**Dependency chain**: `common` ← `framework` ← `chat` (chat also depends on framework). `common` ← `framework` ← `service` ← `chat`, `admin`

### Key Design Patterns

- **Result wrapper**: All API responses use `Result<T>` — `{ code: 200, message: "success", data: T }`. Controllers return `Result<?>`.
- **Pagination**: `PageDTO` for request params (page, size), `PageResult<T>` for response (total, page, size, records). Built on MyBatis-Plus `Page`.
- **Current user**: Retrieved from `SecurityContextHolder.getContext().getAuthentication()` (JWT filter sets it as `UsernamePasswordAuthenticationToken`).
- **Business exceptions**: Throw `BusinessException(code, message)` — caught by `GlobalExceptionHandler` (@RestControllerAdvice).
- **ID generation**: MyBatis-Plus `IdType.ASSIGN_ID` (Snowflake algorithm).
- **Mapper scanning**: `@MapperScan("com.xianyuplus.**.mapper")` in MyBatisPlusConfig — all mapper interfaces are in the `common` module (`com.xianyuplus.common.mapper`).

### Authentication Flow

1. `/api/auth/login`, `/api/auth/register`, GET `/api/product`, GET `/api/category`, `/ws/**`, `/uploads/**` are public.
2. `/api/admin/**` requires ADMIN role (role=1).
3. All other endpoints require a valid JWT token in `Authorization: Bearer <token>` header.
4. JWT is generated via `JwtTokenUtil` (HMAC-SHA256, 7-day expiration). Contains userId, username, role in claims.
5. `JwtTokenFilter` (OncePerRequestFilter) extracts and validates the token on every request.

### WebSocket Chat

- Endpoint: `ws://localhost:8080/ws/chat/{userId}?token=<jwt>`
- `ChatHandshakeInterceptor` validates JWT before upgrading to WebSocket.
- Online users tracked in `ConcurrentHashMap<Long, WebSocketSession>` + Redis `online_users` set.
- Messages saved to MySQL `message` table, then delivered in real-time if receiver is online.
- Redis Pub/Sub broadcasts messages for multi-instance support.

### Backend Configuration Files

- `service/src/main/resources/application.yml` — server port, datasource, Redis, file upload path, JWT secret/expiration
- `service/src/main/resources/db/init.sql` — full schema + seed data

### Frontend Structure

```
xianyuplus-web/src/
├── api/request.js     — Axios instance with token interceptor and 401 redirect
├── router/index.js    — Routes with auth/admin guards (beforeEach)
├── stores/user.js     — Pinia store (persisted: token, userInfo)
├── stores/chat.js     — Pinia store (WebSocket connection, messages)
├── components/        — Layout, AdminLayout, ProductCard, ImageUploader
├── views/             — Home, Login, Register, ProductDetail, Publish, Profile, Chat, Order, Favorite
└── views/admin/       — Dashboard, Users, Products, Orders
```

- **Layout switching**: App.vue renders `<Layout>` for normal routes, `<AdminLayout>` for `/admin/*` routes.
- **Route guards**: `beforeEach` checks `meta.auth` (requires login) and `meta.admin` (requires role=1). Logged-in users are redirected away from login/register.
- **API proxy**: Vite dev server proxies `/api` → `http://localhost:8080`, `/ws` → `ws://localhost:8080`, `/uploads` → `http://localhost:8080`.
