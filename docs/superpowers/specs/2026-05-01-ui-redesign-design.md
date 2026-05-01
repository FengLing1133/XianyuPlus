# XianyuPlus UI 重设计方案

## 概述

将 XianyuPlus 校园二手交易平台的 UI 从"马卡龙风格"升级为"活力潮流风"，采用粉色+青色配色，用 SVG 图标替代 emoji，提升整体视觉品质和现代感。

## 设计方向

- **风格**: 活力潮流风（Vibrant & Block-based）
- **关键词**: 大胆、活力、渐变、圆润卡片、药丸按钮
- **配色**: 粉色 #EC4899 + 青色 #0891B2
- **图标**: Lucide SVG 图标库替代 emoji
- **布局**: 保留原版布局节奏，两侧留白 230px

## 配色系统

### 主色

| 用途 | 色值 | CSS 变量名 |
|------|------|-----------|
| 主色（活力粉） | #EC4899 | `--color-primary` |
| 浅粉 | #F472B6 | `--color-secondary` |
| 辅色（清新青） | #0891B2 | `--color-accent` |
| 背景色 | #FDF2F8 | `--color-background` |
| 前景色/文字 | #831843 | `--color-foreground` |
| 卡片背景 | #FFFFFF | `--color-card` |
| 卡片文字 | #831843 | `--color-card-foreground` |
| 静音色 | #F1EEF5 | `--color-muted` |
| 静音文字 | #64748B | `--color-muted-foreground` |
| 边框 | #FBCFE8 | `--color-border` |
| 焦点环 | #EC4899 | `--color-ring` |

### 语义色

| 用途 | 色值 | CSS 变量名 |
|------|------|-----------|
| 价格/错误 | #DC2626 | `--color-destructive` |
| 成功 | #10B981 | `--color-success` |
| 警告 | #F59E0B | `--color-warning` |
| 信息 | #3B82F6 | `--color-info` |

### 渐变

- **主按钮渐变**: `linear-gradient(135deg, #EC4899, #F472B6)`
- **Hero 区域渐变**: `linear-gradient(135deg, #FDF2F8 0%, #ECFEFF 50%, #FDF2F8 100%)`
- **Logo 渐变**: `linear-gradient(135deg, #EC4899, #0891B2)`

### 成色标签色

| 成色 | 背景色 | 文字色 |
|------|--------|--------|
| 全新 | rgba(59,130,246,0.9) | #FFFFFF |
| 几乎全新 | rgba(8,145,178,0.9) | #FFFFFF |
| 轻微使用 | rgba(249,115,22,0.9) | #FFFFFF |
| 明显痕迹 | rgba(239,68,68,0.9) | #FFFFFF |

## 排版

- **字体栈**: `PingFang SC, Hiragino Sans GB, Microsoft YaHei, Helvetica Neue, sans-serif`
- **标题字号**: 24px (h1), 20px (h2), 16px (h3)
- **正文字号**: 14px（桌面端）, 13px（卡片内）
- **辅助文字**: 12px, 11px
- **行高**: 1.5（正文）, 1.4（卡片标题）

## 布局系统

### 页面容器

- **最大宽度**: 1200px（`.main-content` 和 `.navbar-inner`）
- **两侧留白**: 230px（内容区域两侧 padding）
- **导航栏高度**: 60px
- **页面内边距**: 20px 24px（`.main-content`）

### 首页布局（从上到下）

1. **Hero 区域**: 圆角 20px，粉青渐变背景，左文右图标横向布局，padding 28px 32px
2. **搜索行**: 搜索框（flex:1）+ 成色筛选 + 排序筛选，gap 12px
3. **分类标签**: 横向排列，gap 8px，包含"全部"和各分类 + "其他"
4. **商品网格**: 4 列网格，gap 14px

### 响应式断点

| 断点 | 列数 | 留白 |
|------|------|------|
| ≥1200px | 4 列 | 230px |
| 1024-1199px | 3 列 | 适当缩小 |
| 768-1023px | 2 列 | 24px |
| <768px | 2 列 | 16px |

