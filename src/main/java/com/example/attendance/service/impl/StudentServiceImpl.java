package com.example.attendance.service.impl;

import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import com.example.attendance.dto.ImportResult;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.File;
import java.io.File;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student createStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }
        return studentRepository.save(student);
    }

    @Override
    public Student getById(String studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    @Override
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    @Override
    public List<Student> getByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    @Override
    public void deleteById(String studentId) {
        studentRepository.deleteById(studentId);
    }

    @Override
    public List<Student> search(String keyword, String sortBy, String direction) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "studentId";
        }

        if (!"studentId".equals(sortBy) && !"name".equals(sortBy)) {
            sortBy = "studentId";
        }

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll(sort);
        }

        return studentRepository.findByStudentIdContainingOrNameContainingOrClassNameContaining(
                keyword.trim(),
                keyword.trim(),
                keyword.trim(),
                sort);
    }

    @Override
    public void deleteBatch(List<String> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        studentRepository.deleteAllById(studentIds);
    }

    @Override
    public ImportResult importStudentsFromExcel(File file) {
        ImportResult result = new ImportResult();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            // 跳过第一行表头，从第二行开始读
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 根据你发的图片，提取指定列
                    String studentId = getCellValue(row.getCell(0)); // 学号
                    String name = getCellValue(row.getCell(1));      // 姓名
                    String gender = getCellValue(row.getCell(2));    // 性别
                    String className = getCellValue(row.getCell(6)); // 班级 (图片中的第7列，索引为6)

                    if (studentId.isEmpty() || name.isEmpty()) {
                        result.incrementFail("第" + (i + 1) + "行：学号或姓名为空");
                        continue;
                    }

                    // 创建并填充学生对象
                    Student student = new Student();
                    student.setStudentId(studentId);
                    student.setName(name);
                    student.setGender(gender);
                    // 默认没填的部分给个初始值防止报错
                    student.setClassName(className.isEmpty() ? "默认班级" : className);
                    student.setAge(18); // 默认年龄

                    studentRepository.save(student);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.incrementFail("第" + (i + 1) + "行异常：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取学生Excel失败", e);
        }
        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                // 避免学号等数字带小数点，如 42211127.0
                return String.valueOf((long)cell.getNumericCellValue());
            default: return "";
        }
    }
}