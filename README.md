# 闲鱼Plus (XianyuPlus)

校园二手物品交易平台 — 课程/毕业设计项目。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| 前端框架 | Vue 3 + Vite + Element Plus |
| 数据库 | MySQL 8.0 |
| ORM | MyBatis-Plus 3.5 |
| 缓存 | Redis |
| 认证 | Spring Security + JWT |
| 即时通讯 | WebSocket + Redis Pub/Sub |
| 构建工具 | Maven |

## 环境要求

- **JDK** 1.8+
- **Maven** 3.6+
- **Node.js** 16+（推荐 18+）
- **MySQL** 8.0
- **Redis** 6.0+

## 项目结构

```
XianyuPlus/
├── xianyu-plus/                  # 后端 Maven 多模块项目
│   ├── common/                   # 公共模块：实体类、DTO、枚举、异常、统一返回
│   ├── framework/                # 基础设施：Spring Security、JWT、CORS、MyBatis-Plus、Redis
│   ├── service/                  # 核心业务：启动入口、用户、商品、分类、收藏、订单、文件上传
│   ├── chat/                     # WebSocket 即时通讯模块
│   └── admin/                    # 管理后台 API（需要 ADMIN 角色）
└── xianyuplus-web/               # 前端 Vue 3 项目
    └── src/
        ├── api/                  # Axios 封装 + 接口定义
        ├── router/               # 路由配置（含权限守卫）
        ├── stores/               # Pinia 状态管理（用户、聊天）
        ├── components/           # 公共组件
        ├── views/                # 页面视图
        └── views/admin/          # 管理后台页面
```

## 快速启动

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd XianyuPlus
```

### 2. 初始化数据库

确保 MySQL 8.0 已安装并运行，然后执行初始化脚本：

```bash
mysql -u root -p < xianyu-plus/service/src/main/resources/db/init.sql
```

或者用 Navicat、DBeaver 等工具打开 `init.sql` 文件执行。

脚本会自动完成以下操作：
- 创建数据库 `xianyu_plus`（字符集 utf8mb4）
- 创建所有业务表（user、category、product、product_image、favorite、orders、message）
- 插入 16 条商品分类数据
- 创建默认管理员账号

### 3. 安装并启动 Redis

**Windows：**

```bash
# 下载 Redis for Windows (https://github.com/tporadowski/redis/releases)
# 解压后运行
redis-server.exe
```

**macOS：**

```bash
brew install redis
brew services start redis
```

**Linux：**

```bash
sudo apt install redis-server    # Ubuntu/Debian
sudo systemctl start redis
```

默认端口 6379，无需密码。

### 4. 配置数据库连接

编辑 `xianyu-plus/service/src/main/resources/application.yml`，修改数据库用户名和密码为你本机的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xianyu_plus?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root        # 改为你的 MySQL 用户名
    password: root        # 改为你的 MySQL 密码
```

### 5. 启动后端

```bash
cd xianyu-plus

# 首次运行先编译整个项目（跳过测试）
mvn clean package -DskipTests

# 启动 service 模块（端口 8080）
mvn -pl service spring-boot:run
```

看到类似以下日志表示启动成功：

```
Started StartApplication in X.XXX seconds
```

后端接口文档（Knife4j/Swagger）：http://localhost:8080/doc.html

### 6. 启动前端

打开**新的终端窗口**：

```bash
cd xianyuplus-web

# 安装依赖（仅首次需要）
npm install

# 启动开发服务器
npm run dev
```

启动后访问 http://localhost:5173 即可进入网站。

Vite 已配置代理，前端请求 `/api` 会自动转发到后端的 `http://localhost:8080`。

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

普通用户可直接在注册页面注册。

## 功能模块

- **用户模块**：注册、登录、个人信息管理
- **商品模块**：发布商品、浏览商品、搜索/分类筛选、商品详情
- **收藏模块**：收藏/取消收藏商品
- **订单模块**：下单购买、订单管理
- **即时通讯**：买家卖家在线聊天（WebSocket）
- **管理后台**：用户管理、商品管理、订单管理（需管理员账号登录）

## API 接口一览

| 接口路径 | 说明 | 认证 |
|----------|------|------|
| POST /api/auth/login | 用户登录 | 无 |
| POST /api/auth/register | 用户注册 | 无 |
| GET /api/product | 商品列表（支持分页+分类筛选） | 无 |
| GET /api/product/{id} | 商品详情 | 无 |
| GET /api/category | 分类列表 | 无 |
| POST /api/product | 发布商品 | JWT |
| POST /api/favorite | 收藏商品 | JWT |
| POST /api/order | 创建订单 | JWT |
| GET /api/user/info | 获取用户信息 | JWT |
| /ws/chat/{userId} | WebSocket 聊天 | JWT(token参数) |
| /api/admin/** | 管理后台接口 | ADMIN角色 |

## 常见问题

### MySQL 连接失败

- 确认 MySQL 服务已启动
- 检查 `application.yml` 中的用户名和密码是否正确
- 确认已执行 `init.sql` 创建数据库

### Redis 连接失败

- 确认 Redis 服务已启动：`redis-cli ping` 返回 `PONG`
- Windows 下建议使用 [Redis for Windows](https://github.com/tporadowski/redis/releases)

### 前端页面空白 / 接口 404

- 确认后端已启动且运行在 8080 端口
- 检查浏览器控制台是否有跨域错误
- 确认 Vite 代理配置正确（`vite.config.js`）

### 端口被占用

```bash
# Windows 查看端口占用
netstat -ano | findstr :8080
netstat -ano | findstr :5173

# macOS/Linux 查看端口占用
lsof -i :8080
lsof -i :5173
```

### Maven 编译报错

- 确认 JDK 版本 >= 1.8：`java -version`
- 清理 Maven 缓存重试：`mvn clean package -DskipTests -U`
