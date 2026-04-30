package com.xianyuplus.service.service;

import com.xianyuplus.common.dto.LoginDTO;
import com.xianyuplus.common.dto.RegisterDTO;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.Result;

import java.util.Map;

public interface AuthService {
    Result<?> register(RegisterDTO dto);
    Result<Map<String, Object>> login(LoginDTO dto);
    Result<User> info();
    Result<?> updatePassword(Map<String, String> params);
}
