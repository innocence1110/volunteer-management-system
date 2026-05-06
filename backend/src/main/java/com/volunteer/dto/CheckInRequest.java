package com.volunteer.dto;

import lombok.Data;

/**
 * 签到请求
 */
@Data
public class CheckInRequest {

    private Long activityId;
    private String checkInType;
    private String checkInCode;
    private String gpsAddress;
}
