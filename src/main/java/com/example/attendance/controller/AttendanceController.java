package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.AttendancceRecord;
import org.springframework.web.bind.annotation.*;

@RestController
public class AttendanceController {
    @PostMapping("/attendance/update")
    public Result<String> updateAttendance(@RequestBody AttendancceRecord record) {
        System.out.println("收到的JSON考勤数据：" + record);
        return Result.success("更新成功");
    }
}
