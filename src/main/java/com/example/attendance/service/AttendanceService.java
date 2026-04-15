package com.example.attendance.service;

import com.example.attendance.entity.Attendance;

import java.util.List;

public interface AttendanceService {

    Attendance create(Attendance attendance);

    List<Attendance> getAll();

    List<Attendance> getByStudentId(Long studentId);

    List<Attendance> getByCourseId(Long courseId);
}