package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报名 Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("SELECT * FROM registrations WHERE user_id = #{userId} AND activity_id = #{activityId} AND status = 'registered'")
    Registration selectByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);
}