## 组件规范

### 导航栏

- 高度 60px，白色背景，底部 2px 粉色边框
- Logo: 34x34 渐变方块 + "闲鱼Plus" 文字
- 导航按钮: 药丸形（border-radius: 50px）
  - 当前页: 粉色渐变填充 + 白色文字
  - 其他页: 白色底 + 粉色描边 + 粉色文字
- 右侧: 消息图标（SVG + 粉色角标）+ 通知铃铛（SVG + 青色角标）+ 用户头像下拉

### 搜索框

- 药丸形，白色背景，柔和阴影
- padding: 10px 18px，字号 13px
- 左侧 SVG 搜索图标（粉色）
- 边框: 1.5px solid #E5E7EB

### 筛选按钮

- 药丸形，白色背景，柔和阴影
- padding: 9px 16px，字号 13px
- 左侧 SVG 图标（粉色/青色）
- 下拉菜单: 圆角 14px，阴影，选中项粉色背景

### 分类标签

- 药丸形，gap 8px
- 选中项: 粉色填充 + 白色文字
- 未选中项: 白色底 + 灰色描边
- 包含: 全部、数码电子、服饰鞋包、图书教材、生活用品、运动户外、其他

### 商品卡片

- 圆角 12px，白色背景，柔和阴影
- **缩略图**: 正方形比例（aspect-ratio: 1/1），渐变背景色
  - 无图片时显示 SVG 占位图标 + 渐变背景
  - 成色标签: 右上角，小药丸，对应颜色
- **信息区**: padding 10px 12px
  - 标题: 13px，最多 2 行，超出省略
  - 价格: 15px，#DC2626，font-weight 700
  - 想要数: 11px，#9CA3AF
  - 卖家: 18px 圆形头像 + 11px 昵称
- hover 效果: translateY(-2px) + 阴影加深

### 按钮

- **主要按钮**: 粉色渐变背景 + 白色文字，药丸形
- **次要按钮**: 白色底 + 粉色描边 + 粉色文字
- **操作按钮**: 青色渐变背景 + 白色文字
- **危险按钮**: 红色背景 + 白色文字
- hover: 颜色加深，200ms 过渡
- active: scale(0.98) 缩放反馈

### 标签/徽章

- 药丸形，小字号（10-11px）
- 状态标签: 对应语义色背景 + 白色文字
- 未读角标: 粉色（消息）/ 青色（通知），白色文字

### 表单

- 输入框: 圆角 14px，边框 #E5E7EB，focus 时粉色边框
- 标签: 14px，#374151，font-weight 500
- 错误提示: 红色，位于输入框下方
- placeholder: #9CA3AF

### 弹窗/对话框

- 圆角 20px，白色背景
- 遮罩: rgba(0,0,0,0.5)
- 进入动画: scale(0.95) + opacity(0) → scale(1) + opacity(1)，200ms
- 关闭: 点击遮罩或关闭按钮

### Toast 通知

- 药丸形，居中显示
- 成功: 绿色背景
- 错误: 红色背景
- 信息: 蓝色背景
- 自动消失: 2.5s

### 分页

- 药丸形按钮，间距 4px
- 当前页: 粉色填充
- 其他页: 白色底 + 描边

## 图标系统

使用 **Lucide Icons**（通过 `lucide-vue-next` 包）替代所有 emoji 图标。

### 图标映射

