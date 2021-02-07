package com.icbms.iot.dto;

import lombok.Data;

@Data
public class GatewayStatusDto {
    private Integer status;
    private String reportTime;
    private String msg;
}
