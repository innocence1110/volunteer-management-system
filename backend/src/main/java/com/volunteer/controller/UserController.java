package com.volunteer.controller;

import com.volunteer.dto.Result;
import com.volunteer.dto.UserUpdateRequest;
import com.volunteer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户控制器 - 个人信息管理
 */
@Tag(name = "用户管理", description = "个人信息管理接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return Result.success(userService.getProfile(userId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<?> updateProfile(HttpServletRequest request, @RequestBody UserUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            userService.updateProfile(userId, updateRequest);
            return Result.success("修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
