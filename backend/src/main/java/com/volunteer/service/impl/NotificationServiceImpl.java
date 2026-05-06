package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Notification;
import com.volunteer.mapper.NotificationMapper;
import com.volunteer.service.NotificationService;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void sendNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        save(notification);
    }

    @Override
    public IPage<Notification> getUserNotifications(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.orderByDesc(Notification::getCreateTime);

        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void markAsRead(Long notificationId) {
        baseMapper.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        baseMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        return (int) count(wrapper);
    }
}
