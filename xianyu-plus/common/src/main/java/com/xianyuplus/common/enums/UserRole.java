package com.xianyuplus.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER(0, "用户"),
    ADMIN(1, "管理员");

    private final int code;
    private final String desc;

    UserRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
