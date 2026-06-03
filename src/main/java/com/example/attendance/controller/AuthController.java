package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.config.AuthorizationInterceptor;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }
        User user = userService.findByUsernameOrNull(request.getUsername().trim());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        session.setAttribute(AuthorizationInterceptor.SESSION_USER, sessionUser(user));

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
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword()) || isBlank(request.getRealName())) {
            return Result.error("用户名、密码和真实姓名不能为空");
        }
        String username = request.getUsername().trim();
        if (userService.existsByUsername(username)) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());

        user.setRole("STUDENT");

        userService.addUser(user);

        return Result.success("注册成功");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private User sessionUser(User user) {
        return new User(user.getId(), user.getUsername(), null, user.getRealName(), user.getRole(), user.getCreateTime());
    }
}
