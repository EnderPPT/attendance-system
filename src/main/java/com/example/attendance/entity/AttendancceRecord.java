package com.example.attendance.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendancceRecord {
    private String studentId;
    private String status;
    private String data;
}
