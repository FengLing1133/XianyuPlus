package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.entity.Review;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.OrderStatus;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.mapper.ReviewMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Result<?> create(Long orderId, Integer rating, String content) {
        Long userId = getCurrentUserId();

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 验证是否是买家
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("只有买家可以评价");
        }

        // 验证订单状态是否为已完成
        if (!order.getStatus().equals(OrderStatus.COMPLETED.getCode())) {
            return Result.error("只能评价已完成的订单");
        }

        // 验证是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        Long count = reviewMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.error("该订单已评价");
        }

        // 验证评分范围
        if (rating < 1 || rating > 5) {
            return Result.error("评分必须在1-5之间");
        }

        // 创建评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setProductId(order.getProductId());
        review.setBuyerId(userId);
        review.setSellerId(order.getSellerId());
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.insert(review);

        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> reply(Long reviewId, String reply) {
        Long userId = getCurrentUserId();

        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }

        // 验证是否是卖家
        if (!review.getSellerId().equals(userId)) {
            return Result.error("只有卖家可以回复");
        }

        // 验证是否已回复
        if (review.getSellerReply() != null) {
            return Result.error("已回复过该评价");
        }

        review.setSellerReply(reply);
        review.setReplyAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        return Result.ok();
    }

    @Override
    public Result<?> getByProduct(Long productId, Integer page, Integer size) {
        Page<Review> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);

        // 补充买家昵称
        result.getRecords().forEach(review -> {
            User buyer = userMapper.selectById(review.getBuyerId());
            if (buyer != null) {
                review.setBuyerId(buyer.getId()); // 临时存储，前端需要
            }
        });

        return Result.ok(result);
    }

    @Override
    public Result<?> getBySeller(Long sellerId, Integer page, Integer size) {
        Page<Review> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSellerId, sellerId)
               .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);
        return Result.ok(result);
    }

    @Override
    public Result<?> getSellerStats(Long sellerId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSellerId, sellerId);

        java.util.List<Review> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("avgRating", 0);
            stats.put("reviewCount", 0);
            stats.put("ratingDistribution", new int[]{0, 0, 0, 0, 0});
            return Result.ok(stats);
        }

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        // 计算评分分布
        int[] distribution = new int[5];
        reviews.forEach(r -> {
            if (r.getRating() >= 1 && r.getRating() <= 5) {
                distribution[r.getRating() - 1]++;
            }
        });

        Map<String, Object> stats = new HashMap<>();
        stats.put("avgRating", BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        stats.put("reviewCount", reviews.size());
        stats.put("ratingDistribution", distribution);

        return Result.ok(stats);
    }

    @Override
    public Result<?> checkCanReview(Long orderId) {
        Long userId = getCurrentUserId();

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 验证是否是买家
        if (!order.getBuyerId().equals(userId)) {
            return Result.ok(false);
        }

        // 验证订单状态
        if (!order.getStatus().equals(OrderStatus.COMPLETED.getCode())) {
            return Result.ok(false);
        }

        // 验证是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        Long count = reviewMapper.selectCount(wrapper);

        return Result.ok(count == 0);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
