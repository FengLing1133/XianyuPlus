# XianyuPlus Implementation Plan

## Phase 1: Project Scaffolding

### Task 1: Create Maven parent POM with multi-module structure

**Files to create:**

- `xianyu-plus/pom.xml` — Parent POM with Spring Boot 2.7.18, dependency management for all modules
- `xianyu-plus/common/pom.xml` — Plain jar, no Spring Boot parent
- `xianyu-plus/framework/pom.xml` — Spring Boot starter dependencies (security, redis, websocket)
- `xianyu-plus/service/pom.xml` — Runnable module, depends on all others
- `xianyu-plus/chat/pom.xml` — WebSocket handlers
- `xianyu-plus/admin/pom.xml` — Admin controllers

**Parent POM** (`pom.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.xianyuplus</groupId>
    <artifactId>xianyu-plus</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>XianyuPlus</name>
    <description>校园二手物品交易平台</description>
    <modules>
        <module>common</module>
        <module>framework</module>
        <module>service</module>
        <module>chat</module>
        <module>admin</module>
    </modules>
    <properties>
        <java.version>1.8</java.version>
        <spring-boot.version>2.7.18</spring-boot.version>
        <mybatis-plus.version>3.5.3.1</mybatis-plus.version>
        <hutool.version>5.8.23</hutool.version>
        <jjwt.version>0.9.1</jjwt.version>
        <knife4j.version>4.3.0</knife4j.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${hutool.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
                <version>${knife4j.version}</version>
            </dependency>
            <dependency>
                <groupId>com.xianyuplus</groupId>
                <artifactId>common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.xianyuplus</groupId>
                <artifactId>framework</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.xianyuplus</groupId>
                <artifactId>chat</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.xianyuplus</groupId>
                <artifactId>admin</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**common/pom.xml**: Plain jar, dependencies: lombok, jackson (for annotations), mybatis-plus (for IdType), hutool.

**framework/pom.xml**: Dependencies: common, spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-data-redis, jjwt, knife4j.

**service/pom.xml**: Dependencies: common, framework, chat, admin, spring-boot-starter-web, mybatis-plus-boot-starter, mysql-connector-java, spring-boot-starter-aop. Also includes spring-boot-maven-plugin for packaging.

**chat/pom.xml**: Dependencies: common, spring-boot-starter-websocket, spring-boot-starter-data-redis.

**admin/pom.xml**: Dependencies: common, framework, spring-boot-starter-web.

### Task 2: Create SQL initialization script

**File:** `xianyu-plus/service/src/main/resources/db/init.sql`

Creates database `xianyu_plus` and all 6 tables with proper indexes. Inserts default categories and admin user (password: admin123, BCrypt encoded).

```sql
CREATE DATABASE IF NOT EXISTS xianyu_plus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xianyu_plus;

-- user table
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50),
    `avatar` VARCHAR(500),
    `phone` VARCHAR(20),
    `role` TINYINT DEFAULT 0 COMMENT '0用户 1管理员',
    `status` TINYINT DEFAULT 0 COMMENT '0正常 1封禁',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- category table
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- product table
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10,2) NOT NULL,
    `original_price` DECIMAL(10,2),
    `category_id` BIGINT,
    `user_id` BIGINT NOT NULL,
    `condition` TINYINT DEFAULT 1 COMMENT '1全新 2几乎全新 3轻微使用 4明显痕迹',
    `status` TINYINT DEFAULT 1 COMMENT '1在售 2已售出 3已下架',
    `view_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- product_image table
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `url` VARCHAR(500) NOT NULL,
    `sort_order` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- favorite table
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- orders table
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `buyer_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0待付款 1已付款 2已完成 3已取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- message table
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `product_id` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `is_read` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- Default data
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('电子产品', 0, 1), ('书籍教材', 0, 2), ('生活用品', 0, 3), ('衣物鞋包', 0, 4), ('运动户外', 0, 5), ('其他', 0, 6),
('手机', 1, 1), ('电脑', 1, 2), ('平板', 1, 3), ('耳机', 1, 4),
('教材', 2, 1), ('考研', 2, 2), ('小说', 2, 3),
('日用', 3, 1), ('美妆', 3, 2), ('电器', 3, 3);

