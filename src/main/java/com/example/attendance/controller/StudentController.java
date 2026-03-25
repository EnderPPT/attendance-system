package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.common.Result;

import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/student/create")
    public Result<String> createStudent(@RequestBody Student student) {
        try {
            String msg = studentService.createStudent(student);
            return Result.success(msg);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/student/info")
    public String getStudentInfo() {
        return "姓名：张三，学号：1，专业：计算机科学与技术";
    }

    @PostMapping("/student/attendance")
    public String studentAttendance(@RequestBody String studentId) {
        return "学号为 " + studentId + " 的学生打卡成功！";
    }

    @GetMapping("/student/courses")
    public List<String> getStudentCourses() {
        return Arrays.asList("JavaEE", "数据库原理", "计算机组成原理");
    }

    @GetMapping("/student/info/{studentId}")
    public Result<Student> getStudentInfo(@PathVariable String studentId) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName("张三");
        student.setClassName("JAVAEE开发实践");
        student.setAge(20);

        return Result.success(student);
    }

    @GetMapping("/student/list")
    public Result<List<Student>> getStudentList(
            @RequestParam String className,
            @RequestParam(defaultValue = "1") int page) {
        List<Student> list = new ArrayList<>();
        Student student = new Student();
        student.setStudentId("1");
        student.setName("张三");
        student.setClassName("JAVAEE开发实践");
        student.setAge(20);
        list.add(student);

        System.out.println("查询的班级：" + className + "， 页码："+ page);
        return Result.success(list);
    }
}
