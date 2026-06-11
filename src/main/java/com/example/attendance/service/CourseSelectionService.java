package com.example.attendance.service;

import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.CourseSelection;
import com.example.attendance.entity.User;

import java.util.List;

public interface CourseSelectionService {
    CourseSelection select(Long courseId, Long studentId);

    CourseSelection selectByUsername(Long courseId, String studentNumber);

    ImportResult batchSelectByUsernames(Long courseId, List<String> studentNumbers);

    void unselect(Long courseId, Long studentId);

    boolean isSelected(Long courseId, Long studentId);

    List<Course> getCoursesByStudent(Long studentId);

    List<User> getStudentsByCourse(Long courseId);
}
