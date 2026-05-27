package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.dto.AttendanceQueryDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Attendance create(Attendance attendance) {
        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(new Timestamp(System.currentTimeMillis()));
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
        int pageNum = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "checkInTime";
        }

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> queryByCondition(AttendanceQueryDTO queryDTO) {
        Specification<Attendance> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDTO.getStudentId() != null) {
                predicates.add(cb.equal(root.get("studentId"), queryDTO.getStudentId()));
            }

            if (queryDTO.getCourseId() != null) {
                predicates.add(cb.equal(root.get("courseId"), queryDTO.getCourseId()));
            }

            if (queryDTO.getStatus() != null && !queryDTO.getStatus().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }

            if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().trim().isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("checkInTime"),
                        java.sql.Timestamp.valueOf(queryDTO.getStartTime())
                ));
            }

            if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().trim().isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("checkInTime"),
                        java.sql.Timestamp.valueOf(queryDTO.getEndTime())
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        String sortBy = queryDTO.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "checkInTime";
        }

        Sort sort = "asc".equalsIgnoreCase(queryDTO.getDirection())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        int pageNum = queryDTO.getPage() == null || queryDTO.getPage() < 1 ? 0 : queryDTO.getPage() - 1;
        int pageSize = queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 5 : queryDTO.getSize();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

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

                    if(studentIdStr.isEmpty() || courseIdStr.isEmpty()) {
                        result.incrementFail("第" + (i+1) + "行：学号或课程号为空");
                        continue;
                    }

                    Attendance attendance = new Attendance();
                    // 如果 Excel 中的是纯数字，转为 Long
                    attendance.setStudentId(Double.valueOf(studentIdStr).longValue());
                    attendance.setCourseId(Double.valueOf(courseIdStr).longValue());
                    attendance.setStatus(getCellValue(row.getCell(3)));
                    attendance.setIp("127.0.0.1");

                    // 安全的日期处理
                    Cell timeCell = row.getCell(2);
                    if (timeCell != null && DateUtil.isCellDateFormatted(timeCell)) {
                        attendance.setCheckInTime(new java.sql.Timestamp(timeCell.getDateCellValue().getTime()));
                    } else {
                        // 若是文本格式的 yyyy-MM-dd HH:mm:ss
                        attendance.setCheckInTime(java.sql.Timestamp.valueOf(getCellValue(timeCell)));
                    }
                    attendance.setSeatRow(1);
                    attendance.setSeatCol(1);
                    attendance.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

                    attendanceRepository.save(attendance);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.incrementFail("第" + (i+1) + "行数据异常：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取Excel失败", e);
        }
        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: return "";
        }
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

        if (total == 0) {
            dto.setAttendanceRate("0.00%");
        } else {
            // (正常数 + 迟到数算作部分出勤等，这里按 正常数/总数 来计算严格出勤率)
            double rate = (double) normal / total * 100;
            dto.setAttendanceRate(String.format("%.2f%%", rate));
        }
        return dto;
    }
}