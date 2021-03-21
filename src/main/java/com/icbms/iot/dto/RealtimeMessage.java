package com.icbms.iot.dto;

import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.enums.BoxAlarmType;
import lombok.Data;

import java.util.List;

@Data
public class RealtimeMessage {

    private Integer boxNo;
    private Integer circuitVoltage;
    private String currentLeak;
    private Integer circuitPower;
    private Double modTemp;
    private String circuitCurrent;
    private List<AlarmType> alarmTypes;
    private String electric;
    private Integer aVoltage;
    private Integer bVoltage;
    private Integer cVoltage;
    private String aCurrent;
    private String bCurrent;
    private String cCurrent;
    private String nCurrent;
    private Integer aPower;
    private Integer bPower;
    private Integer cPower;
    private List<BoxAlarmType> aAlarmTypes;
    private List<BoxAlarmType> bAlarmTypes;
    private List<BoxAlarmType> cAlarmTypes;
    private String net380;
    private Boolean switchFlag;
    private Integer aPowerFactor;
    private Integer bPowerFactor;
    private Integer cPowerFactor;
    private Double aTemp;
    private Double bTemp;
    private Double cTemp;

    private String gatewayId;

    //private DataType dataType;

}
