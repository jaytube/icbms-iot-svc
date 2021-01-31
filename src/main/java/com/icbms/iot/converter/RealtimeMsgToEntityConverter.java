package com.icbms.iot.converter;

import com.icbms.iot.common.service.DeviceInfoService;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.RealDataEntity;
import com.icbms.iot.common.service.GatewayConfigService;
import com.icbms.iot.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RealtimeMsgToEntityConverter {


    @Autowired
    private DeviceInfoService deviceInfoService;

    public RealDataEntity convert(RealtimeMessage realtimeMessage) {
        RealDataEntity entity = new RealDataEntity();
        String projectId = deviceInfoService.getProjectIdByDeviceNo(realtimeMessage.getBoxNo() + "");
        entity.setProjectId(projectId);
        entity.setGatewayId(realtimeMessage.getGatewayId());
        entity.setTerminalId(Objects.toString(realtimeMessage.getBoxNo(), null));
        entity.setSwitchAddr("100");
        entity.setSwitchOnoff(realtimeMessage.getSwitchFlag() ? "0" : "1");
        entity.setControlFlag("0");
        entity.setVoltage(Objects.toString(realtimeMessage.getCircuitVoltage(), null));
        entity.setLeakageCurrent(Objects.toString(realtimeMessage.getCurrentLeak(), null));
        entity.setPower(Objects.toString(realtimeMessage.getCircuitPower(), null));
        entity.setTemperature(Objects.toString(realtimeMessage.getModTemp()));
        entity.setElectricCurrent(Objects.toString(realtimeMessage.getCircuitCurrent()));
        entity.setElectricCnt(Objects.toString(realtimeMessage.getElectric()));
        entity.setLockStatus("");
        entity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
        entity.setPhaseVoltageA(Objects.toString(realtimeMessage.getAVoltage()));
        entity.setPhaseVoltageB(Objects.toString(realtimeMessage.getBVoltage()));
        entity.setPhaseVoltageC(Objects.toString(realtimeMessage.getCVoltage()));
        entity.setPhaseCurrentA(Objects.toString(realtimeMessage.getACurrent()));
        entity.setPhaseCurrentB(Objects.toString(realtimeMessage.getBCurrent()));
        entity.setPhaseCurrentC(Objects.toString(realtimeMessage.getCCurrent()));
        entity.setPhaseCurrentN(Objects.toString(realtimeMessage.getNCurrent()));
        entity.setPhasePowerA(Objects.toString(realtimeMessage.getAPower()));
        entity.setPhasePowerB(Objects.toString(realtimeMessage.getBPower()));
        entity.setPhasePowerC(Objects.toString(realtimeMessage.getCPower()));

        return entity;
    }
}