| 原 emoji | 新 SVG 图标 | 用途 |
|----------|------------|------|
| 🏠 | Home | 首页导航 |
| 📦 | Package | 发布商品 |
| 👤 | User | 个人中心 |
| 💬 | MessageCircle | 消息 |
| 🔔 | Bell | 通知 |
| 🔍 | Search | 搜索 |
| ✨ | Sparkles | 成色筛选 |
| 🕐 | Clock | 排序 |
| ⭐ | Star | 收藏 |
| 📖 | BookOpen | 浏览历史 |
| 📋 | ClipboardList | 订单 |
| ⚙️ | Settings | 管理后台 |
| 🚪 | LogOut | 退出登录 |
| 🎓 | GraduationCap | Logo |
| 🔄 | RefreshCw | Hero 图标 |
| 📱 | Smartphone | 电子产品分类 |
| 💻 | Laptop | 电脑分类 |
| 👗 | Shirt | 服饰分类 |
| 📚 | BookOpen | 教材分类 |
| 🎮 | Gamepad2 | 运动娱乐分类 |
| 🎒 | Backpack | 生活用品分类 |

### 图标规范

- 导航图标: 22px
- 搜索/筛选图标: 14-15px
- 卡片占位图标: 36-40px
- 角标图标: 12px
- stroke-width: 2（导航/操作）, 1.5（装饰/占位）

## 动画

- **页面进入**: fadeIn，opacity 0→1 + translateY(8px)→0，300ms ease
- **卡片 hover**: translateY(-2px) + 阴影变化，200ms ease
- **按钮 active**: scale(0.98)，100ms
- **弹窗进入**: scale(0.95→1) + opacity(0→1)，200ms ease-out
- **Toast**: fadeIn + fadeOut，200ms
- **下拉菜单**: opacity + translateY，150ms

## 无障碍

- 所有交互元素最小触摸目标 44x44px
- 文字对比度 ≥ 4.5:1（WCAG AA）
- 图标按钮需有 aria-label
- 焦点环: 2px solid #EC4899
- 支持 prefers-reduced-motion

## 实现要点

### 文件变更清单

1. **`src/styles/variables.css`** — 替换所有 CSS 变量为新配色系统
2. **`src/styles/global.css`** — 更新全局样式（按钮、卡片、表单、toast 等）
3. **`src/components/Layout.vue`** — 导航栏按钮样式、SVG 图标、消息/通知保留
4. **`src/components/ProductCard.vue`** — 正方形缩略图、新配色
5. **`src/components/NotificationBell.vue`** — SVG 图标替代 emoji
6. **`src/components/AdminLayout.vue`** — 管理后台配色更新
7. **`src/components/ImageUploader.vue`** — 新配色
8. **`src/components/ReviewForm.vue`** — 新配色
9. **`src/components/ReviewList.vue`** — 新配色
10. **`src/components/ReportForm.vue`** — 新配色
11. **`src/views/Home.vue`** — Hero 布局、筛选按钮、分类标签（含"其他"）、4 列网格
12. **`src/views/Login.vue`** — 新配色
13. **`src/views/Register.vue`** — 新配色
14. **`src/views/ProductDetail.vue`** — 新配色
15. **`src/views/Profile.vue`** — 新配色
16. **`src/views/Publish.vue`** — 新配色
17. **`src/views/Chat.vue`** — 新配色
18. **`src/views/ChatWindow.vue`** — 新配色
19. **`src/views/Order.vue`** — 新配色
20. **`src/views/Favorite.vue`** — 新配色
21. **`src/views/SellerProfile.vue`** — 新配色
22. **`src/views/ViewHistory.vue`** — 新配色
23. **`src/views/admin/Dashboard.vue`** — 新配色
24. **`src/views/admin/Users.vue`** — 新配色
25. **`src/views/admin/Products.vue`** — 新配色
26. **`src/views/admin/Orders.vue`** — 新配色
27. **`src/views/admin/Reports.vue`** — 新配色
28. **`src/utils/category.js`** — 更新 emoji 映射（可选，或保留用于无图片占位）
29. **`src/utils/toast.js`** — 更新 toast 样式
30. **`src/utils/dialog.js`** — 更新对话框样式
31. **`package.json`** — 添加 `lucide-vue-next` 依赖

### 新增依赖

```json
{
  "lucide-vue-next": "^0.300.0"
}
```

### 分类数据

确保后端 categories 表包含"其他"分类，前端分类标签列表固定包含"其他"选项。
