package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;
import org.springframework.data.domain.Page;

import java.io.File;
import java.util.List;

public interface AttendanceService {

    Attendance create(Attendance attendance);

    List<Attendance> getAll();

    List<Attendance> getByStudentId(Long studentId);

    List<Attendance> getByCourseId(Long courseId);

    Page<Attendance> getPage(int page, int size, String soryBy, String direction);

    Page<Attendance> queryByCondition(AttendanceQueryDTO QueryDTO);

    ImportResult importFromExcel(File file);

    StatisticsDTO getStudentStatistics(Long studentId);
}