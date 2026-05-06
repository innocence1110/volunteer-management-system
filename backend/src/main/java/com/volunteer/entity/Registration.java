package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报名实体类
 */
@Data
@TableName("registrations")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 志愿者ID */
    private Long userId;

    /** 活动ID */
    private Long activityId;

    /** 报名状态：registered-已报名, cancelled-已取消 */
    private String status;

    /** 报名时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 志愿者姓名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 志愿者电话（非数据库字段） */
    @TableField(exist = false)
    private String userPhone;

    /** 活动名称（非数据库字段） */
    @TableField(exist = false)
    private String activityName;

    /** 是否已签到（非数据库字段） */
    @TableField(exist = false)
    private Boolean checkedIn;

    /** 签到方式（非数据库字段） */
    @TableField(exist = false)
    private String checkInType;

    /** 签到时间（非数据库字段） */
    @TableField(exist = false)
    private String checkInTime;

    /** 签到GPS地址（非数据库字段） */
    @TableField(exist = false)
    private String checkInAddress;

    /** 签到图片路径（非数据库字段） */
    @TableField(exist = false)
    private String checkInImage;
}
