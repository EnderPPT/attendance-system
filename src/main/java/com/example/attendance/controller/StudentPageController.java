package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StudentPageController {
    @Autowired
    private StudentService studentService;

    @GetMapping("/student/page/list")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        List<Student> students = studentService.search(keyword, sortBy, direction);

        model.addAttribute("students", students);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "student-list";
    }

    @GetMapping("/student/page/add")
    public String add(Model model) {
        model.addAttribute("student", new Student());
        return "student-form";
    }

    @GetMapping("/student/page/edit/{studentId}")
    public String editPage(@PathVariable String studentId, Model model) {
        Student student = studentService.getById(studentId);

        if (student == null) {
            model.addAttribute("errorMsg", "学生不存在");
            model.addAttribute("student", new Student());
            return "student-form";
        }

        model.addAttribute("student", student);
        return "student-form";
    }

    @PostMapping("/student/page/save")
    public String save(@ModelAttribute Student student, Model model) {
        String errorMsg = validateStudent(student);

        if (errorMsg != null) {
            model.addAttribute("errorMsg", errorMsg);
            model.addAttribute("student", student);
            return "student-form";
        }

        studentService.createStudent(student);

        return "redirect:/student/page/list";
    }

    private String validateStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            return "学号不能为空";
        }

        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return "姓名不能为空";
        }

        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            return "班级不能为空";
        }

        if (student.getGender() == null || student.getGender().trim().isEmpty()) {
            return "性别不能为空";
        }

        if (student.getBirthDate() == null) {
            return "出生日期不能为空";
        }

        if (student.getPhone() == null || student.getPhone().trim().isEmpty()) {
            return "联系方式不能为空";
        }

        if (!student.getPhone().matches("^1[3-9]\\d{9}$")) {
            return "联系方式格式不正确，请输入11位手机号";
        }

        if (student.getAge() == null || student.getAge() < 1 || student.getAge() > 100) {
            return "年龄必须在1到100之间";
        }

        return null;
    }

    @GetMapping("/student/page/delete/{studentId}")
    public String deleteOne(@PathVariable String studentId) {
        studentService.deleteById(studentId);
        return "redirect:/student/page/list";
    }

    @PostMapping("/student/page/deleteBatch")
    public String deleteBatch(@RequestParam(required = false) List<String> studentIds) {
        studentService.deleteBatch(studentIds);
        return "redirect:/student/page/list";
    }

    @org.springframework.beans.factory.annotation.Value("${file.upload.path}")
    private String uploadPath;

    // 1. 跳转到学生导入页面
    @GetMapping("/student/page/import")
    public String importPage() {
        return "student-import";
    }

    // 2. 处理学生文件上传
    @PostMapping("/student/page/import")
    public String importStudentFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (file.isEmpty() || (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "无效文件！请上传 Excel 文件");
            return "redirect:/student/page/import";
        }
        try {
            java.io.File dest = new java.io.File(uploadPath + file.getOriginalFilename());
            if(!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            com.example.attendance.dto.ImportResult result = studentService.importStudentsFromExcel(dest);

            String msg = "学生导入成功: " + result.getSuccessCount() + " 人。失败: " + result.getFailCount() + " 人。";
            if(result.getFailCount() > 0) {
                msg += " 失败原因示例：" + String.join(" | ", result.getFailReports().subList(0, Math.min(3, result.getFailCount())));
            }
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "服务器解析异常: " + e.getMessage());
        }
        return "redirect:/student/page/import";
    }
}