-- admin account: admin / admin123
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '管理员', 1);
```

---

## Phase 2: Common Module

### Task 3: Create entity classes

All entity classes under `common/src/main/java/com/xianyuplus/common/entity/`:

**User.java** — `@TableName("user")`, fields: id(Long, ASSIGN_ID), username, password, nickname, avatar, phone, role(Integer), status(Integer), createdAt, updatedAt. Uses `@TableLogic` for status if needed.

**Category.java** — `@TableName("category")`, fields: id(Long, ASSIGN_ID), name, parentId, sortOrder. Has `@TableField(exist = false) private List<Category> children` for tree.

**Product.java** — `@TableName("product")`, fields: id(Long, ASSIGN_ID), title, description, price(BigDecimal), originalPrice(BigDecimal), categoryId, userId, condition(Integer), status(Integer), viewCount, createdAt, updatedAt. Has `@TableField(exist = false) private List<ProductImage> images`, `@TableField(exist = false) private String sellerName`, `@TableField(exist = false) private String categoryName`.

**ProductImage.java** — `@TableName("product_image")`, id, productId, url, sortOrder.

**Favorite.java** — `@TableName("favorite")`, id, userId, productId, createdAt.

**Order.java** — `@TableName("orders")`, id, buyerId, sellerId, productId, amount, status, createdAt, updatedAt. Has `@TableField(exist = false) private String productTitle`, `@TableField(exist = false) private String buyerName`, `@TableField(exist = false) private String sellerName`.

**Message.java** — `@TableName("message")`, id, senderId, receiverId, productId, content, isRead, createdAt. Has `@TableField(exist = false) private String senderName`, `@TableField(exist = false) private String receiverName`.

### Task 4: Create DTOs, Enums, and utility classes

**Enums** (`common/src/main/java/com/xianyuplus/common/enums/`):

- `ProductStatus.java` — ON_SALE(1, "在售"), SOLD(2, "已售出"), OFF_SHELF(3, "已下架")
- `OrderStatus.java` — PENDING(0, "待付款"), PAID(1, "已付款"), COMPLETED(2, "已完成"), CANCELLED(3, "已取消")
- `ProductCondition.java` — NEW(1, "全新"), LIKE_NEW(2, "几乎全新"), LIGHT_USE(3, "轻微使用"), VISIBLE_WEAR(4, "明显痕迹")
- `UserRole.java` — USER(0, "用户"), ADMIN(1, "管理员")

**DTOs** (`common/src/main/java/com/xianyuplus/common/dto/`):

- `LoginDTO.java` — username, password (with validation annotations)
- `RegisterDTO.java` — username, password, nickname, phone
- `ProductDTO.java` — title, description, price, originalPrice, categoryId, condition, images(List of url strings)
- `ProductQueryDTO.java` — keyword, categoryId, condition, minPrice, maxPrice, sort(String, e.g. "newest"/"price_asc"/"price_desc"), page, size
- `OrderDTO.java` — productId
- `PageDTO.java` — page, size (base class for paginated queries)

**Result** (`common/src/main/java/com/xianyuplus/common/utils/Result.java`):
```java
public class Result<T> {
    private int code;
    private String message;
    private T data;
    public static <T> Result<T> ok(T data) { return new Result<>(200, "success", data); }
    public static <T> Result<T> ok() { return ok(null); }
    public static <T> Result<T> error(int code, String message) { return new Result<>(code, message, null); }
    public static <T> Result<T> error(String message) { return error(500, message); }
}
```

**PageResult** (`common/src/main/java/com/xianyuplus/common/utils/PageResult.java`):
```java
public class PageResult<T> {
    private long total;
    private long page;
    private long size;
    private List<T> records;
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
```

**BusinessException** (`common/src/main/java/com/xianyuplus/common/exception/BusinessException.java`):
```java
public class BusinessException extends RuntimeException {
    private int code;
    public BusinessException(int code, String message) { super(message); this.code = code; }
    public BusinessException(String message) { this(500, message); }
    public int getCode() { return code; }
}
```

---

## Phase 3: Framework Module

### Task 5: Create JWT utilities and Spring Security configuration

**JwtTokenUtil** (`framework/src/main/java/com/xianyuplus/framework/security/JwtTokenUtil.java`):
- Uses `io.jsonwebtoken.Jwts` with HMAC-SHA256
- Fields: secret(Base64), expiration(7 days), header("Authorization"), prefix("Bearer ")
- Methods: `generateToken(Long userId, String username, Integer role)`, `parseToken(String token) -> Claims`, `getUserId(Claims)`, `getUserRole(Claims)`, `isExpired(Claims)`, `validateToken(String token) -> boolean`

**JwtTokenFilter** (`framework/src/main/java/com/xianyuplus/framework/security/JwtTokenFilter.java`):
- Extends `OncePerRequestFilter`
- Skips: /api/auth/login, /api/auth/register, /api/product GET, /api/category
- Extracts token from Authorization header, validates, sets SecurityContext

**UserDetailsServiceImpl** (`framework/src/main/java/com/xianyuplus/framework/security/UserDetailsServiceImpl.java`):
- Implements `UserDetailsService`
- Queries User by username from UserMapper
- Returns `org.springframework.security.core.userdetails.User`

**SecurityConfig** (`framework/src/main/java/com/xianyuplus/framework/config/SecurityConfig.java`):
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/auth/login", "/api/auth/register", "/api/product", "/api/product/*", "/api/category", "/ws/**", "/uploads/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

### Task 6: Create infrastructure configuration

**RedisConfig** (`framework/src/main/java/com/xianyuplus/framework/config/RedisConfig.java`):
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**CorsConfig** (`framework/src/main/java/com/xianyuplus/framework/config/CorsConfig.java`):
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:5173")
            .allowedMethods("*").allowedHeaders("*").allowCredentials(true);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
```

**MyBatisPlusConfig** (`framework/src/main/java/com/xianyuplus/framework/config/MyBatisPlusConfig.java`):
```java
@Configuration
@MapperScan("com.xianyuplus.**.mapper")
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**GlobalExceptionHandler** (`framework/src/main/java/com/xianyuplus/framework/handler/GlobalExceptionHandler.java`):
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) { return Result.error(e.getCode(), e.getMessage()); }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) { return Result.error("服务器内部错误"); }
}
```

---

## Phase 4: Service Module — Core

### Task 7: Create application entry and configuration

**StartApplication** (`service/src/main/java/com/xianyuplus/StartApplication.java`):
```java
@SpringBootApplication(scanBasePackages = "com.xianyuplus")
public class StartApplication {
    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }
}
```

**application.yml** (`service/src/main/resources/application.yml`):
```yaml
server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/xianyu_plus?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
  mapper-locations: classpath*:/mapper/**/*.xml
jwt:
  secret: xianyuplus-jwt-secret-key-2024-campus-second-hand-trading-platform
  expiration: 604800000
file:
  upload-path: ./uploads
```

### Task 8: Create Auth API (register/login/info/password)

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/AuthController.java`
- `service/src/main/java/com/xianyuplus/service/service/AuthService.java`
- `service/src/main/java/com/xianyuplus/service/service/impl/AuthServiceImpl.java`
- `service/src/main/java/com/xianyuplus/service/mapper/UserMapper.java`

**AuthController:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthService authService;
    @PostMapping("/register") public Result<?> register(@Valid @RequestBody RegisterDTO dto) { return authService.register(dto); }
    @PostMapping("/login") public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) { return authService.login(dto); }
    @GetMapping("/info") public Result<User> info() { return authService.info(); }
    @PutMapping("/password") public Result<?> password(@RequestBody Map<String, String> params) { return authService.updatePassword(params); }
}
```

**AuthServiceImpl — register:** Check username uniqueness, BCrypt password, save user, return ok.
**AuthServiceImpl — login:** Query user by username, verify password, generate JWT, return token + user info.
**AuthServiceImpl — info:** Get user from SecurityContextHolder, query full user info, return.
**AuthServiceImpl — updatePassword:** Get current user, verify old password, update to new password.

### Task 9: Create User API (profile/avatar)

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/UserController.java`
- `service/src/main/java/com/xianyuplus/service/service/UserService.java` / `UserServiceImpl.java`

**UserController:**
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private UserService userService;
    @GetMapping("/{id}") public Result<User> detail(@PathVariable Long id) { ... }
    @PutMapping("/profile") public Result<?> updateProfile(@RequestBody User user) { ... }
    @PostMapping("/avatar") public Result<String> avatar(@RequestParam("file") MultipartFile file) { ... }
}
```

**UserServiceImpl:** Standard CRUD, avatar upload saves to local `./uploads/avatar/` and returns URL.

### Task 10: Create Category API

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/CategoryController.java`
- `service/src/main/java/com/xianyuplus/service/service/CategoryService.java` / `CategoryServiceImpl.java`
- `service/src/main/java/com/xianyuplus/service/mapper/CategoryMapper.java`

**CategoryController:** `GET /api/category` — returns category tree (parent categories with children nested).

**CategoryServiceImpl:** Query all categories, build tree structure by grouping children under parents with parentId=0.

### Task 11: Create File Upload API

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/FileController.java`
- `service/src/main/java/com/xianyuplus/service/service/FileService.java` / `FileServiceImpl.java`

**FileController:** `POST /api/file/upload` — accepts MultipartFile, saves to `./uploads/products/` with UUID filename, returns URL like `/uploads/products/uuid.jpg`.

### Task 12: Create Product API (list/detail/publish/edit/status/my)

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/ProductController.java`
- `service/src/main/java/com/xianyuplus/service/service/ProductService.java` / `ProductServiceImpl.java`
- `service/src/main/java/com/xianyuplus/service/mapper/ProductMapper.java`
- `service/src/main/java/com/xianyuplus/service/mapper/ProductImageMapper.java`

**ProductController:**
```java
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @GetMapping
    public Result<PageResult<Product>> list(ProductQueryDTO query) {
        // public endpoint, supports keyword, categoryId, condition, price range, sort, pagination
    }
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        // public endpoint, increments view_count
    }
    @PostMapping
    public Result<?> publish(@Valid @RequestBody ProductDTO dto) {
        // requires auth, saves product + images
    }
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        // only publisher can edit
    }
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        // only publisher can change status
    }
    @GetMapping("/my")
    public Result<PageResult<Product>> myProducts(PageDTO dto) {
        // current user's published products
    }
}
```

**ProductServiceImpl — list:** Builds MyBatis-Plus QueryWrapper from ProductQueryDTO. Keyword → LIKE title. categoryId → eq. Price range → between. Sort → orderBy (created_at desc / price asc / price desc). Uses Page + PageResult. Attaches first image as cover, seller nickname.

**ProductServiceImpl — detail:** Query product by id, increment view_count, query images list, query seller nickname, build full response.

**ProductServiceImpl — publish:** Save product, then batch save product_images with product_id. Uses @Transactional.

**ProductServiceImpl — update:** Verify ownership, update product, delete old images, batch insert new images.

**ProductServiceImpl — updateStatus:** Verify ownership, update status field.

### Task 13: Create Favorite API

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/FavoriteController.java`
- `service/src/main/java/com/xianyuplus/service/service/FavoriteService.java` / `FavoriteServiceImpl.java`
- `service/src/main/java/com/xianyuplus/service/mapper/FavoriteMapper.java`

