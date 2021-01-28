package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Cherry
 * @Date: 2021/1/28
 * @Desc: GateWayInfo
 */
@Data
@EqualsAndHashCode
public class GatewayAddress {

    private int id;

    private String ipAddress;

    private String name;

}
