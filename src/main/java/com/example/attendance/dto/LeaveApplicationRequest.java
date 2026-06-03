package com.example.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveApplicationRequest {
    private Long studentId;
    private Long courseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
}
