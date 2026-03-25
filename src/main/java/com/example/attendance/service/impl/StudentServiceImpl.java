package com.example.attendance.service.impl;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentDao studentDao;

    @Override
    public String createStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }
        studentDao.insert(student);
        return "学生创建成功";
    }
}