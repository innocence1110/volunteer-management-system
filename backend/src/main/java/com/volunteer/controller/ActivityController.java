package com.volunteer.controller;

import com.volunteer.dto.ActivityRequest;
import com.volunteer.dto.Result;
import com.volunteer.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 活动控制器
 */
@Tag(name = "活动管理", description = "活动发布、管理、浏览接口")
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Operation(summary = "发布活动（管理员）")
    @PostMapping
    public Result<?> publishActivity(HttpServletRequest request,
                                      @Valid @RequestBody ActivityRequest activityRequest) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可以发布活动");
        }
        try {
            activityService.publishActivity(activityRequest, userId);
            return Result.success("发布成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改活动（管理员）")
    @PutMapping("/{id}")
    public Result<?> updateActivity(@PathVariable Long id,
                                     @Valid @RequestBody ActivityRequest activityRequest,
                                     HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可以修改活动");
        }
        try {
            activityService.updateActivity(id, activityRequest);
            return Result.success("修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除活动（管理员）")
    @DeleteMapping("/{id}")
    public Result<?> deleteActivity(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "仅管理员可以删除活动");
        }
        try {
            activityService.deleteActivity(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动列表（分页）")
    @GetMapping
    public Result<?> getActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(activityService.getActivityList(page, size, keyword));
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Result<?> getActivityDetail(@PathVariable Long id) {
        try {
            return Result.success(activityService.getActivityDetail(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我发布的活动（管理员）")
    @GetMapping("/my")
    public Result<?> getMyActivities(HttpServletRequest request,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }
        return Result.success(activityService.getMyActivities(userId, page, size));
    }
}
