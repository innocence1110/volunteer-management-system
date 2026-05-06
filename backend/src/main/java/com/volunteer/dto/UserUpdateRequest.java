package com.volunteer.dto;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {

    private String phone;
    private String major;
    private Integer age;
    private String studentId;
}
