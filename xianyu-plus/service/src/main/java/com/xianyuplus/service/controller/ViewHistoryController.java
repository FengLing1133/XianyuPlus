package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ViewHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/view-history")
@RequiredArgsConstructor
public class ViewHistoryController {

    private final ViewHistoryService viewHistoryService;

    @PostMapping
    public Result<?> record(@RequestBody Map<String, Long> body) {
        return viewHistoryService.record(body.get("productId"));
    }

    @GetMapping
    public Result<?> getList(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size) {
        return viewHistoryService.getList(page, size);
    }

    @DeleteMapping
    public Result<?> clear() {
        return viewHistoryService.clear();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return viewHistoryService.delete(id);
    }
}
