package com.xianyuplus.chat.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xianyuplus.chat.service.ChatService;
import com.xianyuplus.common.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.put(userId, session);
            redisTemplate.opsForSet().add("online_users", userId.toString());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        String payload = textMessage.getPayload();
        JSONObject json = JSONUtil.parseObj(payload);

        Long receiverId = json.getLong("receiverId");
        Long productId = json.getLong("productId");
        String content = json.getStr("content");

        // Save to database
        Message msg = chatService.saveMessage(senderId, receiverId, productId, content);

        // Send to receiver if online
        WebSocketSession receiverSession = ONLINE_USERS.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject response = JSONUtil.createObj()
                    .set("id", msg.getId())
                    .set("senderId", senderId)
                    .set("receiverId", receiverId)
                    .set("productId", productId)
                    .set("content", content)
                    .set("createdAt", msg.getCreatedAt().toString());
            receiverSession.sendMessage(new TextMessage(response.toString()));
        }

        // Broadcast via Redis Pub/Sub
        redisTemplate.convertAndSend("chat_message", msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            redisTemplate.opsForSet().remove("online_users", userId.toString());
        }
    }

    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    public static void sendToUser(Long userId, String message) throws Exception {
        WebSocketSession session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
