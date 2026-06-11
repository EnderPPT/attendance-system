package com.example.attendance.controller;

import com.example.attendance.config.AuthorizationInterceptor;
import com.example.attendance.dao.UserDao;
import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.entity.User;
import com.example.attendance.service.CourseSelectionService;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.LeaveApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/leave/page")
public class LeaveApplicationPageController {
    private final LeaveApplicationService leaveApplicationService;
    private final CourseService courseService;
    private final CourseSelectionService courseSelectionService;
    private final UserDao userDao;

    public LeaveApplicationPageController(LeaveApplicationService leaveApplicationService,
                                          CourseService courseService,
                                          CourseSelectionService courseSelectionService,
                                          UserDao userDao) {
        this.leaveApplicationService = leaveApplicationService;
        this.courseService = courseService;
        this.courseSelectionService = courseSelectionService;
        this.userDao = userDao;
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long studentId,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String status,
                       Model model) {
        List<LeaveApplication> applications;
        if (studentId != null) {
            applications = leaveApplicationService.getByStudentId(studentId);
        } else if (courseId != null) {
            applications = leaveApplicationService.getByCourseId(courseId);
        } else if (status != null && !status.trim().isEmpty()) {
            applications = leaveApplicationService.getByStatus(status);
        } else {
            applications = leaveApplicationService.getAll();
        }
        List<Course> courses = courseService.getAll();
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        Map<Long, User> userMap = buildUserMap(applications);
        model.addAttribute("applications", applications);
        model.addAttribute("courses", courses);
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("userMap", userMap);
        model.addAttribute("studentId", studentId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        return "leave-list";
    }

    private Map<Long, User> buildUserMap(List<LeaveApplication> applications) {
        java.util.Set<Long> ids = new java.util.LinkedHashSet<>();
        for (LeaveApplication a : applications) {
            if (a.getStudentId() != null) ids.add(a.getStudentId());
        }
        Map<Long, User> map = new java.util.HashMap<>();
        if (!ids.isEmpty()) {
            for (User u : userDao.findStudentsByIds(new java.util.ArrayList<>(ids))) {
                map.put(u.getId(), u);
            }
        }
        return map;
    }

    @GetMapping("/my")
    public String myList(Model model, HttpSession session) {
        User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        List<LeaveApplication> applications = user == null
                ? java.util.List.of()
                : leaveApplicationService.getByStudentId(user.getId());
        List<Course> courses = courseService.getAll();
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        model.addAttribute("applications", applications);
        model.addAttribute("courseMap", courseMap);
        return "leave-my";
    }

    @GetMapping("/apply")
    public String applyPage(@RequestParam(required = false) Long courseId, Model model, HttpSession session) {
        User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        boolean student = user != null && "STUDENT".equalsIgnoreCase(user.getRole());
        LeaveApplicationRequest request = new LeaveApplicationRequest();
        request.setCourseId(courseId);
        if (student) {
            request.setStudentId(user.getId());
        }
        model.addAttribute("request", request);
        if (student) {
            model.addAttribute("courses", courseSelectionService.getCoursesByStudent(user.getId()));
        } else {
            model.addAttribute("courses", courseService.getAll());
        }
        model.addAttribute("isStudent", student);
        model.addAttribute("sessionUserId", student ? user.getId() : null);
        return "leave-form";
    }

    @PostMapping("/apply")
    public String apply(LeaveApplicationRequest request, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        boolean student = user != null && "STUDENT".equalsIgnoreCase(user.getRole());
        if (student) {
            request.setStudentId(user.getId());
        }
        try {
            leaveApplicationService.apply(request);
            redirectAttributes.addFlashAttribute("success", "请假申请提交成功，等待审批");
            return student ? "redirect:/leave/page/my" : "redirect:/leave/page/list?studentId=" + request.getStudentId();
        } catch (Exception e) {
            model.addAttribute("request", request);
            if (student) {
                model.addAttribute("courses", courseSelectionService.getCoursesByStudent(user.getId()));
            } else {
                model.addAttribute("courses", courseService.getAll());
            }
            model.addAttribute("isStudent", student);
            model.addAttribute("sessionUserId", student ? user.getId() : null);
            model.addAttribute("error", e.getMessage());
            return "leave-form";
        }
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id,
                          @RequestParam boolean approved,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirectAttributes) {
        try {
            leaveApplicationService.approve(id, approved, remark);
            redirectAttributes.addFlashAttribute("success", approved ? "请假申请已通过" : "请假申请已驳回");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "审批失败：" + e.getMessage());
        }
        return "redirect:/leave/page/list";
    }
}
