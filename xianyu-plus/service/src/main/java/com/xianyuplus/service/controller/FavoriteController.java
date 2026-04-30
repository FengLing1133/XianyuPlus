package com.xianyuplus.service.controller;

import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.entity.Favorite;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long productId) {
        return favoriteService.toggle(productId);
    }

    @GetMapping
    public Result<PageResult<Favorite>> list(PageDTO dto) {
        return favoriteService.list(dto);
    }
}
