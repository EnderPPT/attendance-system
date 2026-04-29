package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByUsernameOrNull(request.getUsername());

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole()
        );

        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());

        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            user.setRole("STUDENT");
        } else {
            user.setRole(request.getRole());
        }

        userService.addUser(user);

        return Result.success("注册成功");
    }
}
