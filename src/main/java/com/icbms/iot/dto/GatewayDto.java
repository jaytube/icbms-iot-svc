package com.icbms.iot.dto;

import com.icbms.iot.enums.GatewayRunType;
import lombok.Data;

@Data
public class GatewayDto {

    private int id;
    private String ip;
    private String port;
    private volatile boolean finished;
    private volatile boolean stopped;
    private GatewayRunType type;

}
