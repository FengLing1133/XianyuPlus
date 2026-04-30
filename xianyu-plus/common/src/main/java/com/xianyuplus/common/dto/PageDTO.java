package com.xianyuplus.common.dto;

import lombok.Data;

@Data
public class PageDTO {
    private Integer page = 1;
    private Integer size = 10;
}
