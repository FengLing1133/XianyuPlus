package com.xianyuplus.common.enums;

import lombok.Getter;

@Getter
public enum ProductCondition {
    NEW(1, "全新"),
    LIKE_NEW(2, "几乎全新"),
    LIGHT_USE(3, "轻微使用"),
    VISIBLE_WEAR(4, "明显痕迹");

    private final int code;
    private final String desc;

    ProductCondition(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
