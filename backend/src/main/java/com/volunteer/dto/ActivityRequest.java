package com.volunteer.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 活动创建/修改请求
 */
@Data
public class ActivityRequest {

    @NotBlank(message = "请输入活动名称")
    private String name;

    private String description;

    @NotNull(message = "请选择活动开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "请选择活动结束时间")
    private LocalDateTime endTime;

    @NotBlank(message = "请输入活动地点")
    private String location;

    @NotNull(message = "请输入招募人数")
    @Min(value = 1, message = "招募人数至少为1")
    private Integer maxParticipants;

    @NotBlank(message = "请选择签到方式")
    private String checkInType;

    private String checkInCode;

    private Integer pointsReward = 10;
}
