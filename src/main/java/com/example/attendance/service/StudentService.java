package com.example.attendance.service;

import com.example.attendance.entity.Student;

import java.util.List;

public interface StudentService {
    Student createStudent(Student student);

    Student getById(String studentId);

    List<Student> getAll();

    List<Student> getByClassName(String className);

    void deleteById(String studentId);
}
