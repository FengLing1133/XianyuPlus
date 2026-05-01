package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long orderId = Long.parseLong(body.get("orderId").toString());
        Integer rating = Integer.parseInt(body.get("rating").toString());
        String content = (String) body.get("content");
        return reviewService.create(orderId, rating, content);
    }

    @PutMapping("/{id}/reply")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return reviewService.reply(id, body.get("reply"));
    }

    @GetMapping("/product/{productId}")
    public Result<?> getByProduct(@PathVariable Long productId,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getByProduct(productId, page, size);
    }

    @GetMapping("/seller/{sellerId}")
    public Result<?> getBySeller(@PathVariable Long sellerId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getBySeller(sellerId, page, size);
    }

    @GetMapping("/seller/{sellerId}/stats")
    public Result<?> getSellerStats(@PathVariable Long sellerId) {
        return reviewService.getSellerStats(sellerId);
    }

    @GetMapping("/check/{orderId}")
    public Result<?> checkCanReview(@PathVariable Long orderId) {
        return reviewService.checkCanReview(orderId);
    }
}
