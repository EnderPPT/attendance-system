package com.example.attendance.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int successCount = 0;
    private int failCount = 0;
    private List<String> failReports = new ArrayList<>();

    public void incrementSuccess() { this.successCount++; }
    public void incrementFail(String reason) {
        this.failCount++;
        this.failReports.add(reason);
    }
}