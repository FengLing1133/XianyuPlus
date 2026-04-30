package com.xianyuplus.service.service;

import com.xianyuplus.common.dto.OrderDTO;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;

public interface OrderService {
    Result<?> create(OrderDTO dto);
    Result<PageResult<Order>> list(String type, PageDTO dto);
    Result<?> updateStatus(Long id, Integer status);
}
