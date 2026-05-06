package com.volunteer.controller;

import com.volunteer.dto.CheckInRequest;
import com.volunteer.dto.Result;
import com.volunteer.entity.CheckIn;
import com.volunteer.service.CheckInService;
import com.volunteer.service.ActivityService;
import com.volunteer.entity.Activity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 签到控制器
 */
@Tag(name = "签到管理", description = "活动签到接口")
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private ActivityService activityService;

    @Operation(summary = "按钮签到")
    @PostMapping("/button/{activityId}")
    public Result<?> buttonCheckIn(@PathVariable Long activityId,
                                    @RequestBody CheckInRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            checkInService.buttonCheckIn(userId, activityId, request.getGpsAddress());
            return Result.success("签到成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "数字码签到")
    @PostMapping("/code/{activityId}")
    public Result<?> codeCheckIn(@PathVariable Long activityId,
                                  @RequestBody CheckInRequest request,
                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            checkInService.codeCheckIn(userId, activityId, request.getCheckInCode());
            return Result.success("签到成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "图片签到")
    @PostMapping("/image/{activityId}")
    public Result<?> imageCheckIn(@PathVariable Long activityId,
                                   @RequestParam("file") MultipartFile file,
                                   HttpServletRequest httpRequest) throws IOException {
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 校验文件
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            return Result.error("仅支持 JPG/PNG/GIF 格式图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        // 保存文件 - 使用绝对路径
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "checkin" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest.getAbsoluteFile());

        try {
            checkInService.imageCheckIn(userId, activityId, "/uploads/checkin/" + fileName);
            return Result.success("签到成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查是否已签到")
    @GetMapping("/check/{activityId}")
    public Result<?> checkStatus(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.isCheckedIn(userId, activityId));
    }

    @Operation(summary = "获取活动签到列表（管理员）")
    @GetMapping("/activity/{activityId}")
    public Result<?> getActivityCheckIns(@PathVariable Long activityId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }
        return Result.success(checkInService.getActivityCheckIns(activityId, page, size));
    }
}
