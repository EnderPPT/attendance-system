package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student createStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }
        return studentRepository.save(student);
    }

    @Override
    public Student getById(String studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    @Override
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    @Override
    public List<Student> getByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    @Override
    public void deleteById(String studentId) {
        studentRepository.deleteById(studentId);
    }

    @Override
    public List<Student> search(String keyword, String sortBy, String direction) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "studentId";
        }

        if (!"studentId".equals(sortBy) && !"name".equals(sortBy)) {
            sortBy = "studentId";
        }

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll(sort);
        }

        return studentRepository.findByStudentIdContainingOrNameContainingOrClassNameContaining(
                keyword.trim(),
                keyword.trim(),
                keyword.trim(),
                sort);
    }

    @Override
    public void deleteBatch(List<String> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        studentRepository.deleteAllById(studentIds);
    }
}