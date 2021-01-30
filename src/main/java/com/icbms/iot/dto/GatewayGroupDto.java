package com.icbms.iot.dto;

import com.icbms.iot.enums.GatewayRunType;
import lombok.Data;

import java.util.Set;

@Data
public class GatewayGroupDto {

    private Set<GatewayDto> gateways;
    private int groupId;
    private boolean stopped;
    private boolean finished;
}
