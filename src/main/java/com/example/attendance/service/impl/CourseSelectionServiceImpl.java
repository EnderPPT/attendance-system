package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.CourseSelection;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.service.CourseSelectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseRepository courseRepository;
    private final UserDao userDao;

    public CourseSelectionServiceImpl(CourseSelectionRepository courseSelectionRepository,
                                      CourseRepository courseRepository,
                                      UserDao userDao) {
        this.courseSelectionRepository = courseSelectionRepository;
        this.courseRepository = courseRepository;
        this.userDao = userDao;
    }

    @Override
    public CourseSelection select(Long courseId, Long studentId) {
        if (courseId == null || studentId == null) {
            throw new BusinessException("课程 ID 和学生 ID 不能为空");
        }
        if (!courseRepository.existsById(courseId)) {
            throw new BusinessException("课程不存在");
        }
        User student = findUser(studentId);
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new BusinessException("仅学生账号可被选课");
        }
        if (courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("该学生已选该课程");
        }
        CourseSelection selection = new CourseSelection();
        selection.setCourseId(courseId);
        selection.setStudentId(studentId);
        selection.setSelectTime(new Timestamp(System.currentTimeMillis()));
        return courseSelectionRepository.save(selection);
    }

    @Override
    public CourseSelection selectByUsername(Long courseId, String studentNumber) {
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new BusinessException("学号不能为空");
        }
        User user = userDao.findByUsernameOrNull(studentNumber.trim());
        if (user == null) {
            throw new BusinessException("学号 " + studentNumber.trim() + " 对应的账号不存在");
        }
        return select(courseId, user.getId());
    }

    @Override
    public ImportResult batchSelectByUsernames(Long courseId, List<String> studentNumbers) {
        ImportResult result = new ImportResult();
        if (courseId == null) {
            throw new BusinessException("课程 ID 不能为空");
        }
        if (!courseRepository.existsById(courseId)) {
            throw new BusinessException("课程不存在");
        }
        if (studentNumbers == null || studentNumbers.isEmpty()) {
            return result;
        }

        Set<String> distinct = new LinkedHashSet<>();
        for (String raw : studentNumbers) {
            if (raw == null) continue;
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                distinct.add(trimmed);
            }
        }

        for (String studentNumber : distinct) {
            try {
                selectByUsername(courseId, studentNumber);
                result.incrementSuccess();
            } catch (Exception e) {
                result.incrementFail(studentNumber + "：" + e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void unselect(Long courseId, Long studentId) {
        if (!courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("该选课记录不存在");
        }
        courseSelectionRepository.deleteByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    public boolean isSelected(Long courseId, Long studentId) {
        return courseId != null && studentId != null
                && courseSelectionRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    public List<Course> getCoursesByStudent(Long studentId) {
        List<CourseSelection> selections = courseSelectionRepository.findByStudentId(studentId);
        List<Course> courses = new ArrayList<>();
        for (CourseSelection selection : selections) {
            courseRepository.findById(selection.getCourseId()).ifPresent(courses::add);
        }
        return courses;
    }

    @Override
    public List<User> getStudentsByCourse(Long courseId) {
        List<CourseSelection> selections = courseSelectionRepository.findByCourseId(courseId);
        List<User> users = new ArrayList<>();
        for (CourseSelection selection : selections) {
            User user = userDao.findById(selection.getStudentId());
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    private User findUser(Long userId) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                throw new BusinessException("学生不存在");
            }
            return user;
        } catch (Exception e) {
            throw new BusinessException("学生不存在");
        }
    }
}
