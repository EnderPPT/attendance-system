package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTests {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseSelectionRepository courseSelectionRepository;

    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl(attendanceRepository, courseRepository, courseSelectionRepository);
        lenient().when(courseSelectionRepository.existsByCourseIdAndStudentId(any(), any())).thenReturn(true);
    }

    @Test
    void createRejectsSeatOutsideClassroom() {
        Attendance attendance = validAttendance();
        attendance.setSeatRow(9);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse()));

        BusinessException exception = assertThrows(BusinessException.class, () -> attendanceService.create(attendance));

        assertEquals("座位不在该课程教室的有效范围内", exception.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateDailyCheckin() {
        Attendance attendance = validAttendance();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse()));
        when(attendanceRepository.existsByCourseIdAndStudentIdAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThan(
                eq(1L), eq(3L), any(Timestamp.class), any(Timestamp.class))).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> attendanceService.create(attendance));

        assertEquals("该学生当天已完成本课程打卡，请勿重复提交", exception.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void createNormalizesStatusAndSetsCreateTime() {
        Attendance attendance = validAttendance();
        attendance.setStatus(" late ");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse()));
        when(attendanceRepository.save(attendance)).thenReturn(attendance);

        Attendance result = attendanceService.create(attendance);

        assertEquals("LATE", result.getStatus());
        assertNotNull(result.getCreateTime());
        verify(attendanceRepository).save(attendance);
    }

    private Attendance validAttendance() {
        Attendance attendance = new Attendance();
        attendance.setCourseId(1L);
        attendance.setStudentId(3L);
        attendance.setSeatRow(2);
        attendance.setSeatCol(3);
        attendance.setCheckInTime(Timestamp.valueOf(LocalDateTime.of(2026, 6, 3, 8, 20)));
        attendance.setStatus("NORMAL");
        return attendance;
    }

    private Course validCourse() {
        Course course = new Course();
        course.setCourseId(1L);
        course.setLayoutRows(8);
        course.setLayoutCols(10);
        course.setExcludeSeats("1,1;1,2");
        return course;
    }
}
