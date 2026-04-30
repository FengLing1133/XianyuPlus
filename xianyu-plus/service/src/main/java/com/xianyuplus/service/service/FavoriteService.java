package com.xianyuplus.service.service;

import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Favorite;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;

import java.util.Map;

public interface FavoriteService {
    Result<Map<String, Object>> toggle(Long productId);
    Result<PageResult<Favorite>> list(PageDTO dto);
}
