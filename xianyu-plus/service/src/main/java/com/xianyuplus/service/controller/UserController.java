package com.xianyuplus.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.Review;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.OrderStatus;
import com.xianyuplus.common.enums.ProductStatus;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.ReviewMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        return userService.updateProfile(user);
    }

    @PostMapping("/avatar")
    public Result<String> avatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadAvatar(file.getBytes(), file.getOriginalFilename());
    }

    @GetMapping("/{id}/profile")
    public Result<?> getSellerProfile(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("createdAt", user.getCreatedAt());

        // 获取评价统计
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getSellerId, id);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        profile.put("avgRating", BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        profile.put("reviewCount", reviews.size());

        return Result.ok(profile);
    }

    @GetMapping("/{id}/stats")
    public Result<?> getSellerStats(@PathVariable Long id) {
        // 已完成订单数
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getSellerId, id)
                .eq(Order::getStatus, OrderStatus.COMPLETED.getCode());
        Long completedOrders = orderMapper.selectCount(orderWrapper);

        // 总销量（已售出商品数）
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getUserId, id)
                .eq(Product::getStatus, ProductStatus.SOLD.getCode());
        Long totalSold = productMapper.selectCount(productWrapper);

        // 好评率
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getSellerId, id);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);

        long positiveCount = reviews.stream()
                .filter(r -> r.getRating() >= 4)
                .count();

        BigDecimal positiveRate = reviews.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(positiveCount * 100.0 / reviews.size())
                        .setScale(1, RoundingMode.HALF_UP);

        Map<String, Object> stats = new HashMap<>();
        stats.put("completedOrders", completedOrders);
        stats.put("totalSold", totalSold);
        stats.put("positiveRate", positiveRate);

        return Result.ok(stats);
    }

    @GetMapping("/{id}/products")
    public Result<?> getSellerProducts(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "12") Integer size) {
        Page<Product> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, id)
                .eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
                .orderByDesc(Product::getCreatedAt);
        Page<Product> result = productMapper.selectPage(pageObj, wrapper);

        return Result.ok(PageResult.of(result));
    }
}
