package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private Integer rating;
    private String content;
    private String sellerReply;
    private LocalDateTime createdAt;
    private LocalDateTime replyAt;
}
