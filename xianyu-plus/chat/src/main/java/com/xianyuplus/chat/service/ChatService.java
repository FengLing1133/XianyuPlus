package com.xianyuplus.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianyuplus.common.entity.Message;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.mapper.MessageMapper;
import com.xianyuplus.common.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    public Message saveMessage(Long senderId, Long receiverId, Long productId, String content) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setProductId(productId);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);
        return msg;
    }

    public List<Message> getHistory(Long userId1, Long userId2) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w
                                .eq(Message::getSenderId, userId1).eq(Message::getReceiverId, userId2)
                                .or()
                                .eq(Message::getSenderId, userId2).eq(Message::getReceiverId, userId1))
                        .orderByAsc(Message::getCreatedAt));
    }

    public long getUnreadCount(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, 0));
    }

    public void markRead(Long senderId, Long receiverId) {
        List<Message> unread = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getSenderId, senderId)
                        .eq(Message::getReceiverId, receiverId)
                        .eq(Message::getIsRead, 0));
        for (Message msg : unread) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
    }

    public List<Map<String, Object>> getConversations(Long userId) {
        // Get all messages related to current user
        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
                        .orderByDesc(Message::getCreatedAt));

        // Group by conversation partner
        Map<Long, Message> latestMap = new LinkedHashMap<>();
        for (Message msg : messages) {
            Long partnerId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (!latestMap.containsKey(partnerId)) {
                latestMap.put(partnerId, msg);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMap.entrySet()) {
            Long partnerId = entry.getKey();
            Message latest = entry.getValue();
            User partner = userMapper.selectById(partnerId);
            long unread = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getSenderId, partnerId)
                            .eq(Message::getReceiverId, userId)
                            .eq(Message::getIsRead, 0));

            Map<String, Object> conv = new HashMap<>();
            conv.put("partnerId", partnerId);
            conv.put("partnerName", partner != null ? partner.getNickname() : "未知用户");
            conv.put("partnerAvatar", partner != null ? partner.getAvatar() : null);
            conv.put("lastMessage", latest.getContent());
            conv.put("lastTime", latest.getCreatedAt());
            conv.put("unread", unread);
            result.add(conv);
        }
        return result;
    }
}
