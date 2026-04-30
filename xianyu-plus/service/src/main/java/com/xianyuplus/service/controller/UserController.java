package com.xianyuplus.service.controller;

import com.xianyuplus.common.entity.User;
import com.xianyuplus.common.utils.Result;
import com.xianyuplus.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        return userService.updateProfile(user);
    }

    @PostMapping("/avatar")
    public Result<String> avatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadAvatar(file.getBytes(), file.getOriginalFilename());
    }
}
