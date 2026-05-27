package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByCourseId(Long courseId);

    List<Attendance> findByStudentIdAndStatus(Long studentId, String status);

    // 统计指定学生的总打卡数
    long countByStudentId(Long studentId);

    // 统计指定学生某状态的打卡数（NORMAL/LATE/ABSENT）
    long countByStudentIdAndStatus(Long studentId, String status);

    // 统计某课程当天的特定状态数（用于班级总体出勤）
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.courseId = :courseId AND a.status = :status AND a.checkInTime >= :start AND a.checkInTime <= :end")
    long countByCourseStatusAndDateRange(@org.springframework.data.repository.query.Param("courseId") Long courseId,
                                         @org.springframework.data.repository.query.Param("status") String status,
                                         @org.springframework.data.repository.query.Param("start") java.sql.Timestamp start,
                                         @org.springframework.data.repository.query.Param("end") java.sql.Timestamp end);
}