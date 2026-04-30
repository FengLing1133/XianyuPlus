package com.xianyuplus.service.controller;

import com.xianyuplus.chat.service.ChatService;
import com.xianyuplus.common.entity.Message;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{userId}")
    public Result<List<Message>> history(@PathVariable Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        List<Message> messages = chatService.getHistory(currentUser.getId(), userId);

        // Mark messages from partner as read
        chatService.markRead(userId, currentUser.getId());

        return Result.ok(messages);
    }

    @GetMapping("/unread")
    public Result<Map<String, Long>> unread() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        long count = chatService.getUnreadCount(currentUser.getId());
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.ok(result);
    }

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        return Result.ok(chatService.getConversations(currentUser.getId()));
    }
}
