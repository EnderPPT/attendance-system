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

    private static final String SELECT_COLUMNS =
            "id, username, password, real_name as realName, role, " +
            "must_change_password as mustChangePassword, create_time as createTime";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertUser(User user) {
        String sql = "INSERT INTO sys_user (username, password, real_name, role, must_change_password) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getMustChangePassword() != null && user.getMustChangePassword());
    }

    public User findById(Long id) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
    }

    public User findByUsername(String username) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
    }

    public List<User> findAllTeachers() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    public void updateUser(User user) {
        String sql = "UPDATE sys_user SET password = ?, real_name = ?, role = ?, must_change_password = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getMustChangePassword() != null && user.getMustChangePassword(),
                user.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM sys_user WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public User findByUsernameOrNull(String username) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE username = ?";
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

    public List<User> findStudentsByUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String inClause = String.join(",", java.util.Collections.nCopies(usernames.size(), "?"));
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE role = 'STUDENT' AND username IN (" + inClause + ")";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), usernames.toArray());
    }

    public List<User> findStudentsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String inClause = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sys_user WHERE id IN (" + inClause + ")";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), ids.toArray());
    }
}