**FavoriteController:**
```java
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @PostMapping("/{productId}")
    public Result<?> toggle(@PathVariable Long productId) {
        // toggle: if favorited → delete, else → insert. returns { favorited: true/false }
    }
    @GetMapping
    public Result<PageResult<Favorite>> list(PageDTO dto) {
        // paginated list of current user's favorites with product info
    }
}
```

**FavoriteServiceImpl — toggle:** Query by userId + productId. If exists, remove. Otherwise save new Favorite record. Use unique constraint to prevent duplicates.

**FavoriteServiceImpl — list:** Page query favorites joined with product info (title, price, first image).

### Task 14: Create Order API

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/OrderController.java`
- `service/src/main/java/com/xianyuplus/service/service/OrderService.java` / `OrderServiceImpl.java`
- `service/src/main/java/com/xianyuplus/service/mapper/OrderMapper.java`

**OrderController:**
```java
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @PostMapping
    public Result<?> create(@Valid @RequestBody OrderDTO dto) {
        // buyer cannot buy own product, product must be ON_SALE, create order with PENDING status, update product status to SOLD
    }
    @GetMapping
    public Result<PageResult<Order>> list(@RequestParam(defaultValue = "buy") String type, PageDTO dto) {
        // type=buy: buyer's orders, type=sell: seller's orders
    }
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        // verify ownership, update status
    }
}
```

---

## Phase 5: Chat Module

### Task 15: Create WebSocket handler and config

**Files:**
- `chat/src/main/java/com/xianyuplus/chat/config/WebSocketConfig.java`
- `chat/src/main/java/com/xianyuplus/chat/handler/ChatWebSocketHandler.java`
- `chat/src/main/java/com/xianyuplus/chat/handler/HandshakeInterceptor.java`
- `chat/src/main/java/com/xianyuplus/chat/service/ChatService.java`

**WebSocketConfig** (chat module):
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired private ChatWebSocketHandler handler;
    @Autowired private HandshakeInterceptor interceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/chat/{userId}")
            .addInterceptors(interceptor)
            .setAllowedOrigins("*");
    }
}
```

