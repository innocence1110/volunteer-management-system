package com.volunteer.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "请输入账号")
    private String account;

    @NotBlank(message = "请输入密码")
    private String password;
}
