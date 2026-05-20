package com.example.attendance.service;

import com.example.attendance.entity.Student;

import java.util.List;

public interface StudentService {
    Student createStudent(Student student);

    Student getById(String studentId);

    List<Student> getAll();

    List<Student> getByClassName(String className);

    void deleteById(String studentId);

    List<Student> search(String keyword, String sortBy, String direction);

    void deleteBatch(List<String> studentIds);
}
