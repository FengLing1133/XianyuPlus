package com.xianyuplus.service.service;

import com.xianyuplus.common.entity.Category;
import com.xianyuplus.common.utils.Result;

import java.util.List;

public interface CategoryService {
    Result<List<Category>> tree();
}
