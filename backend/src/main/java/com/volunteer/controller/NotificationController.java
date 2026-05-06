package com.volunteer.controller;

import com.volunteer.dto.Result;
import com.volunteer.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知控制器
 */
@Tag(name = "通知管理", description = "系统通知接口")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<?> getNotifications(HttpServletRequest request,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("标记成功", null);
    }

    @Operation(summary = "标记全部已读")
    @PutMapping("/read-all")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记已读", null);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getUnreadCount(userId));
    }
}
