package com.icbms.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoraMessage {

    private String applicationID;
    private String applicationName;
    private String nodeName;
    private String devEUI;
    private List<RxInfo> rxInfo;
    private TxInfo txInfo;
    private Integer fCnt;
    private Integer fPort;
    private String data;
    private String gatewayId;

}
