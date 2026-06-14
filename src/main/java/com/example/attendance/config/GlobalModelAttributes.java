package com.example.attendance.config;

import com.example.attendance.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.attendance.controller")
public class GlobalModelAttributes {

    @ModelAttribute("loginUser")
    public User loginUser(HttpSession session) {
        if (session == null) return null;
        Object u = session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        return u instanceof User ? (User) u : null;
    }

    @ModelAttribute("isManager")
    public boolean isManager(HttpSession session) {
        User user = loginUser(session);
        if (user == null || user.getRole() == null) return false;
        return "ADMIN".equalsIgnoreCase(user.getRole()) || "TEACHER".equalsIgnoreCase(user.getRole());
    }
}
