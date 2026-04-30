package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long productId;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String senderName;

    @TableField(exist = false)
    private String receiverName;
}
