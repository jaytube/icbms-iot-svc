package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class AlarmDataEntity {

    private String projectId;
    private String gatewayId;
    private String terminalId;
    private String switchAddr;
    private String alarmType;
    private String alarmContent;
    private String alarmStatus;
    private String reportTime;
    private String alarmLevel;

}
