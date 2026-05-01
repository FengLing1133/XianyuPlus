# 商品举报实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现商品举报功能，用户可举报违规商品，管理员审核处理。

**Architecture:** 新增 report 表存储举报记录，后端提供用户举报和管理员审核 API，前端在商品详情页添加举报入口，管理员后台新增举报管理页面。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 3

---

## 文件结构

### 后端文件

| 文件 | 职责 |
|------|------|
| `common/.../entity/Report.java` | 举报实体类 |
| `common/.../mapper/ReportMapper.java` | MyBatis-Plus Mapper |
| `service/.../controller/ReportController.java` | 举报 API 控制器（用户端） |
| `admin/.../controller/AdminReportController.java` | 举报 API 控制器（管理端） |
| `service/.../service/ReportService.java` | 举报服务接口 |
| `service/.../service/impl/ReportServiceImpl.java` | 举报服务实现 |

### 前端文件

| 文件 | 职责 |
|------|------|
| `src/components/ReportForm.vue` | 举报表单弹窗组件 |
| `src/views/admin/Reports.vue` | 管理员举报管理页面 |
| `src/api/report.js` | 举报 API 封装 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `init.sql` | 添加 report 表 |
| `ProductDetail.vue` | 添加举报按钮 |
| `AdminLayout.vue` | 添加举报管理菜单 |
| `router/index.js` | 添加举报管理路由 |

---

## Task 1: 数据库 — 创建 report 表

**Files:**
- Modify: `xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 1: 添加 report 表定义**

在 `review` 表之后添加：

```sql
-- --------------------------------------------
-- 举报表
-- --------------------------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL COMMENT '被举报商品',
    `reporter_id` BIGINT NOT NULL COMMENT '举报者',
    `reason` TINYINT NOT NULL COMMENT '1虚假信息 2违禁品 3价格异常 4恶意欺诈 5其他',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '补充说明(可选)',
    `status` TINYINT DEFAULT 0 COMMENT '0待处理 1已处理 2已驳回',
    `admin_note` VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `handled_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';
```

- [ ] **Step 2: 执行数据库更新**

Run: `mysql -u root -p xianyu_plus < xianyu-plus/service/src/main/resources/db/init.sql`

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/resources/db/init.sql
git commit -m "feat(db): 添加 report 举报表"
```

---

## Task 2: 后端 — 创建 Report 实体和 Mapper

**Files:**
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Report.java`
- Create: `xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ReportMapper.java`

- [ ] **Step 1: 创建 Report 实体类**

```java
package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long productId;
    private Long reporterId;
    private Integer reason;
    private String description;
    private Integer status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
```

- [ ] **Step 2: 创建 ReportMapper 接口**

