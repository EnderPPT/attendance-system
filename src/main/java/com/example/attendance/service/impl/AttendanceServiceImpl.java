package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.dto.AttendanceQueryDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Attendance create(Attendance attendance) {
        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(new Timestamp(System.currentTimeMillis()));
        }
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getAll() {
        return attendanceRepository.findAll();
    }

    @Override
    public List<Attendance> getByStudentId(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> getByCourseId(Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    @Override
    public Page<Attendance> getPage(int page, int size, String sortBy, String direction) {
        int pageNum = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "checkInTime";
        }

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> queryByCondition(AttendanceQueryDTO queryDTO) {
        Specification<Attendance> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDTO.getStudentId() != null) {
                predicates.add(cb.equal(root.get("studentId"), queryDTO.getStudentId()));
            }

            if (queryDTO.getCourseId() != null) {
                predicates.add(cb.equal(root.get("courseId"), queryDTO.getCourseId()));
            }

            if (queryDTO.getStatus() != null && !queryDTO.getStatus().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }

            if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().trim().isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("checkInTime"),
                        java.sql.Timestamp.valueOf(queryDTO.getStartTime())
                ));
            }

            if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().trim().isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("checkInTime"),
                        java.sql.Timestamp.valueOf(queryDTO.getEndTime())
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        String sortBy = queryDTO.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "checkInTime";
        }

        Sort sort = "asc".equalsIgnoreCase(queryDTO.getDirection())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        int pageNum = queryDTO.getPage() == null || queryDTO.getPage() < 1 ? 0 : queryDTO.getPage() - 1;
        int pageSize = queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 5 : queryDTO.getSize();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        return attendanceRepository.findAll(specification, pageable);
    }
}