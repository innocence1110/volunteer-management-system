package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.CheckIn;

/**
 * 签到服务接口
 */
public interface CheckInService extends IService<CheckIn> {

    /** 按钮签到 */
    void buttonCheckIn(Long userId, Long activityId, String gpsAddress);

    /** 数字码签到 */
    void codeCheckIn(Long userId, Long activityId, String code);

    /** 图片签到 */
    void imageCheckIn(Long userId, Long activityId, String imagePath);

    /** 检查是否已签到 */
    boolean isCheckedIn(Long userId, Long activityId);

    /** 获取活动签到列表 */
    IPage<CheckIn> getActivityCheckIns(Long activityId, int page, int size);
}
