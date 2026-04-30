package com.xianyuplus.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianyuplus.common.entity.Category;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.CategoryMapper;
import com.xianyuplus.service.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result<List<Category>> tree() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));

        List<Category> parents = all.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        for (Category parent : parents) {
            List<Category> children = all.stream()
                    .filter(c -> c.getParentId().equals(parent.getId()))
                    .collect(Collectors.toList());
            parent.setChildren(children);
        }

        return Result.ok(parents);
    }
}
