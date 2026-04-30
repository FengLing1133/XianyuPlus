package com.xianyuplus.service.controller;

import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.dto.ProductDTO;
import com.xianyuplus.common.dto.ProductQueryDTO;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> list(ProductQueryDTO query) {
        return productService.list(query);
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return productService.detail(id);
    }

    @PostMapping
    public Result<?> publish(@Valid @RequestBody ProductDTO dto) {
        return productService.publish(dto);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return productService.update(id, dto);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productService.updateStatus(id, status);
    }

    @GetMapping("/my")
    public Result<PageResult<Product>> myProducts(PageDTO dto) {
        return productService.myProducts(dto);
    }
}
