package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Activity;
import com.volunteer.entity.CheckIn;
import com.volunteer.entity.Notification;
import com.volunteer.entity.Registration;
import com.volunteer.entity.User;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.CheckInMapper;
import com.volunteer.mapper.NotificationMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 报名服务实现
 */
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Override
    @Transactional
    public void registerActivity(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查活动状态
        if ("ended".equals(activity.getStatus()) || "cancelled".equals(activity.getStatus())) {
            throw new RuntimeException("该活动已结束，无法报名");
        }

        // 检查活动是否已开始
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw new RuntimeException("活动已开始，无法报名");
        }

        // 检查是否已报名
        Registration existing = baseMapper.selectByUserAndActivity(userId, activityId);
        if (existing != null && "registered".equals(existing.getStatus())) {
            throw new RuntimeException("您已报名该活动");
        }

        // 检查是否名额已满
        int count = activityMapper.countRegistrations(activityId);
        if (count >= activity.getMaxParticipants()) {
            throw new RuntimeException("该活动报名人数已满");
        }

        if (existing != null) {
            // 之前取消过，重新报名
            existing.setStatus("registered");
            updateById(existing);
        } else {
            Registration reg = new Registration();
            reg.setUserId(userId);
            reg.setActivityId(activityId);
            reg.setStatus("registered");
            save(reg);
        }

        // 发送通知给管理员
        User volunteer = userMapper.selectById(userId);
        notificationMapper.insert(new Notification() {{
            setUserId(activity.getPublisherId());
            setTitle("新的报名通知");
            setContent("志愿者 " + (volunteer != null ? volunteer.getName() : "未知") + " 报名了活动【" + activity.getName() + "】");
            setIsRead(0);
        }});

        // 发送通知给志愿者
        notificationMapper.insert(new Notification() {{
            setUserId(userId);
            setTitle("报名成功");
            setContent("您已成功报名活动【" + activity.getName() + "】");
            setIsRead(0);
        }});
    }

    @Override
    @Transactional
    public void cancelRegistration(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查距活动开始时间是否超过2小时
        Duration duration = Duration.between(LocalDateTime.now(), activity.getStartTime());
        if (duration.toHours() < 2) {
            throw new RuntimeException("活动开始前2小时内不允许取消报名");
        }

        Registration reg = baseMapper.selectByUserAndActivity(userId, activityId);
        if (reg == null || !"registered".equals(reg.getStatus())) {
            throw new RuntimeException("您未报名该活动");
        }

        reg.setStatus("cancelled");
        updateById(reg);

        // 发送通知给管理员
        User volunteer = userMapper.selectById(userId);
        notificationMapper.insert(new Notification() {{
            setUserId(activity.getPublisherId());
            setTitle("取消报名通知");
            setContent("志愿者 " + (volunteer != null ? volunteer.getName() : "未知") + " 取消了活动【" + activity.getName() + "】的报名");
            setIsRead(0);
        }});

        // 发送通知给志愿者
        notificationMapper.insert(new Notification() {{
            setUserId(userId);
            setTitle("取消报名成功");
            setContent("您已成功取消活动【" + activity.getName() + "】的报名");
            setIsRead(0);
        }});
    }

    @Override
    public boolean isRegistered(Long userId, Long activityId) {
        Registration reg = baseMapper.selectByUserAndActivity(userId, activityId);
        return reg != null && "registered".equals(reg.getStatus());
    }

    @Override
    public IPage<Registration> getUserRegistrations(Long userId, int page, int size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, userId)
               .eq(Registration::getStatus, "registered")
               .orderByDesc(Registration::getCreateTime);

        IPage<Registration> result = page(new Page<>(page, size), wrapper);

        result.getRecords().forEach(reg -> {
            Activity activity = activityMapper.selectById(reg.getActivityId());
            if (activity != null) {
                reg.setActivityName(activity.getName());
            }
            User user = userMapper.selectById(reg.getUserId());
            if (user != null) {
                reg.setUserName(user.getName());
                reg.setUserPhone(user.getPhone());
            }
        });

        return result;
    }

    @Override
    public IPage<Registration> getActivityRegistrations(Long activityId, int page, int size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getActivityId, activityId)
               .eq(Registration::getStatus, "registered")
               .orderByAsc(Registration::getCreateTime);

        IPage<Registration> result = page(new Page<>(page, size), wrapper);

        // 查询该活动的所有签到记录
        LambdaQueryWrapper<CheckIn> ciWrapper = new LambdaQueryWrapper<>();
        ciWrapper.eq(CheckIn::getActivityId, activityId);
        java.util.List<CheckIn> checkIns = checkInMapper.selectList(ciWrapper);
        java.util.Map<Long, CheckIn> checkInMap = new java.util.HashMap<>();
        for (CheckIn ci : checkIns) {
            checkInMap.put(ci.getUserId(), ci);
        }

        result.getRecords().forEach(reg -> {
            User user = userMapper.selectById(reg.getUserId());
            if (user != null) {
                reg.setUserName(user.getName());
                reg.setUserPhone(user.getPhone());
            }
            Activity activity = activityMapper.selectById(reg.getActivityId());
            if (activity != null) {
                reg.setActivityName(activity.getName());
            }
            // 填充签到信息
            CheckIn ci = checkInMap.get(reg.getUserId());
            if (ci != null) {
                reg.setCheckedIn(true);
                reg.setCheckInType(ci.getCheckInType());
                reg.setCheckInTime(ci.getCheckInTime() != null ? ci.getCheckInTime().toString() : null);
                reg.setCheckInAddress(ci.getGpsAddress());
                reg.setCheckInImage(ci.getImagePath());
            } else {
                reg.setCheckedIn(false);
            }
        });

        return result;
    }
}
