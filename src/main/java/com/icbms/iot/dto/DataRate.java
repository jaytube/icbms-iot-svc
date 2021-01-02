package com.icbms.iot.dto;

import lombok.Data;

@Data
public class DataRate {

    private String modulation;
    private Integer bandwidth;
    private Integer spreadFactor;

}
