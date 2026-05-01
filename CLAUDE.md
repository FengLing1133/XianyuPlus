# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Please always communicate, explain code, and answer questions in **Chinese**.

## Project Overview

XianyuPlus (闲鱼Plus) is a campus second-hand goods trading platform — a course/graduation project. It consists of a Spring Boot multi-module backend and a Vue 3 frontend.

## Development Commands

### Backend (Maven, Java 1.8)

Key dependencies (managed in root pom): Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, Hutool 5.8.23, jjwt 0.9.1, Knife4j 4.3.0, Lombok 1.18.38. All modules use Lombok — entities/DTOs use `@Data`, services/controllers use `@RequiredArgsConstructor` for dependency injection.

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
├── service/      — Core business logic: Auth, User, Product, Category, Favorite, Order, Review, Report, Notification, Message, File upload APIs. Contains StartApplication (entry point)
├── chat/         — WebSocket chat (Spring WebSocket), ChatWebSocketHandler, ChatHandshakeInterceptor, ChatService
└── admin/        — Admin dashboard APIs (user/product/order/report management, requires ADMIN role)
```

**Dependency chain**: `common` ← `framework` ← `chat`, `admin`. `service` depends on all: `common`, `framework`, `chat`, `admin`.

### Key Design Patterns

- **Result wrapper**: All API responses use `Result<T>` — `{ code: 200, message: "success", data: T }`. Controllers return `Result<?>`.
- **Pagination**: `PageDTO` for request params (page, size), `PageResult<T>` for response (total, page, size, records). Built on MyBatis-Plus `Page`.
- **Current user**: Retrieved from `SecurityContextHolder.getContext().getAuthentication()` (JWT filter sets it as `UsernamePasswordAuthenticationToken`).
- **Business exceptions**: Throw `BusinessException(code, message)` — caught by `GlobalExceptionHandler` (@RestControllerAdvice).
- **ID generation**: MyBatis-Plus `IdType.ASSIGN_ID` (Snowflake algorithm).
- **Mapper scanning**: `@MapperScan("com.xianyuplus.**.mapper")` in MyBatisPlusConfig — all mapper interfaces are in the `common` module (`com.xianyuplus.common.mapper`).

### Authentication Flow

1. `/api/auth/login`, `/api/auth/register`, GET `/api/product`, GET `/api/category`, `/ws/**`, `/uploads/**` are public.
2. `/api/admin/**` (including `/api/admin/report/**`) requires ADMIN role (role=1).
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
- **API docs** (Knife4j/Swagger): http://localhost:8080/doc.html
- **File uploads**: max 10MB per file, 20MB per request. Path configured in `application.yml` (`file.upload-path`), served at `/uploads/**`.
- **SQL logging**: MyBatis-Plus prints all SQL to stdout (`StdOutImpl`), so you'll see every query in the backend console.
- **Map underscore to camelCase**: Enabled — DB columns like `user_id` auto-map to Java `userId`.

### Frontend Structure

```
xianyuplus-web/src/
├── api/               — request.js (Axios instance), notification.js, report.js, review.js
├── router/index.js    — Routes with auth/admin guards (beforeEach)
├── stores/            — user.js (auth), chat.js (WebSocket), notification.js
├── components/        — Layout, AdminLayout, ProductCard, ImageUploader, ReviewForm, ReviewList, ReportForm, NotificationBell
├── views/             — Home, Login, Register, ProductDetail, Publish, Profile, Chat, ChatWindow, Order, Favorite
├── views/admin/       — Dashboard, Users, Products, Orders, Reports
└── utils/             — toast.js, dialog.js, category.js
```

- **Layout switching**: App.vue renders `<Layout>` for normal routes, `<AdminLayout>` for `/admin/*` routes.
- **Route guards**: `beforeEach` checks `meta.auth` (requires login) and `meta.admin` (requires role=1). Logged-in users are redirected away from login/register.
- **API proxy**: Vite dev server proxies `/api` → `http://localhost:8080`, `/ws` → `ws://localhost:8080`, `/uploads` → `http://localhost:8080`.
- **Publish.vue** serves double-duty: `/publish` (create) and `/edit/:id` (edit) — the component detects an `id` route param to switch between create/edit mode.
- **Toast utility** (`@/utils/toast`): Used for user-facing error/success messages. The Axios response interceptor in `request.js` automatically shows Toast errors for failed requests (unless `config.silent` is set).
- **Silent requests**: Set `silent: true` on an Axios request config to suppress automatic error Toasts (useful for polling or background requests).
- **Review system**: Buyers can rate and comment on products after order completion. Sellers can reply to reviews. Both can delete their own content.
- **Report system**: Users can report products or users. Reports are managed by admins via `/admin/reports`.
- **Notification system**: Bell icon in header shows unread count. Notification store tracks messages in real-time.
- **Chat routing**: `/chat` shows conversation list, `/chat/:userId` opens a specific chat window (ChatWindow.vue).
