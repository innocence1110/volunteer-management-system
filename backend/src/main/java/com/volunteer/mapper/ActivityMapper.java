package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动 Mapper
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Select("SELECT COUNT(*) FROM registrations WHERE activity_id = #{activityId} AND status = 'registered'")
    int countRegistrations(@Param("activityId") Long activityId);
}
