package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Notification;
import com.xianyuplus.common.mapper.NotificationMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public Result<?> getList(Long userId, Integer page, Integer size) {
        Page<Notification> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(pageObj, wrapper);
        return Result.ok(result);
    }

    @Override
    public Result<?> getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        Long count = notificationMapper.selectCount(wrapper);
        return Result.ok(count);
    }

    @Override
    public Result<?> markAsRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return Result.ok();
    }

    @Override
    public Result<?> markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId);
        notificationMapper.delete(wrapper);
        return Result.ok();
    }

    @Override
    public void createNotification(Long userId, Integer type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
