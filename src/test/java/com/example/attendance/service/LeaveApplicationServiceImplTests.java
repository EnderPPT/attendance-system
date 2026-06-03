package com.example.attendance.service;

import com.example.attendance.dto.LeaveApplicationRequest;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.LeaveApplicationRepository;
import com.example.attendance.service.impl.LeaveApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServiceImplTests {
    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;
    @Mock
    private CourseRepository courseRepository;

    private LeaveApplicationServiceImpl leaveApplicationService;

    @BeforeEach
    void setUp() {
        leaveApplicationService = new LeaveApplicationServiceImpl(leaveApplicationRepository, courseRepository);
    }

    @Test
    void applyRejectsLeaveLongerThanThreeDays() {
        LeaveApplicationRequest request = validRequest();
        request.setEndTime(request.getStartTime().plusDays(3).plusMinutes(1));

        BusinessException exception = assertThrows(BusinessException.class, () -> leaveApplicationService.apply(request));

        assertEquals("请假时间不能超过 3 天", exception.getMessage());
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void applyCreatesPendingApplication() {
        LeaveApplicationRequest request = validRequest();
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(leaveApplicationRepository.save(any(LeaveApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveApplication result = leaveApplicationService.apply(request);

        assertEquals(LeaveApplicationServiceImpl.PENDING, result.getStatus());
        assertNotNull(result.getApplyTime());
        assertEquals("参加比赛", result.getReason());
    }

    @Test
    void approveRejectsCompletedApplication() {
        LeaveApplication application = new LeaveApplication();
        application.setId(1L);
        application.setStatus(LeaveApplicationServiceImpl.APPROVED);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> leaveApplicationService.approve(1L, true, null));

        assertEquals("该请假申请已完成审批", exception.getMessage());
        verify(leaveApplicationRepository, never()).save(any());
    }

    private LeaveApplicationRequest validRequest() {
        LeaveApplicationRequest request = new LeaveApplicationRequest();
        request.setStudentId(3L);
        request.setCourseId(1L);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        request.setReason("参加比赛");
        return request;
    }
}
