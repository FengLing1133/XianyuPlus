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
import com.xianyuplus.common.service.NotificationService;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

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

        // 给管理员发送通知
        User reporter = userMapper.selectById(userId);
        String reporterName = reporter != null ? reporter.getNickname() : "未知用户";
        String reasonText = getReasonText(reason);
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getRole, 1);
        java.util.List<User> admins = userMapper.selectList(adminWrapper);
        for (User admin : admins) {
            notificationService.createNotification(
                admin.getId(),
                4, // 系统通知类型
                "新举报待处理",
                reporterName + " 举报了商品「" + product.getTitle() + "」，原因：" + reasonText,
                report.getId()
            );
        }

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

    private String getReasonText(Integer reason) {
        switch (reason) {
            case 1: return "虚假信息";
            case 2: return "违禁品";
            case 3: return "价格异常";
            case 4: return "恶意欺诈";
            case 5: return "其他";
            default: return "未知";
        }
    }
}
