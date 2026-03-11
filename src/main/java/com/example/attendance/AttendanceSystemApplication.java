package com.example.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AttendanceSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);}

    @GetMapping("/hello")
    public String hello() {
        return "欢迎来到班级考勤管理系统！";
    }

    @GetMapping("/about")
    public String about() {
        return "叶黎明 42411171 计算机科学与技术";}
}
