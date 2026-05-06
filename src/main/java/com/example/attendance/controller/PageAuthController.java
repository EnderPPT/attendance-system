package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String role,
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

        if (userService.existsByUsername(username)) {
            model.addAttribute("errorMsg", "用户名已存在");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword(passwordEncoder.encode(password));

        if (role == null || role.trim().isEmpty()) {
            user.setRole("STUDENT");
        } else {
            user.setRole(role);
        }

        userService.addUser(user);
        return "redirect:/login?registered=true";
    }

    @PostMapping("/page/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            return "login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMsg", "密码不能为空");
            return "login";
        }

        User user = userService.findByUsernameOrNull(username);

        if (user == null) {
            model.addAttribute("errorMsg", "用户名不存在");
            return "login";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("errorMsg", "用户名或密码错误");
            return "login";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "班级考勤管理系统首页");
        return "dashboard";
    }
}
