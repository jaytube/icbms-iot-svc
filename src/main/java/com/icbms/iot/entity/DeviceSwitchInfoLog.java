package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
public class DeviceSwitchInfoLog {

    private String id;
    private String projectId;
    private String deviceBoxId;
    private String deviceSwitchName;
    private String address;
    private String deviceSwitchStatus;
    private String switchElectric;
    private String switchElectriCnt;
    private String switchVoltage;
    private String switchTemperature;
    private String switchPower;
    private String switchLeakage;
    private Date createTime;
    private Date updateTime;
    private String createId;
    private String updateId;
    private String remark;
    private String online;
    private String interfaceUpdateTime;
    private String enableNetCtrl;

}
