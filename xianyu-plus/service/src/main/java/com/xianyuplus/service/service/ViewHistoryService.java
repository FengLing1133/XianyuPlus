package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface ViewHistoryService {
    Result<?> record(Long productId);
    Result<?> getList(Integer page, Integer size);
    Result<?> clear();
    Result<?> delete(Long id);
}
