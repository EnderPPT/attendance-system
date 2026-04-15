package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/create")
    public Result<Attendance> create(@RequestBody Attendance attendance) {
        return Result.success(attendanceService.create(attendance));
    }

    @GetMapping("/all")
    public Result<List<Attendance>> getAll() {
        return Result.success(attendanceService.getAll());
    }

    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getByStudenId(@PathVariable Long studentId) {
        return Result.success(attendanceService.getByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    public Result<List<Attendance>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(attendanceService.getByCourseId(courseId));
    }
}
