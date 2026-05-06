package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者ID */
    private Long userId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 是否已读：0-未读, 1-已读 */
    private Integer isRead;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