```java
package com.xianyuplus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyuplus.common.entity.Report;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl common`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/entity/Report.java
git add xianyu-plus/common/src/main/java/com/xianyuplus/common/mapper/ReportMapper.java
git commit -m "feat: 添加 Report 实体和 Mapper"
```

---

## Task 3: 后端 — 创建 ReportService

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ReportService.java`
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ReportServiceImpl.java`

- [ ] **Step 1: 创建 ReportService 接口**

```java
package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface ReportService {
    Result<?> create(Long productId, Integer reason, String description);
    Result<?> checkReported(Long productId);
    Result<?> getList(Integer status, Integer page, Integer size);
    Result<?> handle(Long id, Integer status, String adminNote);
}
```

- [ ] **Step 2: 创建 ReportServiceImpl 实现类**

```java
package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.Report;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.ProductStatus;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.ReportMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Result<?> create(Long productId, Integer reason, String description) {
        Long userId = getCurrentUserId();

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 不能举报自己的商品
        if (product.getUserId().equals(userId)) {
            return Result.error("不能举报自己的商品");
        }

        // 检查是否已举报过
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getProductId, productId)
               .eq(Report::getReporterId, userId);
        Long count = reportMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("您已举报过该商品");
        }

        // 创建举报
        Report report = new Report();
        report.setProductId(productId);
        report.setReporterId(userId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(0);
        reportMapper.insert(report);

        return Result.ok();
    }

    @Override
    public Result<?> checkReported(Long productId) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getProductId, productId)
               .eq(Report::getReporterId, userId);
        Long count = reportMapper.selectCount(wrapper);

        return Result.ok(count > 0);
    }

    @Override
    public Result<?> getList(Integer status, Integer page, Integer size) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(pageObj, wrapper);

        // 补充商品信息和举报者信息
        result.getRecords().forEach(report -> {
            Product product = productMapper.selectById(report.getProductId());
            if (product != null) {
                report.setProductId(product.getId()); // 临时使用，前端需要更多信息
            }
            User reporter = userMapper.selectById(report.getReporterId());
            if (reporter != null) {
                report.setReporterId(reporter.getId()); // 临时使用
            }
        });

        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result<?> handle(Long id, Integer status, String adminNote) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error("举报记录不存在");
        }

        // 更新举报状态
        LambdaUpdateWrapper<Report> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Report::getId, id)
                     .set(Report::getStatus, status)
                     .set(Report::getAdminNote, adminNote)
                     .set(Report::getHandledAt, LocalDateTime.now());
        reportMapper.update(null, updateWrapper);

        // 如果处理（下架商品）
        if (status == 1) {
            Product product = productMapper.selectById(report.getProductId());
            if (product != null) {
                product.setStatus(ProductStatus.OFF_SHELF.getCode());
                productMapper.updateById(product);
            }
        }

        return Result.ok();
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/ReportService.java
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/service/impl/ReportServiceImpl.java
git commit -m "feat: 添加 ReportService 服务层"
```

---

## Task 4: 后端 — 创建 ReportController

**Files:**
- Create: `xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ReportController.java`

- [ ] **Step 1: 创建 ReportController**

```java
package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long productId = Long.parseLong(body.get("productId").toString());
        Integer reason = Integer.parseInt(body.get("reason").toString());
        String description = (String) body.get("description");
        return reportService.create(productId, reason, description);
    }

    @GetMapping("/product/{productId}/check")
    public Result<?> checkReported(@PathVariable Long productId) {
        return reportService.checkReported(productId);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl service`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/service/src/main/java/com/xianyuplus/service/controller/ReportController.java
git commit -m "feat: 添加 ReportController 用户端 API"
```

---

## Task 5: 后端 — 创建 AdminReportController

**Files:**
- Create: `xianyu-plus/admin/src/main/java/com/xianyuplus/admin/controller/AdminReportController.java`

- [ ] **Step 1: 创建 AdminReportController**

```java
package com.xianyuplus.admin.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public Result<?> getList(@RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        return reportService.getList(status, page, size);
    }

    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = Integer.parseInt(body.get("status").toString());
        String adminNote = (String) body.get("adminNote");
        return reportService.handle(id, status, adminNote);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd xianyu-plus && mvn compile -pl admin`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add xianyu-plus/admin/src/main/java/com/xianyuplus/admin/controller/AdminReportController.java
git commit -m "feat: 添加 AdminReportController 管理端 API"
```

---

## Task 6: 前端 — 创建举报 API 封装

**Files:**
- Create: `xianyuplus-web/src/api/report.js`

- [ ] **Step 1: 创建举报 API 文件**

```javascript
import request from './request'

export function createReport(productId, reason, description) {
  return request.post('/report', { productId, reason, description })
}

export function checkReported(productId) {
  return request.get(`/report/product/${productId}/check`)
}

export function getReports(status, page = 1, size = 10) {
  return request.get('/admin/report', { params: { status, page, size } })
}

export function handleReport(id, status, adminNote) {
  return request.put(`/admin/report/${id}/handle`, { status, adminNote })
}
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/api/report.js
git commit -m "feat: 添加举报 API 封装"
```

---

## Task 7: 前端 — 创建 ReportForm 组件

**Files:**
- Create: `xianyuplus-web/src/components/ReportForm.vue`

- [ ] **Step 1: 创建 ReportForm 组件**

```vue
<template>
  <div v-if="visible" class="report-modal" @click.self="close">
    <div class="report-form">
      <div class="form-header">
        <h3>举报商品</h3>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="form-body">
        <div class="reason-section">
          <label>举报原因</label>
          <div class="reason-options">
            <label
              v-for="option in reasonOptions"
              :key="option.value"
              class="reason-option"
              :class="{ active: reason === option.value }"
            >
              <input
                type="radio"
                :value="option.value"
                v-model="reason"
              >
              <span>{{ option.label }}</span>
            </label>
          </div>
        </div>

        <div class="desc-section">
          <label>补充说明（可选）</label>
          <textarea
            v-model="description"
            placeholder="请详细描述举报原因..."
            maxlength="500"
            rows="3"
          ></textarea>
          <span class="char-count">{{ description.length }}/500</span>
        </div>
      </div>

      <div class="form-footer">
        <button class="btn-cancel" @click="close">取消</button>
        <button class="btn-submit" :disabled="reason === 0 || submitting" @click="submit">
          {{ submitting ? '提交中...' : '提交举报' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createReport } from '@/api/report'
import { Toast } from '@/utils/toast'

const props = defineProps({
  visible: Boolean,
  productId: Number
})

const emit = defineEmits(['close', 'success'])

const reason = ref(0)
const description = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 1, label: '虚假信息' },
  { value: 2, label: '违禁品' },
  { value: 3, label: '价格异常' },
  { value: 4, label: '恶意欺诈' },
  { value: 5, label: '其他' }
]

