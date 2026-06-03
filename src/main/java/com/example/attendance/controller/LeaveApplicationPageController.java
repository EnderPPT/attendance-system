package com.example.attendance.controller;

import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.LeaveApplicationService;
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

    public LeaveApplicationPageController(LeaveApplicationService leaveApplicationService, CourseService courseService) {
        this.leaveApplicationService = leaveApplicationService;
        this.courseService = courseService;
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
        model.addAttribute("applications", applications);
        model.addAttribute("courses", courses);
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("studentId", studentId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        return "leave-list";
    }

    @GetMapping("/apply")
    public String applyPage(@RequestParam(required = false) Long courseId, Model model) {
        LeaveApplicationRequest request = new LeaveApplicationRequest();
        request.setCourseId(courseId);
        model.addAttribute("request", request);
        model.addAttribute("courses", courseService.getAll());
        return "leave-form";
    }

    @PostMapping("/apply")
    public String apply(LeaveApplicationRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            leaveApplicationService.apply(request);
            redirectAttributes.addFlashAttribute("success", "请假申请提交成功，等待审批");
            return "redirect:/leave/page/list?studentId=" + request.getStudentId();
        } catch (Exception e) {
            model.addAttribute("request", request);
            model.addAttribute("courses", courseService.getAll());
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
