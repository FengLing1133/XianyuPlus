package com.xianyuplus.chat.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xianyuplus.chat.service.ChatService;
import com.xianyuplus.common.entity.Message;
import com.xianyuplus.common.entity.Notification;
import com.xianyuplus.common.service.NotificationService;
import com.xianyuplus.common.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler implements ApplicationListener<NotificationEvent> {

    private static final ConcurrentHashMap<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NotificationService notificationService;

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

        // 创建消息通知
        notificationService.createNotification(
                msg.getReceiverId(),
                2, // 新消息类型
                "收到新消息",
                msg.getContent().length() > 50 ? msg.getContent().substring(0, 50) + "..." : msg.getContent(),
                msg.getId()
        );

        // Build response with server-generated id and timestamp
        // Long fields must be serialized as String to avoid JavaScript precision loss
        JSONObject response = JSONUtil.createObj()
                .set("id", msg.getId().toString())
                .set("senderId", senderId.toString())
                .set("receiverId", receiverId.toString())
                .set("productId", productId != null ? productId.toString() : null)
                .set("content", content)
                .set("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : "");

        // Send back to sender (confirmation with real id/timestamp)
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(response.toString()));
        }

        // Send to receiver if online (and not the same as sender)
        if (!receiverId.equals(senderId)) {
            WebSocketSession receiverSession = ONLINE_USERS.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(response.toString()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            redisTemplate.opsForSet().remove("online_users", userId.toString());
        }
    }

    @Override
    public void onApplicationEvent(NotificationEvent event) {
        Notification notification = event.getNotification();
        WebSocketSession session = ONLINE_USERS.get(notification.getUserId());
        if (session != null && session.isOpen()) {
            try {
                JSONObject msg = new JSONObject();
                msg.set("type", "notification");
                msg.set("data", JSONUtil.parse(notification));
                session.sendMessage(new TextMessage(msg.toString()));
            } catch (IOException e) {
                log.error("推送通知失败", e);
            }
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
