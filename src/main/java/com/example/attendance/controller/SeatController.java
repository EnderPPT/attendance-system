package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seat")
public class SeatController {
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;

    public SeatController(CourseRepository courseRepository, AttendanceRepository attendanceRepository) {
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/layout/{courseId}")
    public Result<Map<String, Object>> getLayout(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        LocalDate today = LocalDate.now();
        Timestamp dayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp nextDayStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        List<Attendance> todayList = attendanceRepository
                .findByCourseIdAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThan(courseId, dayStart, nextDayStart);

        List<int[]> occupied = new ArrayList<>();
        for (Attendance a : todayList) {
            if (a.getSeatRow() != null && a.getSeatCol() != null
                    && a.getSeatRow() > 0 && a.getSeatCol() > 0) {
                occupied.add(new int[]{a.getSeatRow(), a.getSeatCol()});
            }
        }

        List<int[]> excluded = parseExcludeSeats(course.getExcludeSeats());

        Map<String, Object> data = new HashMap<>();
        data.put("courseId", course.getCourseId());
        data.put("courseName", course.getCourseName());
        data.put("classroomName", course.getClassroomName());
        data.put("rows", course.getLayoutRows());
        data.put("cols", course.getLayoutCols());
        data.put("excluded", excluded);
        data.put("occupied", occupied);
        return Result.success(data);
    }

    private List<int[]> parseExcludeSeats(String text) {
        List<int[]> list = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return list;
        }
        for (String token : text.split(";")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            String[] rc = trimmed.split(",");
            if (rc.length != 2) continue;
            try {
                list.add(new int[]{Integer.parseInt(rc[0].trim()), Integer.parseInt(rc[1].trim())});
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }
}
