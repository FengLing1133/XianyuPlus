# 搜索增强实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 扩展商品搜索功能，支持价格区间、成色筛选、多分类筛选、排序。

**Architecture:** 扩展现有 ProductQueryDTO 和 ProductServiceImpl，前端 Home.vue 添加筛选栏 UI。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3

---

## 文件结构

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `common/.../dto/ProductQueryDTO.java` | 新增筛选参数字段 |
| `service/.../service/impl/ProductServiceImpl.java` | 动态查询条件拼接 |
| `xianyuplus-web/src/views/Home.vue` | 筛选栏 UI + 多分类选择 |

---

## Task 1: 后端 — 扩展 ProductQueryDTO

**Files:**
- Modify: `xianyu-plus/common/src/main/java/com/xianyuplus/common/dto/ProductQueryDTO.java`

- [ ] **Step 1: 添加新字段**

```java
package com.xianyuplus.common.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private String keyword;
    private Long categoryId;
    private Integer page = 1;
    private Integer size = 12;

    // 新增字段
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;
    private Integer condition;
    private String categoryIds; // 逗号分隔的多分类ID
    private String sort; // price_asc, price_desc, newest, views
}
```

- [ ] **Step 2: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl common`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/dto/ProductQueryDTO.java
git commit -m "feat: ProductQueryDTO 新增筛选参数字段"
```

---

## Task 2: 后端 — 修改 ProductServiceImpl 查询逻辑

**Files:**
- Modify: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ProductServiceImpl.java`

- [ ] **Step 1: 修改 list 方法的查询条件构建**

找到 `list` 方法，修改查询条件构建逻辑：

```java
@Override
public Result<?> list(ProductQueryDTO query) {
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getCode());

    // 关键词搜索
    if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
        wrapper.like(Product::getTitle, query.getKeyword());
    }

    // 分类筛选（支持多分类）
    if (query.getCategoryIds() != null && !query.getCategoryIds().isEmpty()) {
        String[] ids = query.getCategoryIds().split(",");
        List<Long> categoryIdList = new ArrayList<>();
        for (String id : ids) {
            categoryIdList.add(Long.parseLong(id.trim()));
        }
        wrapper.in(Product::getCategoryId, categoryIdList);
    } else if (query.getCategoryId() != null) {
        // 单分类（兼容旧逻辑）
        wrapper.eq(Product::getCategoryId, query.getCategoryId());
    }

    // 价格区间筛选
    if (query.getMinPrice() != null) {
        wrapper.ge(Product::getPrice, query.getMinPrice());
    }
    if (query.getMaxPrice() != null) {
        wrapper.le(Product::getPrice, query.getMaxPrice());
    }

    // 成色筛选
    if (query.getCondition() != null) {
        wrapper.eq(Product::getCondition, query.getCondition());
    }

    // 排序
    if (query.getSort() != null) {
        switch (query.getSort()) {
            case "price_asc":
                wrapper.orderByAsc(Product::getPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Product::getPrice);
                break;
            case "newest":
                wrapper.orderByDesc(Product::getCreatedAt);
                break;
            case "views":
                wrapper.orderByDesc(Product::getViewCount);
                break;
            default:
                wrapper.orderByDesc(Product::getCreatedAt);
        }
    } else {
        wrapper.orderByDesc(Product::getCreatedAt);
    }

    Page<Product> page = new Page<>(query.getPage(), query.getSize());
    Page<Product> result = productMapper.selectPage(page, wrapper);

    // 转换为 DTO 返回
    List<ProductDTO> records = result.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

    PageResult<ProductDTO> pageResult = new PageResult<>();
    pageResult.setTotal(result.getTotal());
    pageResult.setPage(query.getPage());
    pageResult.setSize(query.getSize());
    pageResult.setRecords(records);

    return Result.ok(pageResult);
}
```

- [ ] **Step 2: 确保导入必要的类**

在文件顶部添加：

```java
import java.util.ArrayList;
import java.util.Arrays;
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ProductServiceImpl.java
git commit -m "feat: ProductServiceImpl 支持多条件筛选和排序"
```

---

## Task 3: 前端 — Home.vue 筛选栏 UI

**Files:**
- Modify: `xianyuplus-web/src/views/Home.vue`

- [ ] **Step 1: 添加筛选栏 HTML**

在搜索栏下方添加筛选栏：

```html
<div class="filter-bar">
  <!-- 价格区间 -->
  <div class="filter-group">
    <label>价格</label>
    <div class="price-range">
      <input v-model="filters.minPrice" type="number" placeholder="最低价" min="0" step="0.01">
      <span class="separator">-</span>
      <input v-model="filters.maxPrice" type="number" placeholder="最高价" min="0" step="0.01">
    </div>
  </div>

  <!-- 成色筛选 -->
  <div class="filter-group">
    <label>成色</label>
    <select v-model="filters.condition">
      <option value="">全部</option>
      <option value="1">全新</option>
      <option value="2">几乎全新</option>
      <option value="3">轻微使用</option>
      <option value="4">明显痕迹</option>
    </select>
  </div>

  <!-- 排序 -->
  <div class="filter-group">
    <label>排序</label>
    <select v-model="filters.sort">
      <option value="">默认排序</option>
      <option value="price_asc">价格低到高</option>
      <option value="price_desc">价格高到低</option>
      <option value="newest">最新发布</option>
      <option value="views">最多浏览</option>
    </select>
  </div>

  <button class="btn-search" @click="fetchData(true)">筛选</button>
  <button class="btn-reset" @click="resetFilters">重置</button>
