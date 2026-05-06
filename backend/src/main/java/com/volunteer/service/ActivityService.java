package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.ActivityRequest;
import com.volunteer.entity.Activity;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {

    /** 发布活动 */
    void publishActivity(ActivityRequest request, Long publisherId);

    /** 修改活动 */
    void updateActivity(Long activityId, ActivityRequest request);

    /** 删除活动 */
    void deleteActivity(Long activityId);

    /** 获取活动列表（分页） */
    IPage<Activity> getActivityList(int page, int size, String keyword);

    /** 获取活动详情 */
    Activity getActivityDetail(Long activityId);

    /** 获取管理员发布的活动列表 */
    IPage<Activity> getMyActivities(Long publisherId, int page, int size);
}
