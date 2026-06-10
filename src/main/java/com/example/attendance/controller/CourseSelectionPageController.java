package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.entity.User;
import com.example.attendance.service.CourseSelectionService;
import com.example.attendance.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/selection/page")
public class CourseSelectionPageController {
    private final CourseSelectionService courseSelectionService;
    private final CourseService courseService;

    public CourseSelectionPageController(CourseSelectionService courseSelectionService,
                                         CourseService courseService) {
        this.courseSelectionService = courseSelectionService;
        this.courseService = courseService;
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long courseId, Model model) {
        List<Course> courses = courseService.getAll();
        model.addAttribute("courses", courses);
        model.addAttribute("courseId", courseId);
        if (courseId != null) {
            model.addAttribute("students", courseSelectionService.getStudentsByCourse(courseId));
            courseService.getAll().stream()
                    .filter(c -> c.getCourseId().equals(courseId))
                    .findFirst()
                    .ifPresent(c -> model.addAttribute("selectedCourse", c));
        }
        return "selection-list";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long courseId,
                      @RequestParam Long studentId,
                      RedirectAttributes redirectAttributes) {
        try {
            courseSelectionService.select(courseId, studentId);
            redirectAttributes.addFlashAttribute("success", "选课成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "选课失败：" + e.getMessage());
        }
        return "redirect:/selection/page/list?courseId=" + courseId;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long courseId,
                         @RequestParam Long studentId,
                         RedirectAttributes redirectAttributes) {
        try {
            courseSelectionService.unselect(courseId, studentId);
            redirectAttributes.addFlashAttribute("success", "已退选");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "退选失败：" + e.getMessage());
        }
        return "redirect:/selection/page/list?courseId=" + courseId;
    }

    @GetMapping("/my")
    public String mySelection(Model model, jakarta.servlet.http.HttpSession session) {
        User user = (User) session.getAttribute(com.example.attendance.config.AuthorizationInterceptor.SESSION_USER);
        List<Course> myCourses = user == null ? java.util.List.of()
                : courseSelectionService.getCoursesByStudent(user.getId());
        model.addAttribute("courses", myCourses);
        return "selection-my";
    }
}
