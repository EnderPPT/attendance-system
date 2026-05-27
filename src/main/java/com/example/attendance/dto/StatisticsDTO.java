package com.example.attendance.dto;
import lombok.Data;

@Data
public class StatisticsDTO {
    private long totalCount;
    private long normalCount;
    private long lateCount;
    private long absentCount;
    private String attendanceRate;
}