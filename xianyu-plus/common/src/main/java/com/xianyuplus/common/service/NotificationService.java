package com.xianyuplus.common.service;

import com.xianyuplus.common.utils.Result;

public interface NotificationService {
    Result<?> getList(Long userId, Integer page, Integer size);
    Result<?> getUnreadCount(Long userId);
    Result<?> markAsRead(Long id, Long userId);
    Result<?> markAllAsRead(Long userId);
    Result<?> delete(Long id, Long userId);
    void createNotification(Long userId, Integer type, String title, String content, Long relatedId);
}
