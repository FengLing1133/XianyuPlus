package com.xianyuplus.service.service;

import com.xianyuplus.common.utils.Result;

public interface FileService {
    Result<String> upload(byte[] bytes, String originalFilename);
}
