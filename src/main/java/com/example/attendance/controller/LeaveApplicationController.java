package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.service.LeaveApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
public class LeaveApplicationController {
    private final LeaveApplicationService leaveApplicationService;

    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @PostMapping("/apply")
    public Result<LeaveApplication> apply(@RequestBody LeaveApplicationRequest request) {
        return Result.success(leaveApplicationService.apply(request));
    }

    @PostMapping("/approve/{id}")
    public Result<LeaveApplication> approve(@PathVariable Long id,
                                            @RequestParam boolean approved,
                                            @RequestParam(required = false) String remark) {
        return Result.success(leaveApplicationService.approve(id, approved, remark));
    }

    @GetMapping("/{id}")
    public Result<LeaveApplication> getById(@PathVariable Long id) {
        return Result.success(leaveApplicationService.getById(id));
    }

    @GetMapping
    public Result<List<LeaveApplication>> getAll() {
        return Result.success(leaveApplicationService.getAll());
    }

    @GetMapping("/student/{studentId}")
    public Result<List<LeaveApplication>> getByStudentId(@PathVariable Long studentId) {
        return Result.success(leaveApplicationService.getByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    public Result<List<LeaveApplication>> getByCourseId(@PathVariable Long courseId) {
        return Result.success(leaveApplicationService.getByCourseId(courseId));
    }

    @GetMapping("/status/{status}")
    public Result<List<LeaveApplication>> getByStatus(@PathVariable String status) {
        return Result.success(leaveApplicationService.getByStatus(status));
    }
}
