package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
@TableName("activities")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动名称 */
    private String name;

    /** 活动描述 */
    private String description;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 活动地点 */
    private String location;

    /** 招募人数上限 */
    private Integer maxParticipants;

    /** 签到方式：button-按钮, code-数字码, image-图片 */
    private String checkInType;

    /** 数字码签到验证码 */
    private String checkInCode;

    /** 活动状态：pending-待审核, ongoing-进行中, ended-已结束, cancelled-已取消 */
    private String status;

    /** 完成活动奖励积分 */
    private Integer pointsReward;

    /** 发布者ID */
    private Long publisherId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 当前已报名人数（非数据库字段） */
    @TableField(exist = false)
    private Integer currentParticipants;

    /** 发布者姓名（非数据库字段） */
    @TableField(exist = false)
    private String publisherName;
}
