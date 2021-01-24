package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
public class DeviceSwitchInfoDetailLog {

    private String id;
    private String deviceSwitchInfoLogId;
    private String projectId;
    private String deviceBoxId;
    private String address;
    private String switchVoltageA;
    private String switchVoltageB;
    private String switchVoltageC;
    private String switchElectricA;
    private String switchElectricB;
    private String switchElectricC;
    private String switchElectricN;
    private String switchPowerA;
    private String switchPowerB;
    private String switchPowerC;
    private Date createTime;
    private Date updateTime;
    private String remark;

}
