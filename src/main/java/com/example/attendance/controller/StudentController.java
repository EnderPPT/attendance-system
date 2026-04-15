package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.common.Result;

import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/create")
    public Result<String> createStudent(@RequestBody Student student) {
        try {
            return Result.success(studentService.createStudent(student));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{studentId}")
    public Result<Student> getById(@PathVariable String studentId) {
        return Result.success(studentService.getById(studentId));
    }

    @GetMapping("/all")
    public Result<List<Student>> getAll() {
        return Result.success(studentService.getAll());
    }

    @GetMapping("/list")
    public Result<List<Student>> getByClassName(@RequestParam String className) {
        return  Result.success(studentService.getByClassName(className));
    }

    @DeleteMapping("/{studentId}")
    public Result<String> delete(@PathVariable String studentId) {
        studentService.deleteById(studentId);
        return Result.success("删除成功");
    }
}
