package com.example.attendance.service;

import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTests {
    @Mock
    private CourseRepository courseRepository;

    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl(courseRepository);
    }

    @Test
    void createRejectsDuplicateCourseCode() {
        Course course = validCourse();
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> courseService.create(course));

        assertEquals("课程代码已存在", exception.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createSetsCreateTimeAndSavesCourse() {
        Course course = validCourse();
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.save(course)).thenReturn(course);

        Course result = courseService.create(course);

        assertNotNull(result.getCreateTime());
        verify(courseRepository).save(course);
    }

    private Course validCourse() {
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseName("Java 后端开发");
        course.setClassName("计科1班");
        course.setTeacherId(2L);
        course.setClassroomName("第一教学楼-101");
        course.setLayoutRows(8);
        course.setLayoutCols(10);
        course.setWeekday(3);
        course.setStartWeek(1);
        course.setEndWeek(16);
        return course;
    }
}
