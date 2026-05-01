package com.xianyuplus.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Notification;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.Report;
import com.xianyuplus.common.enums.ProductStatus;
import com.xianyuplus.common.mapper.NotificationMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.ReportMapper;
import com.xianyuplus.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @GetMapping
    public Result<?> getList(@RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(pageObj, wrapper);

        return Result.ok(result);
    }

    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = Integer.parseInt(body.get("status").toString());
        String adminNote = (String) body.get("adminNote");

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

                // 通知举报人：举报已处理，商品已下架
                Notification notification = new Notification();
                notification.setUserId(report.getReporterId());
                notification.setType(4);
                notification.setTitle("举报已处理");
                notification.setContent("您举报的商品「" + product.getTitle() + "」已被处理，商品已下架");
                notification.setRelatedId(report.getProductId());
                notification.setIsRead(0);
                notificationMapper.insert(notification);
            }
        } else if (status == 2) {
            // 通知举报人：举报已驳回
            Product product = productMapper.selectById(report.getProductId());
            String productTitle = product != null ? product.getTitle() : "未知商品";
            Notification notification = new Notification();
            notification.setUserId(report.getReporterId());
            notification.setType(4);
            notification.setTitle("举报已驳回");
            notification.setContent("您举报的商品「" + productTitle + "」经审核不符合违规条件，举报已驳回");
            notification.setRelatedId(report.getProductId());
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }

        return Result.ok();
    }
}
