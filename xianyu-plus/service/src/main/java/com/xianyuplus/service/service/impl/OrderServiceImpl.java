package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.dto.OrderDTO;
import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Order;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.entity.ProductImage;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.enums.OrderStatus;
import com.xianyuplus.common.enums.ProductStatus;
import com.xianyuplus.common.exception.BusinessException;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.OrderMapper;
import com.xianyuplus.common.mapper.ProductImageMapper;
import com.xianyuplus.common.mapper.ProductMapper;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<?> create(OrderDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User buyer = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getStatus().equals(ProductStatus.ON_SALE.getCode())) {
            throw new BusinessException("商品已不在售");
        }
        if (product.getUserId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己的商品");
        }

        // Create order
        Order order = new Order();
        order.setBuyerId(buyer.getId());
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatus.PENDING.getCode());
        orderMapper.insert(order);

        // Mark product as sold
        product.setStatus(ProductStatus.SOLD.getCode());
        productMapper.updateById(product);

        return Result.ok();
    }

    @Override
    public Result<PageResult<Order>> list(String type, PageDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Page<Order> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if ("sell".equals(type)) {
            wrapper.eq(Order::getSellerId, user.getId());
        } else {
            wrapper.eq(Order::getBuyerId, user.getId());
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(page, wrapper);

        for (Order order : result.getRecords()) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductTitle(product.getTitle());
                ProductImage firstImage = productImageMapper.selectOne(
                        new LambdaQueryWrapper<ProductImage>()
                                .eq(ProductImage::getProductId, product.getId())
                                .orderByAsc(ProductImage::getSortOrder)
                                .last("limit 1"));
                if (firstImage != null) {
                    order.setProductImage(firstImage.getUrl());
                }
            }
            User buyer = userMapper.selectById(order.getBuyerId());
            if (buyer != null) {
                order.setBuyerName(buyer.getNickname());
            }
            User seller = userMapper.selectById(order.getSellerId());
            if (seller != null) {
                order.setSellerName(seller.getNickname());
            }
        }

        return Result.ok(PageResult.of(result));
    }

    @Override
    @Transactional
    public Result<?> updateStatus(Long id, Integer status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        boolean isBuyer = order.getBuyerId().equals(user.getId());
        boolean isSeller = order.getSellerId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权执行此操作");
        }

        int current = order.getStatus();
        int target = status;

        // 买家操作
        if (isBuyer) {
            // PENDING → PAID: 确认付款
            if (current == OrderStatus.PENDING.getCode() && target == OrderStatus.PAID.getCode()) {
                order.setStatus(target);
                orderMapper.updateById(order);
                return Result.ok();
            }
            // PENDING → CANCELLED: 取消未付款订单
            if (current == OrderStatus.PENDING.getCode() && target == OrderStatus.CANCELLED.getCode()) {
                cancelOrder(order);
                return Result.ok();
            }
            // PAID → CANCELLED: 取消已付款订单（退款）
            if (current == OrderStatus.PAID.getCode() && target == OrderStatus.CANCELLED.getCode()) {
                cancelOrder(order);
                return Result.ok();
            }
            // SHIPPED → RECEIVED: 确认收货 → 自动完成
            if (current == OrderStatus.SHIPPED.getCode() && target == OrderStatus.RECEIVED.getCode()) {
                order.setStatus(OrderStatus.COMPLETED.getCode());
                orderMapper.updateById(order);
                return Result.ok();
            }
        }

        // 卖家操作
        if (isSeller) {
            // PAID → SHIPPED: 确认发货
            if (current == OrderStatus.PAID.getCode() && target == OrderStatus.SHIPPED.getCode()) {
                order.setStatus(target);
                orderMapper.updateById(order);
                return Result.ok();
            }
        }

        throw new BusinessException("无权执行此操作");
    }

    private void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);
        // 恢复商品为在售状态
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(ProductStatus.ON_SALE.getCode());
            productMapper.updateById(product);
        }
    }
}
