package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) {
        userService.addUser(user);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        User user = userService.findById(id);
        return Result.success(user);
    }

    @GetMapping("/username/{username}")
    public Result getByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return Result.success(user);
    }

    @GetMapping("/teachers")
    public Result getAllTeachers() {
        List<User> teachers = userService.findAllTeachers();
        return Result.success(teachers);
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
