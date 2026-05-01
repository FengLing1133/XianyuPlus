package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.entity.ViewHistory;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.common.mapper.ViewHistoryMapper;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ViewHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewHistoryServiceImpl implements ViewHistoryService {

    private final ViewHistoryMapper viewHistoryMapper;
    private final UserMapper userMapper;

    private static final int MAX_HISTORY = 100;

    @Override
    public Result<?> record(Long productId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.ok(); // 未登录不记录
        }

        // 检查是否已存在
        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId)
               .eq(ViewHistory::getProductId, productId);
        ViewHistory existing = viewHistoryMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新时间
            existing.setCreatedAt(LocalDateTime.now());
            viewHistoryMapper.updateById(existing);
        } else {
            // 检查是否超过上限
            LambdaQueryWrapper<ViewHistory> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ViewHistory::getUserId, userId);
            Long count = viewHistoryMapper.selectCount(countWrapper);

            if (count >= MAX_HISTORY) {
                // 删除最早的记录
                LambdaQueryWrapper<ViewHistory> oldestWrapper = new LambdaQueryWrapper<>();
                oldestWrapper.eq(ViewHistory::getUserId, userId)
                             .orderByAsc(ViewHistory::getCreatedAt)
                             .last("LIMIT 1");
                viewHistoryMapper.delete(oldestWrapper);
            }

            // 插入新记录
            ViewHistory history = new ViewHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            viewHistoryMapper.insert(history);
        }

        return Result.ok();
    }

    @Override
    public Result<?> getList(Integer page, Integer size) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        Page<ViewHistory> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId)
               .orderByDesc(ViewHistory::getCreatedAt);
        Page<ViewHistory> result = viewHistoryMapper.selectPage(pageObj, wrapper);

        return Result.ok(result);
    }

    @Override
    public Result<?> clear() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getUserId, userId);
        viewHistoryMapper.delete(wrapper);

        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        LambdaQueryWrapper<ViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewHistory::getId, id)
               .eq(ViewHistory::getUserId, userId);
        viewHistoryMapper.delete(wrapper);

        return Result.ok();
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
