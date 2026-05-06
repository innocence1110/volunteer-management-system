package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.ActivityRequest;
import com.volunteer.entity.Activity;
import com.volunteer.entity.User;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动服务实现
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void publishActivity(ActivityRequest request, Long publisherId) {
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("活动时间不得早于当前时间");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("活动结束时间不能早于开始时间");
        }

        Activity activity = new Activity();
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setLocation(request.getLocation());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCheckInType(request.getCheckInType());
        activity.setCheckInCode(request.getCheckInCode());
        activity.setStatus("ongoing");
        activity.setPointsReward(request.getPointsReward() != null ? request.getPointsReward() : 10);
        activity.setPublisherId(publisherId);

        save(activity);
    }

    @Override
    @Transactional
    public void updateActivity(Long activityId, ActivityRequest request) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setLocation(request.getLocation());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCheckInType(request.getCheckInType());
        activity.setCheckInCode(request.getCheckInCode());
        if (request.getPointsReward() != null) {
            activity.setPointsReward(request.getPointsReward());
        }

        updateById(activity);
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        removeById(activityId);
    }

    /**
     * 检查并更新活动状态（到期自动变为已结束）
     */
    private void updateActivityStatus(Activity activity) {
        if (activity == null) return;
        LocalDateTime now = LocalDateTime.now();
        if ("ongoing".equals(activity.getStatus()) && now.isAfter(activity.getEndTime())) {
            activity.setStatus("ended");
            updateById(activity);
        }
    }

    /**
     * 批量刷新活动状态
     */
    private void refreshActivityStatuses() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, "ongoing");
        List<Activity> ongoingList = list(wrapper);
        LocalDateTime now = LocalDateTime.now();
        for (Activity a : ongoingList) {
            if (a.getEndTime() != null && now.isAfter(a.getEndTime())) {
                a.setStatus("ended");
                updateById(a);
            }
        }
    }

    @Override
    public IPage<Activity> getActivityList(int page, int size, String keyword) {
        // 自动刷新活动状态
        refreshActivityStatuses();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Activity::getName, keyword.trim());
        }
        wrapper.orderByAsc(Activity::getStartTime).orderByAsc(Activity::getName);

        IPage<Activity> result = page(new Page<>(page, size), wrapper);

        // 填充额外信息
        result.getRecords().forEach(activity -> {
            int count = baseMapper.countRegistrations(activity.getId());
            activity.setCurrentParticipants(count);

            User publisher = userMapper.selectById(activity.getPublisherId());
            if (publisher != null) {
                activity.setPublisherName(publisher.getName());
            }
        });

        return result;
    }

    @Override
    public Activity getActivityDetail(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        updateActivityStatus(activity);
        int count = baseMapper.countRegistrations(activity.getId());
        activity.setCurrentParticipants(count);

        User publisher = userMapper.selectById(activity.getPublisherId());
        if (publisher != null) {
            activity.setPublisherName(publisher.getName());
        }
        return activity;
    }

    @Override
    public IPage<Activity> getMyActivities(Long publisherId, int page, int size) {
        refreshActivityStatuses();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getPublisherId, publisherId);
        wrapper.orderByDesc(Activity::getCreateTime);

        IPage<Activity> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(activity -> {
            int count = baseMapper.countRegistrations(activity.getId());
            activity.setCurrentParticipants(count);
        });

        return result;
    }
}
