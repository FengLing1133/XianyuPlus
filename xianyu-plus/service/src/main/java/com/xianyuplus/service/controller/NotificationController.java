package com.xianyuplus.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @GetMapping
    public Result<?> getList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getCurrentUserId();
        return notificationService.getList(userId, page, size);
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.markAsRead(id, userId);
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = getCurrentUserId();
        return notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return notificationService.delete(id, userId);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