</div>
```

- [ ] **Step 2: 添加筛选状态变量**

在 `<script setup>` 中添加：

```javascript
const filters = reactive({
  minPrice: '',
  maxPrice: '',
  condition: '',
  sort: ''
})
```

- [ ] **Step 3: 修改 fetchData 方法**

修改 `fetchData` 方法，将筛选参数传递给 API：

```javascript
async function fetchData(showSkeleton = false) {
  if (showSkeleton) loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: 12
    }
    if (keyword.value) params.keyword = keyword.value
    if (selectedCategory.value) {
      // 检查是否有子分类
      const cat = categories.value.find(c => c.id === selectedCategory.value)
      if (cat && cat.children && cat.children.length > 0) {
        params.categoryIds = [selectedCategory.value, ...cat.children.map(c => c.id)].join(',')
      } else {
        params.categoryId = selectedCategory.value
      }
    }
    if (filters.minPrice) params.minPrice = filters.minPrice
    if (filters.maxPrice) params.maxPrice = filters.maxPrice
    if (filters.condition) params.condition = filters.condition
    if (filters.sort) params.sort = filters.sort

    const res = await getProducts(params)
    if (res.code === 200) {
      products.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 4: 添加 resetFilters 方法**

```javascript
function resetFilters() {
  filters.minPrice = ''
  filters.maxPrice = ''
  filters.condition = ''
  filters.sort = ''
  fetchData(true)
}
```

- [ ] **Step 5: 添加筛选栏 CSS**

```css
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
}

.filter-group select:focus,
.filter-group input:focus {
  border-color: var(--primary);
  outline: none;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-range input {
  width: 100px;
}

.price-range .separator {
  color: #999;
}

.btn-search {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-search:hover {
  opacity: 0.9;
}

.btn-reset {
  padding: 8px 20px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-reset:hover {
  background: #eee;
}
```

- [ ] **Step 6: 测试验证**

Run: `cd xianyuplus-web && npm run dev`

在浏览器中测试：
1. 价格区间筛选是否生效
2. 成色筛选是否生效
3. 排序功能是否正常
4. 重置按钮是否清空所有筛选条件

- [ ] **Step 7: Commit**

```bash
git add xianyuplus-web/src/views/Home.vue
git commit -m "feat: Home.vue 添加搜索筛选栏 UI"
```

---

## Task 4: 前端 — 多分类选择支持

**Files:**
- Modify: `xianyuplus-web/src/views/Home.vue`

- [ ] **Step 1: 修改分类选择逻辑**

将单选分类改为支持多选（Ctrl+点击）：

```javascript
const selectedCategories = ref([])

function handleCategoryClick(catId, event) {
  if (event.ctrlKey || event.metaKey) {
    // Ctrl/Cmd + 点击：多选
    const index = selectedCategories.value.indexOf(catId)
    if (index > -1) {
      selectedCategories.value.splice(index, 1)
    } else {
      selectedCategories.value.push(catId)
    }
  } else {
    // 普通点击：单选/取消
    if (selectedCategories.value.length === 1 && selectedCategories.value[0] === catId) {
      selectedCategories.value = []
    } else {
      selectedCategories.value = [catId]
    }
  }
  currentPage.value = 1
  fetchData(true)
}
```

- [ ] **Step 2: 修改 fetchData 中的分类参数处理**

```javascript
if (selectedCategories.value.length > 0) {
  // 收集所有分类ID（包括子分类）
  let allCategoryIds = [...selectedCategories.value]
  for (const catId of selectedCategories.value) {
    const cat = categories.value.find(c => c.id === catId)
    if (cat && cat.children && cat.children.length > 0) {
      allCategoryIds = [...allCategoryIds, ...cat.children.map(c => c.id)]
    }
  }
  // 去重
  allCategoryIds = [...new Set(allCategoryIds)]
  params.categoryIds = allCategoryIds.join(',')
}
```

- [ ] **Step 3: 添加多选提示**

在分类按钮区域添加提示文字：

```html
<span class="hint">按住 Ctrl 可多选</span>
```

- [ ] **Step 4: 测试验证**

测试：
1. 单击分类是否正常筛选
2. Ctrl+点击是否支持多选
3. 多选后筛选结果是否正确

- [ ] **Step 5: Commit**

```bash
git add xianyuplus-web/src/views/Home.vue
git commit -m "feat: 支持 Ctrl 多选分类筛选"
```

---

## Task 5: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端和前端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`
Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 2: 测试价格筛选**

1. 设置最低价 100，最高价 500
2. 点击筛选
3. 验证结果中所有商品价格在 100-500 之间

- [ ] **Step 3: 测试成色筛选**

1. 选择"几乎全新"
2. 验证结果中所有商品成色为 2

- [ ] **Step 4: 测试排序**

1. 选择"价格低到高"
2. 验证结果按价格升序排列
3. 选择"价格高到低"
4. 验证结果按价格降序排列

- [ ] **Step 5: 测试多分类**

1. Ctrl+点击"电子产品"和"书籍教材"
2. 验证结果包含这两个分类下的商品

- [ ] **Step 6: 测试组合筛选**

1. 设置价格区间 + 成色 + 排序
2. 验证所有条件同时生效

- [ ] **Step 7: 测试重置**

1. 设置多个筛选条件
2. 点击重置
3. 验证所有筛选条件清空，显示全部商品

- [ ] **Step 8: Commit**

```bash
git add .
git commit -m "test: 搜索增强功能完整验证通过"
```

---

## 完成

搜索增强功能实施完成。下一步：商品评价系统。
