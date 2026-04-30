package com.xianyuplus.common.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    ON_SALE(1, "在售"),
    SOLD(2, "已售出"),
    OFF_SHELF(3, "已下架");

    private final int code;
    private final String desc;

    ProductStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
