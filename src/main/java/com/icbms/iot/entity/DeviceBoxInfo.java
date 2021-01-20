package com.icbms.iot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceBoxInfo {

    private String id;
    private String projectId;
    private String deviceBoxNum;
    private String deviceBoxPass;
    private String deviceBoxName;
    private Date createTime;
    private Date updateTime;
    private String createId;
    private String updateId;
    private String remark;
    private String online;
    private String fx;
    private String fy;
    private String secBoxGateway;
    private String standNo;
    private String boxCapacity;
    private String controlFlag;
    private String alarmLogId;
    private String placedFlag;

}
