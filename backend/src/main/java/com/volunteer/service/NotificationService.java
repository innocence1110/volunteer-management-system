package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Notification;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /** 发送通知 */
    void sendNotification(Long userId, String title, String content);

    /** 获取用户通知列表 */
    IPage<Notification> getUserNotifications(Long userId, int page, int size);

    /** 标记已读 */
    void markAsRead(Long notificationId);

    /** 标记全部已读 */
    void markAllAsRead(Long userId);

    /** 获取未读数量 */
    int getUnreadCount(Long userId);
}
