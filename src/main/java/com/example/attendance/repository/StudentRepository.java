package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByClassName(String className);

    List<Student> findByNameContaining(String keyword);

    List<Student> findByStudentIdContainingOrNameContainingOrClassNameContaining(
            String studentId,
            String name,
            String className,
            Sort sort);
}
