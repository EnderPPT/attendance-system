package com.example.attendance.service;

import com.example.attendance.entity.Course;

import java.util.List;

public interface CourseService {
    Course create(Course course);
    Course update(Long courseId, Course course);
    Course getById(Long courseId);
    List<Course> getAll();
    List<Course> getByTeacherId(Long teacherId);
    List<Course> getByClassName(String className);
    void delete(Long courseId);
}
