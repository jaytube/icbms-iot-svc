package com.icbms.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class RealDataEntity {
    private String projectId;
    private String gatewayId;
    private String terminalId;
    private String switchAddr;
    private String switchOnoff;
    private String controlFlag;
    private String voltage;
    private String leakageCurrent;
    private String power;
    private String temperature;
    private String electricCurrent;
    private String electricCnt;
    private String lockStatus;
    private String reportTime;
    private String phaseVoltageA;
    private String phaseVoltageB;
    private String phaseVoltageC;
    private String phaseCurrentA;
    private String phaseCurrentB;
    private String phaseCurrentC;
    private String phaseCurrentN;
    private String phasePowerA;
    private String phasePowerB;
    private String phasePowerC;
    private String lastElectricCnt;
    private String addedElectricCnt;
}
