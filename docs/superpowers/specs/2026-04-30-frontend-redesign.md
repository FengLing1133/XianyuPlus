# 闲鱼Plus 前端 UI 全面改造

## 目标

移除 Element Plus 组件库，用自定义 CSS 重写全部前端组件，统一为现代极简清新校园风格。

## 技术变更

- **移除**：element-plus、@element-plus/icons-vue
- **保留**：Vue 3、Pinia、Vue Router、Axios、Vite
- **新增**：`src/styles/variables.css`（全局 CSS 变量）、`src/styles/global.css`（通用样式）

## 设计规范

采用用户提供的完整 UI 规范，核心要点：

- **主色调**：森林绿 #2b9939
- **背景色**：页面 #f5f7fa，卡片 #ffffff
- **文字色**：主标题 #333333，副标题 #999999
- **价格色**：红色 #e53935
- **圆角**：大范围使用 8px-50px 圆角
- **阴影**：柔和弥散阴影，hover 加深
- **图标**：全部使用 Emoji
- **占位图**：马卡龙色纯色背景 + 居中大号 Emoji

## 分类映射

| 分类 | Emoji | 马卡龙色 |
|------|-------|---------|
| 教材教辅 | 📚 | 淡粉 #fce4ec |
| 电子产品 | 📱 | 淡蓝 #e3f2fd |
| 生活用品 | 🎒 | 淡绿 #e8f5e9 |
| 服饰鞋包 | 👗 | 淡紫 #f3e5f5 |
| 运动娱乐 | 🎮 | 淡黄 #fff9c4 |
| 其他好物 | 🏠 | 淡橙 #fff3e0 |

## 修改文件清单（约 20 个）

### 基础设施
- `main.js` — 移除 Element Plus
- `App.vue` — 全局背景
- 新建 `src/styles/variables.css`、`src/styles/global.css`

### 核心布局
- `components/Layout.vue` — 新导航栏（Emoji Logo、胶囊按钮、导航链接）
- `components/AdminLayout.vue` — 浅色侧边栏后台布局
- `components/ProductCard.vue` — 新卡片（马卡龙占位、成色标签、价格行）

### 前台页面
- `views/Home.vue` — Hero Banner、胶囊搜索、Emoji 分类 Pill、4 列网格
- `views/Login.vue`、`views/Register.vue` — 全圆角表单卡片
- `views/ProductDetail.vue` — 重新布局商品详情
- `views/Profile.vue` — 个人中心卡片式布局
- `views/Publish.vue` — 发布商品表单
- `views/Chat.vue`、`views/ChatWindow.vue` — 聊天页面
- `views/Favorite.vue`、`views/Order.vue` — 收藏和订单

### 管理后台
- `views/admin/Dashboard.vue`、`Users.vue`、`Products.vue`、`Orders.vue`
- `components/ImageUploader.vue` — 文件上传组件

## 实施顺序

1. 基础设施（CSS 变量、移除 Element Plus）
2. 布局层（Layout、AdminLayout、App.vue）
3. 首页核心（Home.vue、ProductCard.vue）
4. 其他前台页面（Login、Register、Detail、Profile 等）
5. 管理后台页面
