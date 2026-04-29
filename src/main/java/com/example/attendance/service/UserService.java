package com.example.attendance.service;

import com.example.attendance.entity.User;
import java.util.List;

public interface UserService {
    void addUser(User user);
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAllTeachers();
    void updateUser(User user);
    void deleteUser(Long id);
    User findByUsernameOrNull(String username);
    boolean existsByUsername(String username);
}
