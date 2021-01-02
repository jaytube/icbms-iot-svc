package com.icbms.iot.dto;

import lombok.Data;

@Data
public class TxInfo {

    private Long frequency;
    private Boolean adr;
    private String codeRate;
    private DataRate dataRate;
}
