package com.xianyuplus.service.service;

import com.xianyuplus.common.dto.PageDTO;
import com.xianyuplus.common.dto.ProductDTO;
import com.xianyuplus.common.dto.ProductQueryDTO;
import com.xianyuplus.common.entity.Product;
import com.xianyuplus.common.utils.PageResult;
import com.xianyuplus.common.utils.Result;

public interface ProductService {
    Result<PageResult<Product>> list(ProductQueryDTO query);
    Result<Product> detail(Long id);
    Result<?> publish(ProductDTO dto);
    Result<?> update(Long id, ProductDTO dto);
    Result<?> updateStatus(Long id, Integer status);
    Result<PageResult<Product>> myProducts(PageDTO dto);
}
