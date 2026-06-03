package com.example.attendance.repository;

import com.example.attendance.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByStudentIdOrderByApplyTimeDesc(Long studentId);
    List<LeaveApplication> findByCourseIdOrderByApplyTimeDesc(Long courseId);
    List<LeaveApplication> findByStatusOrderByApplyTimeDesc(String status);
}
