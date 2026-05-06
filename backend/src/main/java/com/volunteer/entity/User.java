package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名 */
    private String name;

    /** 电话号码 */
    private String phone;

    /** 登录账号 */
    private String account;

    /** 密码（加密存储） */
    private String password;

    /** 角色：admin-管理员, volunteer-志愿者 */
    private String role;

    /** 专业 */
    private String major;

    /** 年龄 */
    private Integer age;

    /** 学号 */
    private String studentId;

    /** 积分 */
    private Integer points;

    /** 头像路径 */
    private String avatar;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