**HandshakeInterceptor:** Extracts JWT token from query parameter `?token=xxx`. Parses and validates token. If valid, sets attributes (userId, username) on session. If invalid, returns false (reject connection).

**ChatWebSocketHandler:**
- Maintains `ConcurrentHashMap<Long, WebSocketSession>` for online users
- `afterConnectionEstablished`: Store session, update Redis online status
- `handleTextMessage`: Parse message JSON {receiverId, productId, content}, save to MySQL via ChatService, check if receiver is online → send directly, else save as unread. Broadcast via Redis Pub/Sub.
- `afterConnectionClosed`: Remove session, clear Redis online status
- Has helper method `sendMessage(Long userId, String json)` to push to specific user

**ChatService:**
- `saveMessage(Long senderId, Long receiverId, Long productId, String content)` — saves to MySQL message table
- `getHistory(Long userId1, Long userId2)` — query messages between two users, ordered by created_at
- `getUnreadCount(Long userId)` — count unread messages for user
- `markRead(Long senderId, Long receiverId)` — mark messages as read
- `getConversations(Long userId)` — distinct users the current user has chatted with, with latest message preview

### Task 16: Create Message REST API

**Files:**
- `service/src/main/java/com/xianyuplus/service/controller/MessageController.java`

```java
@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Autowired private ChatService chatService;
    @GetMapping("/{userId}") public Result<List<Message>> history(@PathVariable Long userId) { ... }
    @GetMapping("/unread") public Result<Map<String, Long>> unread() { ... }
    @GetMapping("/conversations") public Result<List<Map<String, Object>>> conversations() { ... }
}
```

