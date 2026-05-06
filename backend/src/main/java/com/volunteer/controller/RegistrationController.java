package com.volunteer.controller;

import com.volunteer.dto.Result;
import com.volunteer.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 报名控制器
 */
@Tag(name = "报名管理", description = "活动报名、取消报名接口")
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Operation(summary = "报名活动")
    @PostMapping("/{activityId}")
    public Result<?> registerActivity(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            registrationService.registerActivity(userId, activityId);
            return Result.success("报名成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消报名")
    @DeleteMapping("/{activityId}")
    public Result<?> cancelRegistration(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            registrationService.cancelRegistration(userId, activityId);
            return Result.success("取消报名成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查是否已报名")
    @GetMapping("/check/{activityId}")
    public Result<?> checkRegistration(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(registrationService.isRegistered(userId, activityId));
    }

    @Operation(summary = "获取我的报名列表")
    @GetMapping("/my")
    public Result<?> getMyRegistrations(HttpServletRequest request,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(registrationService.getUserRegistrations(userId, page, size));
    }

    @Operation(summary = "获取活动报名列表（管理员）")
    @GetMapping("/activity/{activityId}")
    public Result<?> getActivityRegistrations(@PathVariable Long activityId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }
        return Result.success(registrationService.getActivityRegistrations(activityId, page, size));
    }
}
