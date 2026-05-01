package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

import java.math.BigDecimal;

public interface ReviewService {
    Result<?> create(Long orderId, Integer rating, String content);
    Result<?> reply(Long reviewId, String reply);
    Result<?> getByProduct(Long productId, Integer page, Integer size);
    Result<?> getBySeller(Long sellerId, Integer page, Integer size);
    Result<?> getSellerStats(Long sellerId);
    Result<?> checkCanReview(Long orderId);
}
