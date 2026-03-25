package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Student student) {
        String sql = "INSERT INTO student (student_id, name, class_name) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, student.getStudentId(), student.getName(), student.getClassName());
    }
}
