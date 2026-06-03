package com.example.attendance.service.impl;

import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.CourseService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course create(Course course) {
        validate(course);
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new BusinessException("课程代码已存在");
        }
        if (course.getCreateTime() == null) {
            course.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        return courseRepository.save(course);
    }

    @Override
    public Course update(Long courseId, Course course) {
        Course existing = getById(courseId);
        validate(course);
        if (!existing.getCourseCode().equals(course.getCourseCode())
                && courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new BusinessException("课程代码已存在");
        }
        course.setCourseId(courseId);
        course.setCreateTime(existing.getCreateTime());
        return courseRepository.save(course);
    }

    @Override
    public Course getById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));
    }

    @Override
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<Course> getByClassName(String className) {
        return courseRepository.findByClassName(className);
    }

    @Override
    public void delete(Long courseId) {
        getById(courseId);
        courseRepository.deleteById(courseId);
    }

    private void validate(Course course) {
        if (course == null) {
            throw new BusinessException("课程信息不能为空");
        }
        if (isBlank(course.getCourseCode()) || isBlank(course.getCourseName())
                || isBlank(course.getClassName()) || isBlank(course.getClassroomName())) {
            throw new BusinessException("课程代码、课程名称、班级和教室不能为空");
        }
        if (course.getTeacherId() == null) {
            throw new BusinessException("授课教师不能为空");
        }
        if (course.getLayoutRows() == null || course.getLayoutRows() < 1
                || course.getLayoutCols() == null || course.getLayoutCols() < 1) {
            throw new BusinessException("教室座位行列数必须大于 0");
        }
        if (course.getWeekday() != null && (course.getWeekday() < 1 || course.getWeekday() > 7)) {
            throw new BusinessException("星期必须在 1 到 7 之间");
        }
        if (course.getStartWeek() != null && course.getEndWeek() != null
                && course.getStartWeek() > course.getEndWeek()) {
            throw new BusinessException("开始周不能晚于结束周");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
