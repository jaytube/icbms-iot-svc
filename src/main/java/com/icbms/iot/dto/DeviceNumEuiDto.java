package com.icbms.iot.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeviceNumEuiDto implements Serializable {

    private Integer boxNo;
    private String devEUI;
    private String projectId;
}
