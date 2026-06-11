package com.example.attendance.controller;

import com.example.attendance.dto.ImportResult;
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

import java.util.ArrayList;
import java.util.Arrays;
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
            courses.stream()
                    .filter(c -> c.getCourseId().equals(courseId))
                    .findFirst()
                    .ifPresent(c -> model.addAttribute("selectedCourse", c));
        }
        return "selection-list";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long courseId,
                      @RequestParam String studentNumber,
                      RedirectAttributes redirectAttributes) {
        try {
            courseSelectionService.selectByUsername(courseId, studentNumber);
            redirectAttributes.addFlashAttribute("success", "选课成功：" + studentNumber);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "选课失败：" + e.getMessage());
        }
        return "redirect:/selection/page/list?courseId=" + courseId;
    }

    @PostMapping("/batch")
    public String batch(@RequestParam Long courseId,
                        @RequestParam String studentNumbers,
                        RedirectAttributes redirectAttributes) {
        try {
            List<String> numbers = new ArrayList<>();
            if (studentNumbers != null && !studentNumbers.trim().isEmpty()) {
                numbers.addAll(Arrays.asList(studentNumbers.split("[\\s,;，；]+")));
            }
            if (numbers.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请输入需要批量选课的学号");
                return "redirect:/selection/page/list?courseId=" + courseId;
            }
            ImportResult result = courseSelectionService.batchSelectByUsernames(courseId, numbers);
            String msg = "批量选课完成：成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条";
            if (result.getFailCount() > 0) {
                int show = Math.min(5, result.getFailCount());
                msg += "。失败示例：" + String.join(" | ", result.getFailReports().subList(0, show));
            }
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批量选课失败：" + e.getMessage());
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
