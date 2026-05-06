package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 通知 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Update("UPDATE notifications SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notifications SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);
}
