package com.volunteer.controller;

import com.volunteer.dto.LoginRequest;
import com.volunteer.dto.RegisterRequest;
import com.volunteer.dto.Result;
import com.volunteer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 登录/注册
 */
@Tag(name = "认证管理", description = "登录、注册接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getAccount(), request.getPassword());
            var user = userService.findByAccount(request.getAccount());
            user.setPassword(null);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success("登录成功", data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request.getName(), request.getPhone(),
                    request.getAccount(), request.getPassword(), request.getRole());
            return Result.success("注册成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
