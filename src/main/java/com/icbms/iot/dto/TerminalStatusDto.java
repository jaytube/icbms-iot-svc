package com.icbms.iot.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TerminalStatusDto {
    private String gatewayId;
    private Integer status;
    private Date reportTime;
    private String msg;
}
