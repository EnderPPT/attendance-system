package com.example.attendance.service.impl;

import com.example.attendance.entity.User;
import com.example.attendance.dao.UserDao;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void addUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (userDao.existsByUsername(user.getUsername().trim())) {
            throw new BusinessException("用户名已存在");
        }
        if (!isEncoded(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STUDENT");
        }
        userDao.insertUser(user);
    }

    @Override
    public User findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> findAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public void updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new BusinessException("用户 ID 不能为空");
        }
        User existing = userDao.findById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existing.getPassword());
        } else if (!isEncoded(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            user.setRealName(existing.getRealName());
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole(existing.getRole());
        }
        if (user.getMustChangePassword() == null) {
            user.setMustChangePassword(existing.getMustChangePassword());
        }
        userDao.updateUser(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    @Override
    public User findByUsernameOrNull(String username) {
        return userDao.findByUsernameOrNull(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    private boolean isEncoded(String password) {
        return password != null && password.startsWith("$2");
    }
}
