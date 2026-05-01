package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.dto.ProductDTO;
import com.xianyuplus.common.dto.ProductQueryDTO;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.ProductImage;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.ProductStatus;
import com.xianyuplus.common.exception.BusinessException;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.ProductImageMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<PageResult<Product>> list(ProductQueryDTO query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // Only show on-sale products
        wrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getCode());

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Product::getTitle, query.getKeyword());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }
        if (query.getCondition() != null) {
            wrapper.eq(Product::getCondition, query.getCondition());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, query.getMaxPrice());
        }

        // Sort
        String sort = query.getSort();
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        Page<Product> result = productMapper.selectPage(page, wrapper);

        // Attach images and seller name
        for (Product product : result.getRecords()) {
            List<ProductImage> images = productImageMapper.selectList(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, product.getId())
                            .orderByAsc(ProductImage::getSortOrder));
            product.setImages(images);

            User seller = userMapper.selectById(product.getUserId());
            if (seller != null) {
                product.setSellerName(seller.getNickname());
            }
        }

        return Result.ok(PageResult.of(result));
    }

    @Override
    public Result<Product> detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // Increment view count
        product.setViewCount(product.getViewCount() + 1);
        productMapper.updateById(product);

        // Load images
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSortOrder));
        product.setImages(images);

        // Load seller name
        User seller = userMapper.selectById(product.getUserId());
        if (seller != null) {
            product.setSellerName(seller.getNickname());
        }

        return Result.ok(product);
    }

    @Override
    @Transactional
    public Result<?> publish(ProductDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setUserId(user.getId());
        product.setCondition(dto.getCondition());
        product.setStatus(ProductStatus.ON_SALE.getCode());
        product.setViewCount(0);
        productMapper.insert(product);

        // Save images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(product.getId());
                image.setUrl(dto.getImages().get(i));
                image.setSortOrder(i);
                productImageMapper.insert(image);
            }
        }

        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> update(Long id, ProductDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (!product.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setCondition(dto.getCondition());
        productMapper.updateById(product);

        // Replace images
        productImageMapper.delete(
                new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, id));
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(id);
                image.setUrl(dto.getImages().get(i));
                image.setSortOrder(i);
                productImageMapper.insert(image);
            }
        }

        return Result.ok();
    }

    @Override
    public Result<?> updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (!product.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }

        product.setStatus(status);
        productMapper.updateById(product);
        return Result.ok();
    }

    @Override
    public Result<PageResult<Product>> myProducts(PageDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Page<Product> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, user.getId())
                .orderByDesc(Product::getCreatedAt);

        Page<Product> result = productMapper.selectPage(page, wrapper);

        for (Product product : result.getRecords()) {
            List<ProductImage> images = productImageMapper.selectList(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, product.getId())
                            .orderByAsc(ProductImage::getSortOrder));
            product.setImages(images);
        }

        return Result.ok(PageResult.of(result));
    }
}
