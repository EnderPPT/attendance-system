package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/course/page")
public class CoursePageController {
    private final CourseService courseService;

    public CoursePageController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long teacherId,
                       @RequestParam(required = false) String className,
                       Model model) {
        if (teacherId != null) {
            model.addAttribute("courses", courseService.getByTeacherId(teacherId));
        } else if (className != null && !className.trim().isEmpty()) {
            model.addAttribute("courses", courseService.getByClassName(className.trim()));
        } else {
            model.addAttribute("courses", courseService.getAll());
        }
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("className", className);
        return "course-list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("pageTitle", "新增课程");
        return "course-form";
    }

    @GetMapping("/edit/{courseId}")
    public String edit(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.getById(courseId));
        model.addAttribute("pageTitle", "编辑课程");
        return "course-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Course course, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (course.getCourseId() == null) {
                courseService.create(course);
                redirectAttributes.addFlashAttribute("success", "课程新增成功");
            } else {
                courseService.update(course.getCourseId(), course);
                redirectAttributes.addFlashAttribute("success", "课程更新成功");
            }
            return "redirect:/course/page/list";
        } catch (Exception e) {
            model.addAttribute("course", course);
            model.addAttribute("pageTitle", course.getCourseId() == null ? "新增课程" : "编辑课程");
            model.addAttribute("error", e.getMessage());
            return "course-form";
        }
    }

    @PostMapping("/delete/{courseId}")
    public String delete(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            courseService.delete(courseId);
            redirectAttributes.addFlashAttribute("success", "课程删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "课程删除失败：" + e.getMessage());
        }
        return "redirect:/course/page/list";
    }
}
