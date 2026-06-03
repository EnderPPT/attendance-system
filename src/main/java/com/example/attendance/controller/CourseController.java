package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public Result<Course> create(@RequestBody Course course) {
        return Result.success(courseService.create(course));
    }

    @PutMapping("/{courseId}")
    public Result<Course> update(@PathVariable Long courseId, @RequestBody Course course) {
        return Result.success(courseService.update(courseId, course));
    }

    @GetMapping("/{courseId}")
    public Result<Course> getById(@PathVariable Long courseId) {
        return Result.success(courseService.getById(courseId));
    }

    @GetMapping
    public Result<List<Course>> getAll() {
        return Result.success(courseService.getAll());
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getByTeacherId(@PathVariable Long teacherId) {
        return Result.success(courseService.getByTeacherId(teacherId));
    }

    @GetMapping("/class")
    public Result<List<Course>> getByClassName(@RequestParam String className) {
        return Result.success(courseService.getByClassName(className));
    }

    @DeleteMapping("/{courseId}")
    public Result<String> delete(@PathVariable Long courseId) {
        courseService.delete(courseId);
        return Result.success("删除成功");
    }
}
