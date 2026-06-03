package com.example.attendance.service;

import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.LeaveApplication;

import java.util.List;

public interface LeaveApplicationService {
    LeaveApplication apply(LeaveApplicationRequest request);
    LeaveApplication approve(Long id, boolean approved, String remark);
    LeaveApplication getById(Long id);
    List<LeaveApplication> getAll();
    List<LeaveApplication> getByStudentId(Long studentId);
    List<LeaveApplication> getByCourseId(Long courseId);
    List<LeaveApplication> getByStatus(String status);
}
