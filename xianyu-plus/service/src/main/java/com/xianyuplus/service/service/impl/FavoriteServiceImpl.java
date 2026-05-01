package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Favorite;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.ProductImage;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.FavoriteMapper;
import com.xianyuplus.common.mapper.ProductImageMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.service.NotificationService;
import com.xianyuplus.service.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Result<Map<String, Object>> toggle(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Favorite exist = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getId())
                        .eq(Favorite::getProductId, productId));

        Map<String, Object> result = new HashMap<>();
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            result.put("favorited", false);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(user.getId());
            favorite.setProductId(productId);
            favoriteMapper.insert(favorite);
            result.put("favorited", true);

            // 创建收藏通知
            Product product = productMapper.selectById(productId);
            if (product != null && !product.getUserId().equals(user.getId())) {
                notificationService.createNotification(
                    product.getUserId(),
                    3, // 商品相关类型
                    "商品被收藏",
                    "您发布的商品 \"" + product.getTitle() + "\" 被人收藏了",
                    productId
                );
            }
        }
        return Result.ok(result);
    }

    @Override
    public Result<PageResult<Favorite>> list(PageDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Page<Favorite> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> result = favoriteMapper.selectPage(page, wrapper);

        for (Favorite fav : result.getRecords()) {
            Product product = productMapper.selectById(fav.getProductId());
            if (product != null) {
                fav.setProductTitle(product.getTitle());
                fav.setProductPrice(product.getPrice());
                ProductImage firstImage = productImageMapper.selectOne(
                        new LambdaQueryWrapper<ProductImage>()
                                .eq(ProductImage::getProductId, product.getId())
                                .orderByAsc(ProductImage::getSortOrder)
                                .last("limit 1"));
                if (firstImage != null) {
                    fav.setProductImage(firstImage.getUrl());
                }
            }
        }

        return Result.ok(PageResult.of(result));
    }
}
