package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.attendance.config.AuthorizationInterceptor;
import com.example.attendance.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/attendance/page")
public class AttendancePageController {
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/checkin")
    public String checkinPage(@RequestParam(required = false) Long courseId, Model model, HttpSession session) {
        addCheckinModel(model, courseId, session);
        return "attendance-form";
    }

    @PostMapping("/checkin")
    public String doCheckin(@RequestParam(required = false) Long studentId,
                            @RequestParam Long courseId,
                            @RequestParam Integer seatRow,
                            @RequestParam Integer seatCol,
                            Model model,
                            HttpSession session,
                            HttpServletRequest request) {
        try {
            User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
            Long effectiveStudentId = user != null && "STUDENT".equalsIgnoreCase(user.getRole()) ? user.getId() : studentId;

            Attendance attendance = new Attendance();
            attendance.setStudentId(effectiveStudentId);
            attendance.setCourseId(courseId);
            attendance.setSeatRow(seatRow);
            attendance.setSeatCol(seatCol);

            LocalDateTime now = LocalDateTime.now();
            attendance.setCheckInTime(Timestamp.valueOf(now));
            attendance.setCreateTime(Timestamp.valueOf(now));
            attendance.setIp(resolveClientIp(request));

            if (now.toLocalTime().isAfter(java.time.LocalTime.of(8, 30))) {
                attendance.setStatus("LATE");
            } else {
                attendance.setStatus("NORMAL");
            }

            attendanceService.create(attendance);

            model.addAttribute("successMsg", "打卡成功！当前状态为：" + (attendance.getStatus().equals("LATE") ? "迟到" : "正常"));
        } catch (Exception e) {
            model.addAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }
        addCheckinModel(model, courseId, session);
        return "attendance-form";
    }

    private void addCheckinModel(Model model, Long courseId, HttpSession session) {
        User user = (User) session.getAttribute(AuthorizationInterceptor.SESSION_USER);
        boolean student = user != null && "STUDENT".equalsIgnoreCase(user.getRole());
        model.addAttribute("courses", courseService.getAll());
        model.addAttribute("courseId", courseId);
        model.addAttribute("isStudent", student);
        model.addAttribute("sessionUserId", student ? user.getId() : null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @GetMapping("/list")
    public String listPage(@RequestParam(required = false) Long courseId,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String startTime,
                           @RequestParam(required = false) String endTime,
                           @RequestParam(defaultValue = "1") Integer page,
                           Model model) {
        AttendanceQueryDTO query = new AttendanceQueryDTO();
        query.setCourseId(courseId);
        query.setStatus(status);
        query.setPage(page);

        if (startTime != null && !startTime.isEmpty()) {
            query.setStartTime(startTime.contains(" ") ? startTime : startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.isEmpty()) {
            query.setEndTime(endTime.contains(" ") ? endTime : endTime + " 23:59:59");
        }

        Page<Attendance> pageData = attendanceService.queryByCondition(query);

        model.addAttribute("pageData", pageData);
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("courses", courseService.getAll());

        return "attendance-list";
    }

    @Value("${file.upload.path}")
    private String uploadPath;

    @GetMapping("/import")
    public String importPage() {
        return "attendance-import";
    }

    @PostMapping("/import")
    public String importFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String originalFilename = file.getOriginalFilename();
        if (file.isEmpty() || originalFilename == null
                || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "无效文件！请上传 Excel 文件");
            return "redirect:/attendance/page/import";
        }
        java.io.File dest = null;
        try {
            java.nio.file.Path uploadDirectory = java.nio.file.Path.of(uploadPath);
            java.nio.file.Files.createDirectories(uploadDirectory);
            String suffix = originalFilename.toLowerCase().endsWith(".xlsx") ? ".xlsx" : ".xls";
            dest = java.nio.file.Files.createTempFile(uploadDirectory, "attendance-", suffix).toFile();
            file.transferTo(dest);

            ImportResult result = attendanceService.importFromExcel(dest);

            String msg = "成功: " + result.getSuccessCount() + " 条。失败: " + result.getFailCount() + " 条。";
            if(result.getFailCount() > 0) {
                msg += " 失败原因示例：" + String.join(" | ", result.getFailReports().subList(0, Math.min(3, result.getFailCount())));
            }
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "服务器解析异常: " + e.getMessage());
        } finally {
            if (dest != null && dest.exists() && !dest.delete()) {
                dest.deleteOnExit();
            }
        }
        return "redirect:/attendance/page/import";
    }
}
