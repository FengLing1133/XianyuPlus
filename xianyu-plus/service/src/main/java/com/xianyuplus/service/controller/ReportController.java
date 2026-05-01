package com.xianyuplus.service.controller;

import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long productId = Long.parseLong(body.get("productId").toString());
        Integer reason = Integer.parseInt(body.get("reason").toString());
        String description = (String) body.get("description");
        return reportService.create(productId, reason, description);
    }

    @GetMapping("/product/{productId}/check")
    public Result<?> checkReported(@PathVariable Long productId) {
        return reportService.checkReported(productId);
    }
}
