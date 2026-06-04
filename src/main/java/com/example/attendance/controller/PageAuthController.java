package com.example.attendance.controller;

import com.example.attendance.config.AuthorizationInterceptor;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class PageAuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String registered, Model model) {
        if ("true".equals(registered)) {
            model.addAttribute("successMsg", "注册成功，请登录");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/page/register")
    public String register(
            @RequestParam String username,
            @RequestParam String realName,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            return "register";
        }

        if (realName == null || realName.trim().isEmpty()) {
            model.addAttribute("errorMsg", "真实姓名不能为空");
            return "register";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMsg", "密码不能为空");
            return "register";
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("errorMsg", "确认密码不能为空");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次输入的密码不一致");
            return "register";
        }

        username = username.trim();
        realName = realName.trim();
        if (userService.existsByUsername(username)) {
            model.addAttribute("errorMsg", "用户名已存在");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword(passwordEncoder.encode(password));

        user.setRole("STUDENT");

        userService.addUser(user);
        return "redirect:/login?registered=true";
    }

    @PostMapping("/page/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            return "login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMsg", "密码不能为空");
            return "login";
        }

        User user = userService.findByUsernameOrNull(username.trim());

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("errorMsg", "用户名或密码错误");
            return "login";
        }

        session.setAttribute(AuthorizationInterceptor.SESSION_USER,
                new User(user.getId(), user.getUsername(), null, user.getRealName(), user.getRole(), user.getCreateTime()));
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String permissionDenied,
                            Model model,
                            HttpSession session) {
        User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        model.addAttribute("title", "班级考勤管理系统首页");
        model.addAttribute("loginUser", user);
        model.addAttribute("isManager", "ADMIN".equalsIgnoreCase(user.getRole()) || "TEACHER".equalsIgnoreCase(user.getRole()));
        if ("true".equals(permissionDenied)) {
            model.addAttribute("errorMsg", "学生账号只能使用考勤打卡功能");
        }
        return "dashboard";
    }

    @GetMapping("/page/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
