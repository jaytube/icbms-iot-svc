package com.icbms.iot.entity;


import lombok.Data;

import java.util.Date;

@Data
public class ProjectInfo {

    private String id;
    private String projectName;
    private Date createTime;
    private Date updateTime;
    private String createId;
    private String updateId;
    private String remark;
    private String effectiveDate;
    private String expireDate;
    private String gatewayAddress;
    private Integer gymId;

}
