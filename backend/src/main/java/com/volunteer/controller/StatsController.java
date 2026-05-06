package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.Result;
import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据控制器
 */
@Tag(name = "数据统计", description = "系统统计数据接口")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Operation(summary = "获取系统统计数据")
    @GetMapping
    public Result<?> getStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }

        Map<String, Object> stats = new HashMap<>();

        // 注册用户数
        long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        // 活动总数
        long totalActivities = activityMapper.selectCount(null);
        stats.put("totalActivities", totalActivities);

        // 总报名数（status=registered）
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getStatus, "registered");
        long totalRegistrations = registrationMapper.selectCount(regWrapper);
        stats.put("totalRegistrations", totalRegistrations);

        // 总签到数
        long totalCheckIns = checkInMapper.selectCount(null);
        stats.put("totalCheckIns", totalCheckIns);

        return Result.success(stats);
    }
}
