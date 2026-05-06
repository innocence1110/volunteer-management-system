package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Activity;
import com.volunteer.entity.CheckIn;
import com.volunteer.entity.Registration;
import com.volunteer.entity.User;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.CheckInMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 签到服务实现
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 通用签到校验
     */
    private void validateCheckIn(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查活动是否在进行中
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new RuntimeException("当前不在活动签到时间内");
        }

        // 检查是否已报名
        Registration reg = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (reg == null || !"registered".equals(reg.getStatus())) {
            throw new RuntimeException("请先报名该活动再进行签到");
        }

        // 检查是否已签到
        CheckIn existing = baseMapper.selectByUserAndActivity(userId, activityId);
        if (existing != null) {
            throw new RuntimeException("您已签到，无需重复签到");
        }
    }

    /**
     * 签到成功后发放积分
     */
    private void awardPoints(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity != null && activity.getPointsReward() != null && activity.getPointsReward() > 0) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setPoints(user.getPoints() + activity.getPointsReward());
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional
    public void buttonCheckIn(Long userId, Long activityId, String gpsAddress) {
        validateCheckIn(userId, activityId);

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setActivityId(activityId);
        checkIn.setCheckInType("button");
        checkIn.setGpsAddress(gpsAddress);
        checkIn.setCheckInTime(LocalDateTime.now());

        save(checkIn);
        awardPoints(userId, activityId);
    }

    @Override
    @Transactional
    public void codeCheckIn(Long userId, Long activityId, String code) {
        validateCheckIn(userId, activityId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity.getCheckInCode() == null || !activity.getCheckInCode().equals(code)) {
            throw new RuntimeException("验证码错误");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setActivityId(activityId);
        checkIn.setCheckInType("code");
        checkIn.setCheckInTime(LocalDateTime.now());

        save(checkIn);
        awardPoints(userId, activityId);
    }

    @Override
    @Transactional
    public void imageCheckIn(Long userId, Long activityId, String imagePath) {
        validateCheckIn(userId, activityId);

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setActivityId(activityId);
        checkIn.setCheckInType("image");
        checkIn.setImagePath(imagePath);
        checkIn.setCheckInTime(LocalDateTime.now());

        save(checkIn);
        awardPoints(userId, activityId);
    }

    @Override
    public boolean isCheckedIn(Long userId, Long activityId) {
        CheckIn checkIn = baseMapper.selectByUserAndActivity(userId, activityId);
        return checkIn != null;
    }

    @Override
    public IPage<CheckIn> getActivityCheckIns(Long activityId, int page, int size) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getActivityId, activityId);
        wrapper.orderByAsc(CheckIn::getCheckInTime);

        IPage<CheckIn> result = page(new Page<>(page, size), wrapper);

        result.getRecords().forEach(ci -> {
            User user = userMapper.selectById(ci.getUserId());
            if (user != null) {
                ci.setUserName(user.getName());
            }
            Activity activity = activityMapper.selectById(ci.getActivityId());
            if (activity != null) {
                ci.setActivityName(activity.getName());
            }
        });

        return result;
    }
}
