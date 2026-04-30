package com.xianyuplus.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xianyuplus.common.exception.BusinessException;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public Result<String> upload(byte[] bytes, String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = "products/" + IdUtil.fastSimpleUUID() + ext;
        Path filePath = Paths.get(uploadPath, filename);

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, bytes);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        String url = "/uploads/" + filename.replace("\\", "/");
        return Result.ok(url);
    }
}
