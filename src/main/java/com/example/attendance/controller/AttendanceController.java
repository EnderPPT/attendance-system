package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.common.Result;
import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import org.springframework.data.domain.Page;
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

    @GetMapping("/page")
    public Result<Page<Attendance>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "checkInTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return Result.success(attendanceService.getPage(page, size, sortBy, direction));
    }

    @PostMapping("/search")
    public Result<Page<Attendance>> seach(@RequestBody AttendanceQueryDTO queryDTO) {
        return Result.success(attendanceService.queryByCondition(queryDTO));
    }
}
