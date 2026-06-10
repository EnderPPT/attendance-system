package com.example.attendance.service.impl;

import com.example.attendance.dto.AttendanceQueryDTO;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    private static final Set<String> ALLOWED_STATUSES = Set.of("NORMAL", "LATE", "EARLY", "ABSENT", "LEAVE");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "courseId", "studentId", "checkInTime", "status", "createTime");
    private static final int MAX_PAGE_SIZE = 100;

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 CourseRepository courseRepository,
                                 CourseSelectionRepository courseSelectionRepository) {
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.courseSelectionRepository = courseSelectionRepository;
    }

    @Override
    public Attendance create(Attendance attendance) {
        if (attendance == null || attendance.getStudentId() == null || attendance.getCourseId() == null) {
            throw new BusinessException("学生 ID 和课程 ID 不能为空");
        }
        if (attendance.getSeatRow() == null || attendance.getSeatCol() == null) {
            throw new BusinessException("座位行列不能为空");
        }

        Course course = courseRepository.findById(attendance.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));
        if (!courseSelectionRepository.existsByCourseIdAndStudentId(attendance.getCourseId(), attendance.getStudentId())) {
            throw new BusinessException("该学生未选该课程，无法打卡");
        }
        validateSeat(course, attendance.getSeatRow(), attendance.getSeatCol());

        Timestamp checkInTime = attendance.getCheckInTime();
        if (checkInTime == null) {
            checkInTime = new Timestamp(System.currentTimeMillis());
            attendance.setCheckInTime(checkInTime);
        }
        if (attendance.getStatus() == null || attendance.getStatus().trim().isEmpty()) {
            attendance.setStatus(resolveStatus(course, checkInTime));
        }
        normalizeStatus(attendance);

        LocalDate attendanceDate = checkInTime.toLocalDateTime().toLocalDate();
        Timestamp dayStart = Timestamp.valueOf(attendanceDate.atStartOfDay());
        Timestamp nextDayStart = Timestamp.valueOf(attendanceDate.plusDays(1).atStartOfDay());
        if (attendanceRepository.existsByCourseIdAndStudentIdAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThan(
                attendance.getCourseId(), attendance.getStudentId(), dayStart, nextDayStart)) {
            throw new BusinessException("该学生当天已完成本课程打卡，请勿重复提交");
        }
        if (attendanceRepository.existsByCourseIdAndSeatRowAndSeatColAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThan(
                attendance.getCourseId(), attendance.getSeatRow(), attendance.getSeatCol(), dayStart, nextDayStart)) {
            throw new BusinessException("该座位当天已被本课程其他学生使用");
        }

        if (attendance.getCreateTime() == null) {
            attendance.setCreateTime(new Timestamp(System.currentTimeMillis()));
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
        Pageable pageable = createPageable(page, size, sortBy, direction);
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> queryByCondition(AttendanceQueryDTO queryDTO) {
        AttendanceQueryDTO safeQuery = queryDTO == null ? new AttendanceQueryDTO() : queryDTO;
        Specification<Attendance> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (safeQuery.getStudentId() != null) {
                predicates.add(cb.equal(root.get("studentId"), safeQuery.getStudentId()));
            }
            if (safeQuery.getCourseId() != null) {
                predicates.add(cb.equal(root.get("courseId"), safeQuery.getCourseId()));
            }
            if (safeQuery.getStatus() != null && !safeQuery.getStatus().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), safeQuery.getStatus().trim().toUpperCase()));
            }
            if (safeQuery.getStartTime() != null && !safeQuery.getStartTime().trim().isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), parseTimestamp(safeQuery.getStartTime(), "开始时间")));
            }
            if (safeQuery.getEndTime() != null && !safeQuery.getEndTime().trim().isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), parseTimestamp(safeQuery.getEndTime(), "结束时间")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        int page = safeQuery.getPage() == null ? 1 : safeQuery.getPage();
        int size = safeQuery.getSize() == null ? 5 : safeQuery.getSize();
        Pageable pageable = createPageable(page, size, safeQuery.getSortBy(), safeQuery.getDirection());
        return attendanceRepository.findAll(specification, pageable);
    }

    @Override
    public ImportResult importFromExcel(File file) {
        ImportResult result = new ImportResult();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String studentIdStr = getCellValue(row.getCell(0));
                    String courseIdStr = getCellValue(row.getCell(1));
                    if (studentIdStr.isEmpty() || courseIdStr.isEmpty()) {
                        result.incrementFail("第" + (i + 1) + "行：学号或课程号为空");
                        continue;
                    }

                    Attendance attendance = new Attendance();
                    attendance.setStudentId(Double.valueOf(studentIdStr).longValue());
                    attendance.setCourseId(Double.valueOf(courseIdStr).longValue());
                    attendance.setStatus(getCellValue(row.getCell(3)));
                    attendance.setSeatRow(Double.valueOf(getCellValue(row.getCell(4))).intValue());
                    attendance.setSeatCol(Double.valueOf(getCellValue(row.getCell(5))).intValue());
                    attendance.setIp("IMPORT");
                    Cell timeCell = row.getCell(2);
                    if (timeCell != null && DateUtil.isCellDateFormatted(timeCell)) {
                        attendance.setCheckInTime(new Timestamp(timeCell.getDateCellValue().getTime()));
                    } else {
                        attendance.setCheckInTime(Timestamp.valueOf(getCellValue(timeCell)));
                    }
                    create(attendance);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.incrementFail("第" + (i + 1) + "行数据异常：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("读取 Excel 失败：" + e.getMessage());
        }
        return result;
    }

    private Pageable createPageable(int page, int size, String sortBy, String direction) {
        int pageNum = Math.max(page - 1, 0);
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "checkInTime";
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        return PageRequest.of(pageNum, pageSize, sort);
    }

    private void validateSeat(Course course, int seatRow, int seatCol) {
        if (seatRow < 1 || seatCol < 1 || seatRow > course.getLayoutRows() || seatCol > course.getLayoutCols()) {
            throw new BusinessException("座位不在该课程教室的有效范围内");
        }
        String target = seatRow + "," + seatCol;
        if (course.getExcludeSeats() != null) {
            for (String excluded : course.getExcludeSeats().split(";")) {
                if (target.equals(excluded.trim())) {
                    throw new BusinessException("该座位不可使用");
                }
            }
        }
    }

    private void normalizeStatus(Attendance attendance) {
        String status = attendance.getStatus();
        if (status == null || status.trim().isEmpty()) {
            attendance.setStatus("NORMAL");
            return;
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BusinessException("考勤状态必须为 NORMAL、LATE、EARLY、ABSENT 或 LEAVE");
        }
        attendance.setStatus(normalized);
    }

    private String resolveStatus(Course course, Timestamp checkInTime) {
        LocalTime start = course.getStartTime();
        LocalTime end = course.getEndTime();
        if (start == null || end == null) {
            return "NORMAL";
        }
        int threshold = course.getLateThresholdMinutes() == null ? 15 : course.getLateThresholdMinutes();
        LocalTime lateLine = start.plusMinutes(threshold);
        LocalTime now = checkInTime.toLocalDateTime().toLocalTime();

        if (now.isBefore(start)) {
            return "NORMAL";
        }
        if (!now.isAfter(lateLine)) {
            return "NORMAL";
        }
        if (now.isBefore(end)) {
            return "LATE";
        }
        return "ABSENT";
    }

    private Timestamp parseTimestamp(String value, String fieldName) {
        try {
            return Timestamp.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(fieldName + "格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    @Override
    public StatisticsDTO getStudentStatistics(Long studentId) {
        StatisticsDTO dto = new StatisticsDTO();
        long total = attendanceRepository.countByStudentId(studentId);
        long normal = attendanceRepository.countByStudentIdAndStatus(studentId, "NORMAL");
        long late = attendanceRepository.countByStudentIdAndStatus(studentId, "LATE");
        long absent = attendanceRepository.countByStudentIdAndStatus(studentId, "ABSENT");
        dto.setTotalCount(total);
        dto.setNormalCount(normal);
        dto.setLateCount(late);
        dto.setAbsentCount(absent);
        dto.setAttendanceRate(total == 0 ? "0.00%" : String.format("%.2f%%", (double) normal / total * 100));
        return dto;
    }
}
