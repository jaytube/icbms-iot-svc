package com.icbms.iot.dto;

import lombok.Data;

@Data
public class RxInfo {

    private String mac;
    private Double rssi;
    private Double loRaSNR;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double altitude;

}
