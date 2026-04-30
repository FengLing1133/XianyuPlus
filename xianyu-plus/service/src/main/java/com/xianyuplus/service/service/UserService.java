package com.xianyuplus.service.service;

import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.Result;

public interface UserService {
    Result<User> getById(Long id);
    Result<?> updateProfile(User user);
    Result<String> uploadAvatar(byte[] bytes, String originalFilename);
}
