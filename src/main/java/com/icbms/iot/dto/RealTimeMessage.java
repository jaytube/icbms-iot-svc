package com.icbms.iot.dto;

import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.enums.BoxAlarmType;
import lombok.Data;

@Data
public class RealTimeMessage {

    private Integer boxNo;
    private Integer circuitVoltage;
    private Double currentLeak;
    private Integer circuitPower;
    private Double modTemp;
    private Double circuitCurrent;
    private AlarmType alarmType;
    private Double electric;
    private Integer aVoltage;
    private Integer bVoltage;
    private Integer cVoltage;
    private Double aCurrent;
    private Double bCurrent;
    private Double cCurrent;
    private Double nCurrent;
    private Integer aPower;
    private Integer bPower;
    private Integer cPower;
    private BoxAlarmType aAlarmType;
    private BoxAlarmType bAlarmType;
    private BoxAlarmType cAlarmType;
    private String net380;
    private boolean on;
    private Integer aPowerFactor;
    private Integer bPowerFactor;
    private Integer cPowerFactor;
    private Double aTemp;
    private Double bTemp;
    private Double cTemp;

}
