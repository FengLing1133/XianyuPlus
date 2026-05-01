package com.xianyuplus.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.Report;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.ReportMapper;
import com.xianyuplus.common.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReportMapper reportMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        long userCount = userMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);

        LocalDate today = LocalDate.now();
        long todayUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, today.atStartOfDay()));
        long todayProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .ge(Product::getCreatedAt, today.atStartOfDay()));

        // 待处理举报数量
        long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getStatus, 0));

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userCount);
        data.put("productCount", productCount);
        data.put("orderCount", orderCount);
        data.put("todayNewUsers", todayUsers);
        data.put("todayNewProducts", todayProducts);
        data.put("pendingReports", pendingReports);
        return Result.ok(data);
    }

    @GetMapping("/users")
    public Result<PageResult<User>> users(PageDTO dto,
                                          @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return Result.ok(PageResult.of(result));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> banUser(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok();
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> products(PageDTO dto,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
        Page<Product> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productMapper.selectPage(page, wrapper);

        for (Product p : result.getRecords()) {
            User seller = userMapper.selectById(p.getUserId());
            if (seller != null) {
                p.setSellerName(seller.getNickname());
            }
        }

        return Result.ok(PageResult.of(result));
    }

    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<Order>> orders(PageDTO dto) {
        Page<Order> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderMapper.selectPage(page, wrapper);

        for (Order order : result.getRecords()) {
            User buyer = userMapper.selectById(order.getBuyerId());
            User seller = userMapper.selectById(order.getSellerId());
            Product product = productMapper.selectById(order.getProductId());
            if (buyer != null) order.setBuyerName(buyer.getNickname());
            if (seller != null) order.setSellerName(seller.getNickname());
            if (product != null) order.setProductTitle(product.getTitle());
        }

        return Result.ok(PageResult.of(result));
    }
}
