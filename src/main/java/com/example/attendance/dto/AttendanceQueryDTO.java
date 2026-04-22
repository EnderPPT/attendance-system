package com.example.attendance.dto;

import lombok.Data;

@Data
public class AttendanceQueryDTO {
    private Long studentId;
    private Long courseId;
    private String status;
    private String startTime;
    private String endTime;
    private Integer page = 1;
    private Integer size = 5;
    private String sortBy = "checkInTime";
    private String direction = "desc";
}
