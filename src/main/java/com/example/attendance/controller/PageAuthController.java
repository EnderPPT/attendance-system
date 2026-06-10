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
    public String loginPage(@RequestParam(required = false) String registered,
                            @RequestParam(required = false) String passwordChanged,
                            Model model) {
        if ("true".equals(registered)) {
            model.addAttribute("successMsg", "注册成功，请登录");
        }
        if ("true".equals(passwordChanged)) {
            model.addAttribute("successMsg", "密码修改成功，请使用新密码登录");
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
                new User(user.getId(), user.getUsername(), null, user.getRealName(), user.getRole(), user.getMustChangePassword(), user.getCreateTime()));
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            return "redirect:/page/password?firstLogin=true";
        }
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

    @GetMapping("/page/password")
    public String passwordPage(@RequestParam(required = false) String firstLogin, Model model) {
        if ("true".equals(firstLogin)) {
            model.addAttribute("firstLogin", true);
        }
        return "password";
    }

    @PostMapping("/page/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model,
                                 HttpSession session) {
        User sessionUser = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("errorMsg", "请填写完整的新旧密码");
            return "password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次输入的新密码不一致");
            return "password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("errorMsg", "新密码长度不能少于 6 位");
            return "password";
        }
        if (newPassword.equals(oldPassword)) {
            model.addAttribute("errorMsg", "新密码不能与旧密码相同");
            return "password";
        }

        User current = userService.findById(sessionUser.getId());
        if (current == null || !passwordEncoder.matches(oldPassword, current.getPassword())) {
            model.addAttribute("errorMsg", "旧密码不正确");
            return "password";
        }

        current.setPassword(passwordEncoder.encode(newPassword));
        current.setMustChangePassword(false);
        userService.updateUser(current);
        session.invalidate();
        return "redirect:/login?passwordChanged=true";
    }
}
