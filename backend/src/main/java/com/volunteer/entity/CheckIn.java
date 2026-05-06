package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 签到实体类
 */
@Data
@TableName("check_ins")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long userId;

    /** 活动ID */
    private Long activityId;

    /** 签到方式：button-按钮, code-数字码, image-图片 */
    private String checkInType;

    /** 签到时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime checkInTime;

    /** GPS定位地址 */
    private String gpsAddress;

    /** 签到图片路径 */
    private String imagePath;

    /** 志愿者姓名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 活动名称（非数据库字段） */
    @TableField(exist = false)
    private String activityName;
}
