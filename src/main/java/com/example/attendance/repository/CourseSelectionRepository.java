package com.example.attendance.repository;

import com.example.attendance.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    List<CourseSelection> findByStudentId(Long studentId);

    List<CourseSelection> findByCourseId(Long courseId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    void deleteByCourseIdAndStudentId(Long courseId, Long studentId);
}