---

## Phase 6: Admin Module

### Task 17: Create Admin API

**Files:**
- `admin/src/main/java/com/xianyuplus/admin/controller/AdminController.java`

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    // Dashboard stats
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        // returns: userCount, productCount, orderCount, todayNewUsers, todayNewProducts
    }
    // User management
    @GetMapping("/users") public Result<PageResult<User>> users(PageDTO dto, @RequestParam(required = false) String keyword) { ... }
    @PutMapping("/users/{id}/status") public Result<?> banUser(@PathVariable Long id, @RequestParam Integer status) { ... }
    // Product management
    @GetMapping("/products") public Result<PageResult<Product>> products(PageDTO dto, @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status) { ... }
    @DeleteMapping("/products/{id}") public Result<?> deleteProduct(@PathVariable Long id) { ... }
    // Order management
    @GetMapping("/orders") public Result<PageResult<Order>> orders(PageDTO dto) { ... }
}
```

---

## Phase 7: Frontend Project Setup

### Task 18: Scaffold Vue 3 project and configure

Run `npm create vite@latest xianyuplus-web -- --template vue` in the parent directory, then install dependencies.

**package.json dependencies:** vue, vue-router, pinia, pinia-plugin-persistedstate, axios, element-plus, @element-plus/icons-vue

**vite.config.js:**
```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/ws': { target: 'ws://localhost:8080', ws: true, changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
```

### Task 19: Create router, stores, and axios instance

**src/router/index.js:**
```js
import { createRouter, createWebHistory } from 'vue-router'
const routes = [
  { path: '/', component: () => import('@/views/Home.vue') },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/product/:id', component: () => import('@/views/ProductDetail.vue') },
  { path: '/publish', component: () => import('@/views/Publish.vue'), meta: { auth: true } },
  { path: '/edit/:id', component: () => import('@/views/Publish.vue'), meta: { auth: true } },
  { path: '/chat', component: () => import('@/views/Chat.vue'), meta: { auth: true } },
  { path: '/chat/:userId', component: () => import('@/views/ChatWindow.vue'), meta: { auth: true } },
  { path: '/favorites', component: () => import('@/views/Favorite.vue'), meta: { auth: true } },
  { path: '/orders', component: () => import('@/views/Order.vue'), meta: { auth: true } },
  { path: '/profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
  // Admin routes
  { path: '/admin', component: () => import('@/views/admin/Dashboard.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/users', component: () => import('@/views/admin/Users.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/products', component: () => import('@/views/admin/Products.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/orders', component: () => import('@/views/admin/Orders.vue'), meta: { auth: true, admin: true } },
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.token) return next('/login')
  if (to.meta.admin && userStore.userInfo?.role !== 1) return next('/')
  if ((to.path === '/login' || to.path === '/register') && userStore.token) return next('/')
  next()
})
export default router
```

**src/api/request.js** — Axios instance with baseURL, request interceptor (add token), response interceptor (extract data, handle 401 redirect).

**src/stores/user.js** — Pinia store with persist. State: token, userInfo. Actions: login, register, fetchInfo, logout, updateProfile.

**src/stores/chat.js** — State: ws, messages, onlineUsers, unreadCount. Actions: connect(url), send(msg), disconnect().

**src/main.js** — createApp, use router, use pinia, use ElementPlus (Chinese locale), mount.

### Task 20: Create layout components

**src/components/Layout.vue:**
```vue
<template>
  <el-container>
    <el-header><!-- Logo, nav links, search bar, user dropdown --></el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>
```

**src/components/AdminLayout.vue:**
```vue
<template>
  <el-container>
    <el-aside><!-- Sidebar with admin menu items --></el-aside>
    <el-container>
      <el-header><!-- Admin header --></el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
```

**App.vue:** Use `<Layout>` for non-admin routes, `<AdminLayout>` for `/admin/*` routes.
```vue
<template>
  <AdminLayout v-if="$route.path.startsWith('/admin')" />
  <Layout v-else />
</template>
```

---

## Phase 8: Frontend Pages — Auth & Home

### Task 21: Login and Register pages

**src/views/Login.vue** — Element Plus form with username/password fields. On submit, calls login API, stores token + userInfo in Pinia, redirects to home. Link to register page.

**src/views/Register.vue** — Form with username, password, confirm password, nickname, phone. Client-side validation, calls register API, shows success message, redirects to login.

### Task 22: Home page

**src/views/Home.vue:**
- Top: search bar with keyword input + search button
- Left sidebar: category tree (fetched from /api/category, displayed as expandable list)
- Main area: product grid using Element Plus Card component
- Each card shows: first image, title, price (highlighted in red), seller name, view count
- Click card → navigate to /product/:id
- Pagination component at bottom
- Sort options: newest (default), price low-high, price high-low
- Filter chips: condition (全新/几乎全新/轻微使用/明显痕迹), price range

**src/components/ProductCard.vue:**
```vue
<template>
  <el-card @click="$router.push(`/product/${product.id}`)" class="product-card">
    <img :src="product.images?.[0]?.url || '/placeholder.png'" />
    <div class="title">{{ product.title }}</div>
    <div class="price">¥{{ product.price }}</div>
    <div class="meta">{{ product.sellerName }} · {{ product.viewCount }}次浏览</div>
  </el-card>
</template>
```

---

## Phase 9: Frontend Pages — Product

### Task 23: Product Detail page

**src/views/ProductDetail.vue:**
- Image carousel (Element Plus Carousel) for product images
- Product info: title, price (red), original price (strikethrough), condition badge, category, seller name, description, view count, post date
- Action buttons: "立即购买" (creates order, navigates to orders), "收藏" (toggle star icon), "联系卖家" (navigates to chat with seller)
- If current user is the seller: show "编辑" and "下架/上架" buttons

### Task 24: Publish/Edit Product page

**src/views/Publish.vue:**
- Reused for both publish (route: /publish) and edit (route: /edit/:id)
- Form fields: title (input), description (textarea/rich text), price (number input), original price (number input), category (cascader with categories), condition (select), images (custom ImageUploader component)
- On edit: fetch existing product data, pre-fill form
- On submit: POST or PUT to /api/product

**src/components/ImageUploader.vue:**
- Uses Element Plus Upload component
- Uploads to /api/file/upload
- Displays uploaded images with drag-sort and delete

---

## Phase 10: Frontend Pages — User Features

### Task 25: Profile page

**src/views/Profile.vue:**
- Avatar upload (click to upload)
- Editable fields: nickname, phone
- Password change section
- Display: username (readonly), role badge

### Task 26: Favorite page

**src/views/Favorite.vue:**
- Grid of favorited products using ProductCard
- Click to remove favorite (unfavorite)
- Pagination

### Task 27: Order page

**src/views/Order.vue:**
- Tabs: "我买到的" (buy) / "我卖出的" (sell)
- Each order card shows: product image, title, price, other party name, status badge, time
- Status badges colored: 待付款(gray), 已付款(blue), 已完成(green), 已取消(red)
- Action buttons based on role and status:
  - Buyer + 待付款: "取消订单"
  - Seller + 已付款: "标记完成"  
  - Buyer + 已付款 → show "等待卖家确认"

### Task 28: Chat pages

**src/views/Chat.vue — Conversation list:**
- List of conversations (fetched from /api/message/conversations)
- Each item shows: other user's avatar, nickname, latest message preview, time, unread badge
- Click to navigate to /chat/:userId
- Header shows "消息" title

**src/views/ChatWindow.vue — Chat window:**
- Header: other user's name, link to related product
- Message list (scrollable, auto-scroll to bottom on new message):
  - Right-aligned bubbles for sent messages (blue)
  - Left-aligned bubbles for received messages (white)
  - Time shown between date changes
- Input area: textarea + send button (supports Enter to send)
- WebSocket connection: on mount, connect to `ws://localhost:8080/ws/chat/${currentUserId}?token=${token}`
- Handle incoming messages: if from current chat partner → display; otherwise → update unread count
- On send: WebSocket send JSON { receiverId, productId, content }

**src/components/ChatBubble.vue:**
```vue
<template>
  <div :class="['chat-bubble', isMe ? 'me' : 'other']">
    <div class="content">{{ content }}</div>
    <div class="time">{{ time }}</div>
  </div>
</template>
```

---

## Phase 11: Frontend Admin Pages

### Task 29: Admin Dashboard

**src/views/admin/Dashboard.vue:**
- 4 stat cards in a row: 总用户数, 总商品数, 总订单数, 今日新增
- Uses Element Plus el-stat / el-card

### Task 30: Admin User Management

**src/views/admin/Users.vue:**
- Table with columns: ID, username, nickname, phone, role, status, created_at
- Search by username
- Actions: toggle user status (ban/unban)
- Pagination

### Task 31: Admin Product Management

**src/views/admin/Products.vue:**
- Table: ID, title, seller, price, status, created_at
- Search by title, filter by status
- Actions: view detail, delete (with confirm dialog)
- Pagination

### Task 32: Admin Order Management

**src/views/admin/Orders.vue:**
- Table: ID, buyer, seller, product title, amount, status, created_at
- Pagination (read-only view)

---

## Execution Order

| # | Task | Phase | Depends On |
|---|------|-------|------------|
| 1 | Maven multi-module scaffold | 1 | — |
| 2 | SQL init script | 1 | — |
| 3 | Entity classes | 2 | 1 |
| 4 | DTOs, Enums, Utilities | 2 | 3 |
| 5 | JWT + Security config | 3 | 4 |
| 6 | Infrastructure config (Redis, CORS, MyBatisPlus, Exception Handler) | 3 | 4 |
| 7 | Application entry + config | 4 | 5, 6 |
| 8 | Auth API | 4 | 7 |
| 9 | User API | 4 | 7 |
| 10 | Category API | 4 | 7 |
| 11 | File Upload API | 4 | 7 |
| 12 | Product API | 4 | 10, 11 |
| 13 | Favorite API | 4 | 12 |
| 14 | Order API | 4 | 12 |
| 15 | Chat WebSocket | 5 | 5 |
| 16 | Message REST API | 5 | 15 |
| 17 | Admin API | 6 | 8, 12, 14 |
| 18 | Vue project scaffold | 7 | — |
| 19 | Router, Stores, Axios, Layouts | 7 | 18 |
| 20 | Login + Register pages | 8 | 19 |
| 21 | Home page + ProductCard | 8 | 19, 10 |
| 22 | Product Detail page | 9 | 21 |
| 23 | Publish/Edit Product page | 9 | 22 |
| 24 | Profile page | 10 | 20 |
| 25 | Favorite page | 10 | 21 |
| 26 | Order page | 10 | 21 |
| 27 | Chat pages | 10 | 21 |
| 28 | Admin Dashboard | 11 | 19 |
| 29 | Admin Users | 11 | 28 |
| 30 | Admin Products | 11 | 28 |
| 31 | Admin Orders | 11 | 28 |

Tasks can be parallelized within each phase (e.g., tasks 3-4 in Phase 2 can run together, tasks 5-6 in Phase 3, tasks 8-14 in Phase 4, tasks 20-27 in frontend phases).
