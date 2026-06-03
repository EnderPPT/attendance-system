package com.example.attendance.config;

import com.example.attendance.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    public static final String SESSION_USER = "loginUser";

    private static final Set<String> STUDENT_ALLOWED_PATHS = Set.of(
            "/dashboard",
            "/page/logout",
            "/attendance/page/checkin",
            "/attendance/create"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SESSION_USER);

        if (user == null) {
            return reject(request, response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }

        if (!hasManagerRole(user) && !STUDENT_ALLOWED_PATHS.contains(request.getServletPath())) {
            return reject(request, response, HttpServletResponse.SC_FORBIDDEN, "学生账号只能使用考勤打卡功能");
        }

        return true;
    }

    private boolean hasManagerRole(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole()) || "TEACHER".equalsIgnoreCase(user.getRole());
    }

    private boolean reject(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        if (isPageRequest(request)) {
            String target = status == HttpServletResponse.SC_UNAUTHORIZED
                    ? "/login"
                    : "/dashboard?permissionDenied=true";
            response.sendRedirect(request.getContextPath() + target);
            return false;
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + message + "\",\"data\":null}");
        return false;
    }

    private boolean isPageRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/dashboard") || path.equals("/page/logout") || path.contains("/page/");
    }
}
