package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 签到 Mapper
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    @Select("SELECT * FROM check_ins WHERE user_id = #{userId} AND activity_id = #{activityId}")
    CheckIn selectByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);
}
