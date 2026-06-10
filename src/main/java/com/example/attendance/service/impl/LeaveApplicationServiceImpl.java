package com.example.attendance.service.impl;

import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.LeaveApplicationRepository;
import com.example.attendance.service.LeaveApplicationService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;

    public LeaveApplicationServiceImpl(LeaveApplicationRepository leaveApplicationRepository,
                                       CourseRepository courseRepository,
                                       AttendanceRepository attendanceRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public LeaveApplication apply(LeaveApplicationRequest request) {
        validateRequest(request);
        if (!courseRepository.existsById(request.getCourseId())) {
            throw new BusinessException("课程不存在");
        }

        LeaveApplication application = new LeaveApplication();
        application.setStudentId(request.getStudentId());
        application.setCourseId(request.getCourseId());
        application.setStartTime(request.getStartTime());
        application.setEndTime(request.getEndTime());
        application.setReason(request.getReason().trim());
        application.setStatus(PENDING);
        application.setApplyTime(LocalDateTime.now());
        return leaveApplicationRepository.save(application);
    }

    @Override
    public LeaveApplication approve(Long id, boolean approved, String remark) {
        LeaveApplication application = getById(id);
        if (!PENDING.equals(application.getStatus())) {
            throw new BusinessException("该请假申请已完成审批");
        }
        application.setStatus(approved ? APPROVED : REJECTED);
        application.setApprovalTime(LocalDateTime.now());
        application.setApproverRemark(remark == null ? null : remark.trim());
        LeaveApplication saved = leaveApplicationRepository.save(application);
        if (approved) {
            writeLeaveAttendance(saved);
        }
        return saved;
    }

    private void writeLeaveAttendance(LeaveApplication application) {
        LocalDate start = application.getStartTime().toLocalDate();
        LocalDate end = application.getEndTime().toLocalDate();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            Timestamp dayStart = Timestamp.valueOf(day.atStartOfDay());
            Timestamp nextDayStart = Timestamp.valueOf(day.plusDays(1).atStartOfDay());
            boolean exists = attendanceRepository
                    .existsByCourseIdAndStudentIdAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThan(
                            application.getCourseId(), application.getStudentId(), dayStart, nextDayStart);
            if (exists) {
                continue;
            }
            Attendance attendance = new Attendance();
            attendance.setCourseId(application.getCourseId());
            attendance.setStudentId(application.getStudentId());
            attendance.setCheckInTime(Timestamp.valueOf(day.atTime(0, 0)));
            attendance.setSeatRow(-1);
            int seatCol = -(int) Math.floorMod(application.getStudentId(), 30000);
            if (seatCol == 0) {
                seatCol = -1;
            }
            attendance.setSeatCol(seatCol);
            attendance.setStatus("LEAVE");
            attendance.setIp("LEAVE-APPROVED");
            attendance.setCreateTime(new Timestamp(System.currentTimeMillis()));
            attendanceRepository.save(attendance);
        }
    }

    @Override
    public LeaveApplication getById(Long id) {
        return leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("请假申请不存在"));
    }

    @Override
    public List<LeaveApplication> getAll() {
        return leaveApplicationRepository.findAll();
    }

    @Override
    public List<LeaveApplication> getByStudentId(Long studentId) {
        return leaveApplicationRepository.findByStudentIdOrderByApplyTimeDesc(studentId);
    }

    @Override
    public List<LeaveApplication> getByCourseId(Long courseId) {
        return leaveApplicationRepository.findByCourseIdOrderByApplyTimeDesc(courseId);
    }

    @Override
    public List<LeaveApplication> getByStatus(String status) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if (!PENDING.equals(normalizedStatus) && !APPROVED.equals(normalizedStatus) && !REJECTED.equals(normalizedStatus)) {
            throw new BusinessException("请假状态必须为 PENDING、APPROVED 或 REJECTED");
        }
        return leaveApplicationRepository.findByStatusOrderByApplyTimeDesc(normalizedStatus);
    }

    private void validateRequest(LeaveApplicationRequest request) {
        if (request == null || request.getStudentId() == null || request.getCourseId() == null) {
            throw new BusinessException("学生 ID 和课程 ID 不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException("请假开始时间和结束时间不能为空");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能申请过去时间的请假");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("请假结束时间必须晚于开始时间");
        }
        if (Duration.between(request.getStartTime(), request.getEndTime()).compareTo(Duration.ofDays(3)) > 0) {
            throw new BusinessException("请假时间不能超过 3 天");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BusinessException("请假原因不能为空");
        }
    }
}
