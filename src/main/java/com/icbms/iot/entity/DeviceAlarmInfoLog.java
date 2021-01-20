package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
public class DeviceAlarmInfoLog {

    private String id;
    private String projectId;
    private String deviceBoxMac;
    private String deviceBoxId;
    private String node;
    private String type;
    private String alarmLevel;
    private String info;
    private String alarmStatus;
    private Date recordTime;
    private Date createTime;
    private Date updateTime;
    private String createId;
    private String updateId;
    private String remark;

}
