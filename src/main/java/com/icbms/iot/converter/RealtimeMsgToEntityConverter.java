package com.icbms.iot.converter;

import com.icbms.iot.dto.RealTimeMessage;
import com.icbms.iot.entity.RealDataEntity;
import com.icbms.iot.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RealtimeMsgToEntityConverter {

    public RealDataEntity convert(RealTimeMessage realTimeMessage) {
        RealDataEntity entity = new RealDataEntity();
        //entity.setProjectId();
        //entity.setGatewayId();
        entity.setTerminalId(Objects.toString(realTimeMessage.getBoxNo(), null));
        //entity.setSwitchAddr(realTimeMessage.getSwitchFlag());
        //entity.setControlFlag(realTimeMessage.getSwitchFlag());
        entity.setVoltage(Objects.toString(realTimeMessage.getCircuitVoltage(), null));
        entity.setLeakageCurrent(Objects.toString(realTimeMessage.getCurrentLeak(), null));
        entity.setPower(Objects.toString(realTimeMessage.getCircuitPower(), null));
        entity.setTemperature(Objects.toString(realTimeMessage.getModTemp()));
        entity.setElectricCurrent(Objects.toString(realTimeMessage.getCircuitCurrent()));
        entity.setElectricCnt(Objects.toString(realTimeMessage.getElectric()));
        //entity.setLockStatus(Objects.toString(realTimeMessage.getSwitchFlag()));
        entity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
        entity.setPhaseVoltageA(Objects.toString(realTimeMessage.getAVoltage()));
        entity.setPhaseVoltageB(Objects.toString(realTimeMessage.getBVoltage()));
        entity.setPhaseVoltageC(Objects.toString(realTimeMessage.getCVoltage()));
        entity.setPhaseCurrentA(Objects.toString(realTimeMessage.getACurrent()));
        entity.setPhaseCurrentB(Objects.toString(realTimeMessage.getBCurrent()));
        entity.setPhaseCurrentC(Objects.toString(realTimeMessage.getCCurrent()));
        entity.setPhaseCurrentN(Objects.toString(realTimeMessage.getNCurrent()));
        entity.setPhasePowerA(Objects.toString(realTimeMessage.getAPower()));
        entity.setPhasePowerB(Objects.toString(realTimeMessage.getBPower()));
        entity.setPhasePowerC(Objects.toString(realTimeMessage.getCPower()));

        return entity;
    }
}
