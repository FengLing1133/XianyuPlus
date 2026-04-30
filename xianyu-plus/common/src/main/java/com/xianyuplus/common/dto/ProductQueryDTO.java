package com.xianyuplus.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductQueryDTO {
    private String keyword;
    private Long categoryId;
    private Integer condition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
    private Integer page = 1;
    private Integer size = 12;
}
