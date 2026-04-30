package com.xianyuplus.service.controller;

import com.xianyuplus.common.dto.OrderDTO;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<?> create(@Valid @RequestBody OrderDTO dto) {
        return orderService.create(dto);
    }

    @GetMapping
    public Result<PageResult<Order>> list(@RequestParam(defaultValue = "buy") String type, PageDTO dto) {
        return orderService.list(type, dto);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }
}
