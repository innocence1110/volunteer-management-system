package com.volunteer.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "请输入姓名")
    private String name;

    @NotBlank(message = "请输入电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phone;

    @NotBlank(message = "请输入账号")
    private String account;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @NotBlank(message = "请选择角色")
    private String role;
}
