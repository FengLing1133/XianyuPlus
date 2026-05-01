# XianyuPlus UI 重设计实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 XianyuPlus 的 UI 从马卡龙风格升级为活力潮流风（粉色+青色配色，Lucide SVG 图标）

**Architecture:** 纯前端样式重构，不涉及后端变更。通过更新 CSS 变量文件实现全局配色切换，逐组件替换 emoji 为 Lucide SVG 图标，调整布局参数。

**Tech Stack:** Vue 3, CSS Custom Properties, Lucide Vue Next

**Spec:** `docs/superpowers/specs/2026-05-01-ui-redesign-design.md`

---

### Task 1: 安装 Lucide Vue Next 依赖

**Files:**
- Modify: `xianyuplus-web/package.json`

- [ ] **Step 1: 安装依赖**

```bash
cd xianyuplus-web && npm install lucide-vue-next
```

- [ ] **Step 2: 验证安装**

```bash
cd xianyuplus-web && npm ls lucide-vue-next
```

Expected: 显示 lucide-vue-next 版本号

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/package.json xianyuplus-web/package-lock.json
git commit -m "deps: 添加 lucide-vue-next 图标库"
```

---

### Task 2: 更新 CSS 变量文件

**Files:**
- Modify: `xianyuplus-web/src/styles/variables.css`

- [ ] **Step 1: 替换 variables.css 全部内容**

```css
:root {
  /* Primary */
  --color-primary: #EC4899;
  --color-secondary: #F472B6;
  --color-accent: #0891B2;
  --color-background: #FDF2F8;
  --color-foreground: #831843;
  --color-card: #FFFFFF;
  --color-card-foreground: #831843;
  --color-muted: #F1EEF5;
  --color-muted-foreground: #64748B;
  --color-border: #FBCFE8;
  --color-ring: #EC4899;

  /* Semantic */
  --color-destructive: #DC2626;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-info: #3B82F6;
  --color-on-primary: #FFFFFF;
  --color-on-destructive: #FFFFFF;

  /* Legacy aliases (for gradual migration) */
  --green-500: var(--color-primary);
  --green-400: var(--color-secondary);
  --green-300: var(--color-secondary);
  --green-100: var(--color-border);
  --green-50: var(--color-background);
  --bg-page: #F9FAFB;
  --bg-card: var(--color-card);
  --text-primary: #1F2937;
  --text-secondary: var(--color-muted-foreground);
  --text-muted: #9CA3AF;
  --price-red: var(--color-destructive);
  --border-light: #E5E7EB;
  --border-lighter: var(--color-border);

  /* Radius */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 20px;
  --radius-pill: 50px;

  /* Shadow */
  --shadow-card: 0 2px 10px rgba(0, 0, 0, 0.05);
  --shadow-hover: 0 8px 24px rgba(0, 0, 0, 0.10);
  --shadow-nav: 0 1px 3px rgba(0, 0, 0, 0.06);

  /* Gradients */
  --gradient-primary: linear-gradient(135deg, #EC4899, #F472B6);
  --gradient-accent: linear-gradient(135deg, #0891B2, #06B6D4);
  --gradient-hero: linear-gradient(135deg, #FDF2F8 0%, #ECFEFF 50%, #FDF2F8 100%);
  --gradient-logo: linear-gradient(135deg, #EC4899, #0891B2);

  /* Product placeholder gradients */
  --placeholder-pink: linear-gradient(135deg, #FDF2F8, #FCE7F3);
  --placeholder-cyan: linear-gradient(135deg, #ECFEFF, #CFFAFE);
  --placeholder-orange: linear-gradient(135deg, #FFF7ED, #FFEDD5);
  --placeholder-purple: linear-gradient(135deg, #F5F3FF, #EDE9FE);
  --placeholder-green: linear-gradient(135deg, #ECFDF5, #D1FAE5);
  --placeholder-red: linear-gradient(135deg, #FEF2F2, #FECACA);
  --placeholder-blue: linear-gradient(135deg, #F0F9FF, #DBEAFE);
  --placeholder-yellow: linear-gradient(135deg, #FFFBEB, #FEF3C7);

  /* Condition tag colors */
  --condition-new: rgba(59, 130, 246, 0.9);
  --condition-like-new: rgba(8, 145, 178, 0.9);
  --condition-lightly-used: rgba(249, 115, 22, 0.9);
  --condition-visible-wear: rgba(239, 68, 68, 0.9);

  /* Transition */
  --transition-fast: 0.2s ease;
}
```

- [ ] **Step 2: 验证无语法错误**

启动开发服务器确认无报错：

```bash
cd xianyuplus-web && npm run dev
```

Expected: 编译成功，页面可访问（样式会因旧引用而部分异常，这是预期的）

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/src/styles/variables.css
git commit -m "style: 更新 CSS 变量为粉色+青色配色系统"
```

---

### Task 3: 更新全局样式

**Files:**
- Modify: `xianyuplus-web/src/styles/global.css`

- [ ] **Step 1: 替换 global.css 全部内容**

```css
@import './variables.css';

*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  background: var(--bg-page);
  color: var(--text-primary);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}

a {
  color: inherit;
  text-decoration: none;
}

button {
  cursor: pointer;
  border: none;
  font-family: inherit;
}

input, textarea, select {
  font-family: inherit;
  font-size: inherit;
}

img {
  max-width: 100%;
  display: block;
}

/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Pill button base */
.btn-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 22px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.btn-pill-primary {
  background: var(--gradient-primary);
  color: var(--color-on-primary);
}

.btn-pill-primary:hover {
  opacity: 0.9;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
}

.btn-pill-primary:active {
  transform: scale(0.98);
}

.btn-pill-outline {
  background: transparent;
  border: 1.5px solid var(--color-primary);
  color: var(--color-primary);
}

.btn-pill-outline:hover {
  background: var(--color-background);
}

/* Card base */
.card {
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

/* Pill tag */
.pill-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 500;
}

.pill-tag-green {
  background: rgba(16, 185, 129, 0.1);
  color: var(--color-success);
}

/* Loading skeleton */
.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Form base */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.form-input {
  width: 100%;
  padding: 10px 16px;
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-primary);
  background: #fff;
  transition: border-color var(--transition-fast);
  outline: none;
}

.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

textarea.form-input {
  resize: vertical;
  min-height: 100px;
}

/* Page title */
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
}

/* Toast */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 24px;
  border-radius: var(--radius-pill);
  font-size: 14px;
  z-index: 9999;
  box-shadow: var(--shadow-hover);
  animation: toastIn 0.3s ease;
}

.toast-success {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.toast-error {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.toast-info {
  background: #eff6ff;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

@keyframes toastIn {
  from { opacity: 0; transform: translateX(-50%) translateY(-12px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 32px;
}

.pagination button {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border-radius: var(--radius-pill);
  background: var(--color-card);
  border: 1px solid var(--border-light);
  font-size: 14px;
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.pagination button:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.pagination button.active {
  background: var(--gradient-primary);
  color: var(--color-on-primary);
  border-color: transparent;
}

.pagination button:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.empty-state .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.6;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}
```

- [ ] **Step 2: 验证编译通过**

```bash
cd xianyuplus-web && npm run dev
```

Expected: 编译成功

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/src/styles/global.css
git commit -m "style: 更新全局样式为活力潮流风"
```

---

### Task 4: 更新 Layout.vue 导航栏

**Files:**
- Modify: `xianyuplus-web/src/components/Layout.vue`

- [ ] **Step 1: 替换 Layout.vue 全部内容**

```vue
<template>
  <div class="layout">
    <nav class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">
          <div class="logo-icon-wrapper">
            <GraduationCap :size="18" color="white" />
          </div>
          <span class="logo-text">闲鱼Plus</span>
        </router-link>

        <div class="nav-links">
          <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
            <Home :size="16" />
            <span>首页</span>
          </router-link>
          <router-link v-if="userStore.isLoggedIn" to="/publish" class="nav-link" :class="{ active: $route.path === '/publish' }">
            <Package :size="16" />
            <span>发布商品</span>
          </router-link>
          <router-link v-if="userStore.isLoggedIn" to="/profile" class="nav-link" :class="{ active: $route.path === '/profile' }">
            <User :size="16" />
            <span>个人中心</span>
          </router-link>
        </div>

        <div class="nav-actions">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/chat" class="chat-badge" title="消息">
              <MessageCircle :size="22" />
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </router-link>

            <NotificationBell v-if="userStore.token" />

            <div class="user-menu" @click="showDropdown = !showDropdown" v-click-outside="() => showDropdown = false">
              <img :src="userStore.userInfo?.avatar || ''" class="user-avatar" @error="e => e.target.style.display='none'" />
              <span v-if="!userStore.userInfo?.avatar" class="avatar-placeholder">
                <User :size="16" />
              </span>
              <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
              <ChevronDown :size="14" class="arrow-icon" />

              <div v-if="showDropdown" class="dropdown">
                <router-link to="/profile" class="dropdown-item" @click="showDropdown = false">
                  <User :size="16" /> 个人中心
                </router-link>
                <router-link to="/favorites" class="dropdown-item" @click="showDropdown = false">
                  <Star :size="16" /> 我的收藏
                </router-link>
                <router-link to="/history" class="dropdown-item" @click="showDropdown = false">
                  <Clock :size="16" /> 浏览历史
                </router-link>
                <router-link to="/orders" class="dropdown-item" @click="showDropdown = false">
                  <ClipboardList :size="16" /> 我的订单
                </router-link>
                <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="showDropdown = false">
                  <Settings :size="16" /> 管理后台
                </router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item" @click="handleLogout">
                  <LogOut :size="16" /> 退出登录
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <button class="btn-pill btn-pill-outline" @click="$router.push('/login')">登录</button>
            <button class="btn-pill btn-pill-primary" @click="$router.push('/register')" style="margin-left: 10px;">注册</button>
          </template>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import NotificationBell from './NotificationBell.vue'
import {
  GraduationCap, Home, Package, User, MessageCircle,
  ChevronDown, Star, Clock, ClipboardList, Settings, LogOut
} from 'lucide-vue-next'

const router = useRouter()
const userStore = useUserStore()
const showDropdown = ref(false)
const unreadCount = ref(0)
let timer = null

async function fetchUnread() {
  if (!userStore.token) return
  try {
    const res = await request.get('/message/unread/count', { silent: true })
    unreadCount.value = res.data || 0
  } catch {}
}

function handleLogout() {
  userStore.logout()
  showDropdown.value = false
  router.push('/login')
}

const vClickOutside = {
  mounted(el, binding) {
    el.__clickOutside = (e) => {
      if (!el.contains(e.target)) binding.value()
    }
    document.addEventListener('click', el.__clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el.__clickOutside)
  }
}

onMounted(() => {
  fetchUnread()
  timer = setInterval(fetchUnread, 10000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: var(--shadow-nav);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  border-bottom: 2px solid var(--color-border);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-foreground);
}

.logo-icon-wrapper {
  width: 34px;
  height: 34px;
  background: var(--gradient-logo);
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 18px;
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border: 1.5px solid var(--border-light);
  background: #fff;
  transition: all var(--transition-fast);
}

.nav-link:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.nav-link.active,
.nav-link.router-link-exact-active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-badge {
  position: relative;
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.chat-badge:hover {
  color: var(--color-primary);
}

.badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  font-weight: 600;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  position: relative;
  padding: 5px 10px;
  border-radius: var(--radius-pill);
  background: #F9FAFB;
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
}

.user-menu:hover {
  border-color: var(--color-border);
}

.user-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-name {
  font-size: 13px;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.arrow-icon {
  color: var(--text-muted);
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-hover);
  min-width: 180px;
  padding: 8px;
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-primary);
  background: none;
  transition: background var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--bg-page);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-lighter);
  margin: 4px 0;
}

.main-content {
  min-height: calc(100vh - 60px);
  padding: 20px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
```

- [ ] **Step 2: 验证页面渲染**

```bash
cd xianyuplus-web && npm run dev
```

Expected: 导航栏显示新样式，SVG 图标正常渲染

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/src/components/Layout.vue
git commit -m "style: 重设计导航栏 - SVG图标、按钮样式、粉色+青色配色"
```

---

### Task 5: 更新 Home.vue 首页

**Files:**
- Modify: `xianyuplus-web/src/views/Home.vue`

- [ ] **Step 1: 替换 Home.vue 的 template 部分**

将 template 中的 emoji 替换为 Lucide SVG 图标，Hero 改为横向布局，搜索框和筛选按钮样式更新，分类标签加"其他"。

具体变更：
- Hero: `📦` → `<RefreshCw>` 图标，横向布局（左文右图标）
- 搜索: `🔍` → `<Search>` SVG
- 成色筛选: `✨` → `<Sparkles>` SVG
- 排序: `🕐` → `<Clock>` SVG
- 分类: emoji → Lucide 图标，新增"其他"分类
- 分类 emoji 映射更新

- [ ] **Step 2: 更新 script 部分的分类 emoji 映射**

在 script setup 中导入 Lucide 图标，更新 `categoryEmojis` 映射为 SVG 组件名映射。

- [ ] **Step 3: 更新 style 部分**

更新所有颜色引用为新变量，Hero 背景改为粉青渐变，搜索框/筛选按钮样式更新。

- [ ] **Step 4: 验证页面渲染**

```bash
cd xianyuplus-web && npm run dev
```

Expected: 首页显示新样式，Hero 横向布局，4 列正方形卡片，"其他"分类可见

- [ ] **Step 5: 提交**

```bash
git add xianyuplus-web/src/views/Home.vue
git commit -m "style: 重设计首页 - Hero布局、筛选按钮、分类标签、4列网格"
```

---

### Task 6: 更新 ProductCard.vue

**Files:**
- Modify: `xianyuplus-web/src/components/ProductCard.vue`

- [ ] **Step 1: 更新 template**

- 图片区域改为 `aspect-ratio: 1/1` 正方形
- 成色标签移到图片右上角
- emoji 占位改为 Lucide SVG 图标
- 更新颜色引用

- [ ] **Step 2: 更新 script**

导入 Lucide 图标（Smartphone, Book, ShoppingBag 等），更新 `getProductEmoji` 为返回 SVG 组件。

- [ ] **Step 3: 更新 style**

更新配色、圆角、阴影为新设计系统。

- [ ] **Step 4: 验证并提交**

```bash
git add xianyuplus-web/src/components/ProductCard.vue
git commit -m "style: 重设计商品卡片 - 正方形缩略图、新配色、SVG图标"
```

---

### Task 7: 更新 NotificationBell.vue

**Files:**
- Modify: `xianyuplus-web/src/components/NotificationBell.vue`

- [ ] **Step 1: 替换 emoji 为 Lucide 图标**

- 🔔 → `<Bell>` SVG
- 📦 → `<Package>` (订单类型)
- 💬 → `<MessageCircle>` (消息类型)
- ❤️ → `<Heart>` (收藏类型)
- ℹ️ → `<Info>` (系统类型)

- [ ] **Step 2: 更新样式**

角标改为青色 `var(--color-accent)`，下拉面板样式更新。

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/src/components/NotificationBell.vue
git commit -m "style: 更新通知铃铛 - SVG图标、青色角标"
```

---

### Task 8: 更新其他组件

**Files:**
- Modify: `xianyuplus-web/src/components/ImageUploader.vue`
- Modify: `xianyuplus-web/src/components/ReviewForm.vue`
- Modify: `xianyuplus-web/src/components/ReviewList.vue`
- Modify: `xianyuplus-web/src/components/ReportForm.vue`

- [ ] **Step 1: 更新 ImageUploader.vue**

替换 emoji 为 Lucide 图标（Upload, X），更新配色。

- [ ] **Step 2: 更新 ReviewForm.vue**

替换 ⭐ 为 `<Star>` SVG，更新弹窗样式为新设计（圆角 20px、渐变按钮）。

- [ ] **Step 3: 更新 ReviewList.vue**

替换 emoji 为 Lucide 图标，更新配色和弹窗样式。

- [ ] **Step 4: 更新 ReportForm.vue**

替换 emoji 为 Lucide 图标，更新配色（提交按钮用红色 `var(--color-destructive)`）。

- [ ] **Step 5: 提交**

```bash
git add xianyuplus-web/src/components/ImageUploader.vue xianyuplus-web/src/components/ReviewForm.vue xianyuplus-web/src/components/ReviewList.vue xianyuplus-web/src/components/ReportForm.vue
git commit -m "style: 更新组件样式 - SVG图标、新配色"
```

---

### Task 9: 更新认证页面

**Files:**
- Modify: `xianyuplus-web/src/views/Login.vue`
- Modify: `xianyuplus-web/src/views/Register.vue`

- [ ] **Step 1: 更新 Login.vue**

- emoji 头部图标 → Lucide SVG
- 按钮改为渐变样式
- 表单输入框圆角改为 `var(--radius-md)`
- 链接颜色改为 `var(--color-primary)`

- [ ] **Step 2: 更新 Register.vue**

同 Login.vue 的变更。

- [ ] **Step 3: 提交**

```bash
git add xianyuplus-web/src/views/Login.vue xianyuplus-web/src/views/Register.vue
git commit -m "style: 更新登录/注册页面 - SVG图标、渐变按钮"
```

---

### Task 10: 更新用户页面

**Files:**
- Modify: `xianyuplus-web/src/views/Profile.vue`
- Modify: `xianyuplus-web/src/views/Publish.vue`
- Modify: `xianyuplus-web/src/views/ProductDetail.vue`
- Modify: `xianyuplus-web/src/views/SellerProfile.vue`

- [ ] **Step 1: 更新 Profile.vue**

替换 emoji 为 Lucide 图标，更新配色。

- [ ] **Step 2: 更新 Publish.vue**

替换 emoji 为 Lucide 图标，更新表单样式。

- [ ] **Step 3: 更新 ProductDetail.vue**

替换 emoji 为 Lucide 图标，更新按钮样式（渐变主按钮、青色操作按钮）。

- [ ] **Step 4: 更新 SellerProfile.vue**

替换 emoji 为 Lucide 图标，更新配色。

- [ ] **Step 5: 提交**

```bash
git add xianyuplus-web/src/views/Profile.vue xianyuplus-web/src/views/Publish.vue xianyuplus-web/src/views/ProductDetail.vue xianyuplus-web/src/views/SellerProfile.vue
git commit -m "style: 更新用户页面 - SVG图标、新配色"
```

---

### Task 11: 更新功能页面

**Files:**
- Modify: `xianyuplus-web/src/views/Chat.vue`
- Modify: `xianyuplus-web/src/views/ChatWindow.vue`
- Modify: `xianyuplus-web/src/views/Order.vue`
- Modify: `xianyuplus-web/src/views/Favorite.vue`
- Modify: `xianyuplus-web/src/views/ViewHistory.vue`

- [ ] **Step 1: 更新 Chat.vue**

替换 emoji 为 Lucide 图标，更新配色。

- [ ] **Step 2: 更新 ChatWindow.vue**

替换 emoji 为 Lucide 图标，发送按钮改为渐变样式。

- [ ] **Step 3: 更新 Order.vue**

替换 emoji 为 Lucide 图标，状态标签颜色更新。

- [ ] **Step 4: 更新 Favorite.vue**

替换 emoji 为 Lucide 图标，更新配色。

- [ ] **Step 5: 更新 ViewHistory.vue**

替换 emoji 为 Lucide 图标，更新配色。

- [ ] **Step 6: 提交**

```bash
git add xianyuplus-web/src/views/Chat.vue xianyuplus-web/src/views/ChatWindow.vue xianyuplus-web/src/views/Order.vue xianyuplus-web/src/views/Favorite.vue xianyuplus-web/src/views/ViewHistory.vue
git commit -m "style: 更新功能页面 - SVG图标、新配色"
```

---

### Task 12: 更新管理后台

**Files:**
- Modify: `xianyuplus-web/src/components/AdminLayout.vue`
- Modify: `xianyuplus-web/src/views/admin/Dashboard.vue`
- Modify: `xianyuplus-web/src/views/admin/Users.vue`
- Modify: `xianyuplus-web/src/views/admin/Products.vue`
- Modify: `xianyuplus-web/src/views/admin/Orders.vue`
- Modify: `xianyuplus-web/src/views/admin/Reports.vue`

- [ ] **Step 1: 更新 AdminLayout.vue**

- 侧边栏背景改为深色或保持白色但用新配色
- 导航项图标替换为 Lucide SVG
- 活跃项用粉色标识

- [ ] **Step 2: 更新 Dashboard.vue**

替换 emoji 为 Lucide 图标，统计卡片配色更新。

- [ ] **Step 3: 更新 Users.vue / Products.vue / Orders.vue / Reports.vue**

替换 emoji 为 Lucide 图标，表格样式更新，按钮/标签配色更新。

- [ ] **Step 4: 提交**

```bash
git add xianyuplus-web/src/components/AdminLayout.vue xianyuplus-web/src/views/admin/Dashboard.vue xianyuplus-web/src/views/admin/Users.vue xianyuplus-web/src/views/admin/Products.vue xianyuplus-web/src/views/admin/Orders.vue xianyuplus-web/src/views/admin/Reports.vue
git commit -m "style: 更新管理后台 - SVG图标、新配色"
```

---

### Task 13: 更新工具函数

**Files:**
- Modify: `xianyuplus-web/src/utils/category.js`
- Modify: `xianyuplus-web/src/utils/toast.js`
- Modify: `xianyuplus-web/src/utils/dialog.js`

- [ ] **Step 1: 更新 category.js**

更新 `getProductColor` 返回新渐变色，更新 `getProductEmoji` 返回 Lucide 图标组件名（或保留 emoji 用于无 JS 图标场景）。

- [ ] **Step 2: 更新 toast.js**

更新 toast 样式类名以匹配新的 global.css。

- [ ] **Step 3: 更新 dialog.js**

更新对话框样式：圆角 20px、渐变按钮、新配色。

- [ ] **Step 4: 提交**

```bash
git add xianyuplus-web/src/utils/category.js xianyuplus-web/src/utils/toast.js xianyuplus-web/src/utils/dialog.js
git commit -m "style: 更新工具函数 - 新配色、新样式"
```

---

### Task 14: 最终验证与清理

- [ ] **Step 1: 启动开发服务器完整测试**

```bash
cd xianyuplus-web && npm run dev
```

逐页检查：
- 首页: Hero 横向布局、搜索框、筛选按钮、分类标签（含"其他"）、4 列正方形卡片
- 登录/注册: 渐变按钮、SVG 图标
- 商品详情: 正方形图片、操作按钮样式
- 个人中心: 表单样式
- 发布商品: 表单样式
- 消息/聊天: SVG 图标
- 订单: 状态标签颜色
- 收藏/历史: SVG 图标
- 管理后台: 侧边栏、统计卡片、表格

- [ ] **Step 2: 检查响应式**

在浏览器中测试 1200px、1024px、768px 宽度下的布局。

- [ ] **Step 3: 构建验证**

```bash
cd xianyuplus-web && npm run build
```

Expected: 构建成功，无错误

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "style: UI重设计完成 - 活力潮流风、粉色+青色配色、Lucide SVG图标"
```
