package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface ReportService {
    Result<?> create(Long productId, Integer reason, String description);
    Result<?> checkReported(Long productId);
    Result<?> getList(Integer status, Integer page, Integer size);
    Result<?> handle(Long id, Integer status, String adminNote);
}
