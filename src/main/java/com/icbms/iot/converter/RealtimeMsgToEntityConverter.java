package com.icbms.iot.converter;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.service.DeviceInfoService;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.RealDataEntity;
import com.icbms.iot.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

import static com.icbms.iot.constant.IotConstant.REAL_STAT_LAST_DATA;

@Service
public class RealtimeMsgToEntityConverter {


    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
        String field = realtimeMessage.getBoxNo() + "_100";
        String lastRealDataJson = (String) redisTemplate.opsForHash().get(REAL_STAT_LAST_DATA, field);
        if (StringUtils.isNotBlank(lastRealDataJson)) {
            RealDataEntity lastRealData = JSON.parseObject(lastRealDataJson, RealDataEntity.class);
            entity.setLastElectricCnt(lastRealData.getElectricCnt());
            BigDecimal addedNum = (new BigDecimal(entity.getElectricCnt())).subtract(new BigDecimal(lastRealData.getElectricCnt()));
            if (addedNum.compareTo(new BigDecimal("0")) >= 0) {
                entity.setAddedElectricCnt(addedNum.setScale(3, 4).toString());
            } else {
                entity.setAddedElectricCnt(entity.getElectricCnt());
            }
        } else {
            entity.setLastElectricCnt("0");
            entity.setAddedElectricCnt(entity.getElectricCnt());
        }
        return entity;
    }
}
