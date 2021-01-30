package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Cherry
 * @Date: 2021/1/28
 * @Desc: GateWayDeviceMap
 */
@Data
@EqualsAndHashCode
public class GatewayDeviceMap {

    private int mapId;

    private int gatewayId;

    private String deviceSn;

    private int deviceId;

    private String projectId;

    private int gymId;
}
