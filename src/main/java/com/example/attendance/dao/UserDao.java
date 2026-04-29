package com.example.attendance.dao;

import com.example.attendance.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertUser(User user) {
        String sql = "INSERT INTO sys_user (username, password, real_name, role) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole());
    }

    public User findById(Long id) {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM sys_user WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM sys_user WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
    }

    public List<User> findAllTeachers() {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM sys_user WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    public void updateUser(User user) {
        String sql = "UPDATE sys_user SET password = ?, real_name = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM sys_user WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public User findByUsernameOrNull(String username) {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM sys_user WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}
