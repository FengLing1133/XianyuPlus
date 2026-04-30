package com.xianyuplus.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(0, "待付款"),
    PAID(1, "已付款"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
