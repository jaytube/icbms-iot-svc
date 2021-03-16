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
public class GatewayInfo {

    private int id;
    private int gatewayId;
    private long createTime;
    private long updateTime;
    private int loraId;
    private String name;
    private int applicationId;
    private String applicationName;
    private int loraApplicationId;
    private int sceneId;
    private String sceneName;
    private int loraSceneId;
    private String macAddress;
    private String des;
    private String mgrUrl;
    private int createUserId;
    private String updateUserName;
    private int updateUserId;
    private int isDel;
    private String ipAddress;
    private String online;
}
