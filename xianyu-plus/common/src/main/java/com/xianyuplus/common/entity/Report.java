package com.xianyuplus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long productId;
    private Long reporterId;
    private Integer reason;
    private String description;
    private Integer status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
