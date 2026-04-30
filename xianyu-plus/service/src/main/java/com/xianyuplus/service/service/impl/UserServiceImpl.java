package com.xianyuplus.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.exception.BusinessException;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.common.mapper.UserMapper;
import com.xianyuplus.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public Result<User> getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.ok(user);
    }

    @Override
    public Result<?> updateProfile(User form) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        user.setNickname(form.getNickname());
        user.setPhone(form.getPhone());
        userMapper.updateById(user);
        return Result.ok();
    }

    @Override
    public Result<String> uploadAvatar(byte[] bytes, String originalFilename) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = "avatar/" + IdUtil.fastSimpleUUID() + ext;
        Path filePath = Paths.get(uploadPath, filename);

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, bytes);
        } catch (IOException e) {
            throw new BusinessException("头像上传失败");
        }

        String url = "/uploads/" + filename.replace("\\", "/");
        user.setAvatar(url);
        userMapper.updateById(user);
        return Result.ok(url);
    }
}
