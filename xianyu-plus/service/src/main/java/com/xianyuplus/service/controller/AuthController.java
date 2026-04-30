package com.xianyuplus.service.controller;

import com.xianyuplus.common.dto.LoginDTO;
import com.xianyuplus.common.dto.RegisterDTO;
import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @GetMapping("/info")
    public Result<User> info() {
        return authService.info();
    }

    @PutMapping("/password")
    public Result<?> password(@RequestBody Map<String, String> params) {
        return authService.updatePassword(params);
    }
}
