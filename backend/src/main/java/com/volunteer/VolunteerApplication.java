package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 志愿活动管理系统 启动类
 */
@SpringBootApplication
@MapperScan("com.volunteer.mapper")
public class VolunteerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VolunteerApplication.class, args);
    }
}
