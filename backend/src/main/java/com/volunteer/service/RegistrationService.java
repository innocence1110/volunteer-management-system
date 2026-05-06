package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Registration;

/**
 * 报名服务接口
 */
public interface RegistrationService extends IService<Registration> {

    /** 报名活动 */
    void registerActivity(Long userId, Long activityId);

    /** 取消报名 */
    void cancelRegistration(Long userId, Long activityId);

    /** 检查是否已报名 */
    boolean isRegistered(Long userId, Long activityId);

    /** 获取用户报名列表 */
    IPage<Registration> getUserRegistrations(Long userId, int page, int size);

    /** 获取活动报名列表 */
    IPage<Registration> getActivityRegistrations(Long activityId, int page, int size);
}