function close() {
  emit('close')
  reason.value = 0
  description.value = ''
}

async function submit() {
  if (reason.value === 0) {
    Toast.warning('请选择举报原因')
    return
  }

  submitting.value = true
  try {
    await createReport(props.productId, reason.value, description.value)
    Toast.success('举报已提交，我们会尽快处理')
    emit('success')
    close()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.report-form {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.form-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.form-body {
  padding: 24px;
}

.reason-section {
  margin-bottom: 20px;
}

.reason-section label,
.desc-section label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.reason-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.reason-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.reason-option:hover {
  border-color: var(--primary);
}

.reason-option.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}

.reason-option input {
  display: none;
}

.desc-section {
  position: relative;
}

.desc-section textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.desc-section textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.char-count {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 12px;
  color: #999;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel:hover {
  border-color: #999;
}

.btn-submit {
  padding: 10px 24px;
  border: none;
  background: #ef4444;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover:not(:disabled) {
  background: #dc2626;
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/components/ReportForm.vue
git commit -m "feat: 添加 ReportForm 举报表单组件"
```

---

## Task 8: 前端 — 商品详情页添加举报按钮

**Files:**
- Modify: `xianyuplus-web/src/views/ProductDetail.vue`

- [ ] **Step 1: 导入组件和 API**

```javascript
import ReportForm from '@/components/ReportForm.vue'
import { checkReported } from '@/api/report'
```

- [ ] **Step 2: 添加状态变量**

```javascript
const showReportForm = ref(false)
const hasReported = ref(false)
```

- [ ] **Step 3: 检查是否已举报**

在获取商品详情后添加：

```javascript
if (userStore.token && product.value.userId !== userStore.userInfo?.id) {
  const res = await checkReported(product.value.id)
  if (res.code === 200) {
    hasReported.value = res.data
  }
}
```

- [ ] **Step 4: 添加举报按钮**

在商品操作区域添加：

```html
<button
  v-if="userStore.token && product.userId !== userStore.userInfo?.id"
  class="btn-report"
  :class="{ reported: hasReported }"
  :disabled="hasReported"
  @click="showReportForm = true"
>
  {{ hasReported ? '已举报' : '举报' }}
</button>
```

- [ ] **Step 5: 添加 ReportForm 组件**

在模板底部添加：

```html
<ReportForm
  :visible="showReportForm"
  :product-id="product.id"
  @close="showReportForm = false"
  @success="hasReported = true"
/>
```

- [ ] **Step 6: 添加样式**

```css
.btn-report {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-report:hover:not(:disabled) {
  border-color: #ef4444;
  color: #ef4444;
}

.btn-report.reported {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}
```

- [ ] **Step 7: Commit**

```bash
git add xianyuplus-web/src/views/ProductDetail.vue
git commit -m "feat: 商品详情页添加举报按钮"
```

---

## Task 9: 前端 — 创建管理员举报管理页面

**Files:**
- Create: `xianyuplus-web/src/views/admin/Reports.vue`

- [ ] **Step 1: 创建 Reports.vue 页面**

```vue
<template>
  <div class="reports-page">
    <div class="page-header">
      <h2>举报管理</h2>
      <div class="filter-bar">
        <select v-model="statusFilter" @change="fetchData">
          <option value="">全部状态</option>
          <option value="0">待处理</option>
          <option value="1">已处理</option>
          <option value="2">已驳回</option>
        </select>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>商品ID</th>
            <th>举报者ID</th>
            <th>举报原因</th>
            <th>补充说明</th>
            <th>状态</th>
            <th>举报时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="report in reports" :key="report.id">
            <td>{{ report.productId }}</td>
            <td>{{ report.reporterId }}</td>
            <td>{{ reasonText(report.reason) }}</td>
            <td>{{ report.description || '-' }}</td>
            <td>
              <span :class="'status-tag status-' + report.status">
                {{ statusText(report.status) }}
              </span>
            </td>
            <td>{{ formatTime(report.createdAt) }}</td>
            <td>
              <button
                v-if="report.status === 0"
                class="btn-handle"
                @click="openHandle(report)"
              >
                处理
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="total > 10" class="pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <span>{{ page }} / {{ Math.ceil(total / 10) }}</span>
      <button :disabled="page >= Math.ceil(total / 10)" @click="goPage(page + 1)">下一页</button>
    </div>

    <!-- 处理弹窗 -->
    <div v-if="handlingReport" class="handle-modal" @click.self="cancelHandle">
      <div class="handle-form">
        <h4>处理举报</h4>
        <div class="form-group">
          <label>处理方式</label>
          <div class="handle-options">
            <label>
              <input type="radio" :value="1" v-model="handleStatus">
              <span>处理（下架商品）</span>
            </label>
            <label>
              <input type="radio" :value="2" v-model="handleStatus">
              <span>驳回（举报无效）</span>
            </label>
          </div>
        </div>
        <div class="form-group">
          <label>处理备注</label>
          <textarea v-model="adminNote" placeholder="输入处理备注..." rows="3"></textarea>
        </div>
        <div class="form-actions">
          <button @click="cancelHandle">取消</button>
          <button @click="confirmHandle" :disabled="!handleStatus">确认处理</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReports, handleReport } from '@/api/report'
import { Toast } from '@/utils/toast'

const reports = ref([])
const total = ref(0)
const page = ref(1)
const statusFilter = ref('')

const handlingReport = ref(null)
const handleStatus = ref(0)
const adminNote = ref('')

const reasonMap = {
  1: '虚假信息',
  2: '违禁品',
  3: '价格异常',
  4: '恶意欺诈',
  5: '其他'
}

const statusMap = {
  0: '待处理',
  1: '已处理',
  2: '已驳回'
}

function reasonText(reason) {
  return reasonMap[reason] || '未知'
}

function statusText(status) {
  return statusMap[status] || '未知'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function fetchData() {
  const res = await getReports(statusFilter.value || undefined, page.value, 10)
  if (res.code === 200) {
    reports.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function goPage(p) {
  page.value = p
  fetchData()
}

function openHandle(report) {
  handlingReport.value = report
  handleStatus.value = 0
  adminNote.value = ''
}

function cancelHandle() {
  handlingReport.value = null
  handleStatus.value = 0
  adminNote.value = ''
}

async function confirmHandle() {
  if (!handleStatus.value) {
    Toast.warning('请选择处理方式')
    return
  }

  await handleReport(handlingReport.value.id, handleStatus.value, adminNote.value)
  Toast.success('处理成功')
  cancelHandle()
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.reports-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.table-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

td {
  font-size: 14px;
  color: #666;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-0 {
  background: #fff3e0;
  color: #e65100;
}

.status-1 {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-2 {
  background: #fef0f0;
  color: #c62828;
}

.btn-handle {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-handle:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.handle-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.handle-form {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
}

.handle-form h4 {
  margin: 0 0 20px;
  font-size: 18px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.handle-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.handle-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}

.handle-options input {
  margin: 0;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.form-actions button {
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button:first-child {
  border: 1px solid #ddd;
  background: #fff;
}

.form-actions button:last-child {
  border: none;
  background: var(--primary);
  color: #fff;
}

.form-actions button:last-child:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add xianyuplus-web/src/views/admin/Reports.vue
git commit -m "feat: 添加管理员举报管理页面"
```

---

## Task 10: 前端 — 添加路由和菜单

**Files:**
- Modify: `xianyuplus-web/src/router/index.js`
- Modify: `xianyuplus-web/src/components/AdminLayout.vue`

- [ ] **Step 1: 添加路由**

在 admin 路由的 children 中添加：

```javascript
{
  path: 'reports',
  name: 'AdminReports',
  component: () => import('@/views/admin/Reports.vue'),
  meta: { title: '举报管理', admin: true }
}
```

- [ ] **Step 2: 添加菜单项**

在 AdminLayout.vue 的侧边栏菜单中添加：

```html
<router-link to="/admin/reports" class="menu-item">
  <span class="menu-icon">🚩</span>
  <span>举报管理</span>
</router-link>
```

- [ ] **Step 3: Commit**

```bash
git add xianyuplus-web/src/router/index.js
git add xianyuplus-web/src/components/AdminLayout.vue
git commit -m "feat: 添加举报管理路由和菜单"
```

---

## Task 11: 测试 — 完整功能验证

- [ ] **Step 1: 启动后端和前端**

Run: `cd xianyu-plus && mvn -pl service spring-boot:run`
Run: `cd xianyuplus-web && npm run dev`

- [ ] **Step 2: 测试举报流程**

1. 用 test2 登录，访问 test1 的商品
2. 点击"举报"按钮
3. 选择举报原因，输入补充说明
4. 提交举报
5. 验证按钮变为"已举报"

- [ ] **Step 3: 测试管理员处理**

1. 用管理员账号登录
2. 进入举报管理页面
3. 找到待处理的举报
4. 点击"处理"，选择"处理（下架商品）"
5. 确认处理
6. 验证商品状态变为已下架

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "test: 商品举报功能完整验证通过"
```

---

## 完成

商品举报功能实施完成。下一步：卖家主页。
