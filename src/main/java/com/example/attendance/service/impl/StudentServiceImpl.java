package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    public static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Student createStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }
        Student saved = studentRepository.save(student);
        ensureStudentAccount(saved);
        return saved;
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

        List<Student> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = studentRepository.findAll();
        } else {
            list = studentRepository.findByStudentIdContainingOrNameContainingOrClassNameContaining(
                    keyword.trim(),
                    keyword.trim(),
                    keyword.trim(),
                    org.springframework.data.domain.Sort.unsorted());
        }

        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.CHINA);
        collator.setStrength(java.text.Collator.PRIMARY);
        java.util.Comparator<Student> cmp;
        if ("name".equals(sortBy)) {
            cmp = (a, b) -> collator.compare(safe(a.getName()), safe(b.getName()));
        } else {
            cmp = (a, b) -> safe(a.getStudentId()).compareTo(safe(b.getStudentId()));
        }
        if ("desc".equalsIgnoreCase(direction)) {
            cmp = cmp.reversed();
        }
        list = new java.util.ArrayList<>(list);
        list.sort(cmp);
        return list;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
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
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentId = getCellValue(row.getCell(0));
                    String name = getCellValue(row.getCell(1));
                    String gender = getCellValue(row.getCell(2));
                    String className = getCellValue(row.getCell(6));

                    if (studentId.isEmpty() || name.isEmpty()) {
                        result.incrementFail("第" + (i + 1) + "行：学号或姓名为空");
                        continue;
                    }

                    Student student = new Student();
                    student.setStudentId(studentId);
                    student.setName(name);
                    student.setGender(gender);
                    student.setClassName(className.isEmpty() ? "默认班级" : className);
                    student.setAge(18);

                    studentRepository.save(student);
                    ensureStudentAccount(student);
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

    private void ensureStudentAccount(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            return;
        }
        String username = student.getStudentId().trim();
        if (userDao.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRealName(student.getName() == null ? username : student.getName());
        user.setRole("STUDENT");
        user.setMustChangePassword(true);
        userDao.insertUser(user);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long)cell.getNumericCellValue());
            default: return "";
        }
    }
}
