package com.example.attendance.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class StudentController {
    @GetMapping("/student/info")
    public String getStudentInfo() {
        return "姓名：张三，学号：2023123456，班级：软件工程1班";
    }

    @PostMapping("/student/attendance")
    public String studentAttendance(@RequestBody String studentId) {
        return "学号为 " + studentId + " 的学生打卡成功！";
    }

    @GetMapping("/student/courses")
    public List<String> getStudentCourses() {
        return Arrays.asList("Java程序设计", "数据库原理", "Web前端开发");
    }
}
