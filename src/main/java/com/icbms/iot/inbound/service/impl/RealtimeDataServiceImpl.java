package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.converter.RealtimeMsgToEntityConverter;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.DeviceBoxInfo;
import com.icbms.iot.entity.DeviceSwitchInfoDetailLog;
import com.icbms.iot.entity.DeviceSwitchInfoLog;
import com.icbms.iot.entity.RealDataEntity;
import com.icbms.iot.inbound.service.RealtimeDataService;
import com.icbms.iot.mapper.DeviceBoxInfoMapper;
import com.icbms.iot.mapper.DeviceSwitchInfoDetailLogMapper;
import com.icbms.iot.mapper.DeviceSwitchInfoLogMapper;
import com.icbms.iot.util.CommonUtil;
import com.icbms.iot.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class RealtimeDataServiceImpl implements RealtimeDataService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RealtimeMsgToEntityConverter msgToEntityConverter;

    @Autowired
    private DeviceBoxInfoMapper deviceBoxInfoMapper;

    @Autowired
    private DeviceSwitchInfoLogMapper switchInfoLogMapper;

    @Autowired
    private DeviceSwitchInfoDetailLogMapper switchInfoDetailLogMapper;


    @Override
    public void processRealtimeData(List<RealtimeMessage> realtimeMsgList) {
        logger.info("处理实时数据===========>开始");
        if(CollectionUtils.isEmpty(realtimeMsgList))
            return;

        List<RealDataEntity> entityList = realtimeMsgList.stream().map(msgToEntityConverter::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<RealDataEntity> result = new ArrayList<>();
        for (RealDataEntity realData : entityList) {
            String terminalId = realData.getTerminalId();
            if (redisTemplate.opsForHash().hasKey(REAL_DATA, terminalId)) {
                String latestTime = (String) redisTemplate.opsForHash().get(REAL_HIS_DATA_STORE_UP_TO_DATE, terminalId);
                long currentTime = System.currentTimeMillis();
                if (currentTime - Long.valueOf(latestTime) >= REAL_DATA_SAVE_FREQUENCY) {
                    redisTemplate.opsForHash().put(REAL_STAT_LAST_DATA, terminalId, JSON.toJSONString(realData));
                    redisTemplate.opsForHash().put(REAL_HIS_DATA_STORE_UP_TO_DATE, terminalId, String.valueOf(currentTime));
                    result.add(realData);
                }
            } else {
                redisTemplate.opsForHash().put(REAL_STAT_LAST_DATA, terminalId, JSON.toJSONString(realData));
                redisTemplate.opsForHash().put(REAL_HIS_DATA_STORE_UP_TO_DATE, terminalId, String.valueOf(System.currentTimeMillis()));
                result.add(realData);
            }
            redisTemplate.opsForHash().put(REAL_DATA, terminalId, JSON.toJSONString(realData));
        }

        saveRealHisDataEntities(result);
        logger.info("处理实时数据===========>结束");
    }

    private void saveRealHisDataEntities(List<RealDataEntity> list) {
        if(CollectionUtils.isEmpty(list))
            return;

        List<String> projectIdList = list.stream().filter(Objects::nonNull).map(RealDataEntity::getProjectId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DeviceBoxInfo> deviceBoxes = deviceBoxInfoMapper.findByProjectIdList(projectIdList);
        List<String> deviceBoxNums = list.stream().filter(Objects::nonNull).map(RealDataEntity::getTerminalId).collect(Collectors.toList());
        deviceBoxes = deviceBoxes.stream().filter(l -> deviceBoxNums.contains(l.getDeviceBoxNum().substring(BOX_NO_START_INDEX)))
                .collect(Collectors.toList());
        Map<String, DeviceBoxInfo> deviceBoxMap = new HashMap<>();
        for (DeviceBoxInfo deviceBox : deviceBoxes) {
            if(deviceBox.getDeviceBoxNum().length() == 12)
                deviceBoxMap.put(deviceBox.getDeviceBoxNum().substring(BOX_NO_START_INDEX), deviceBox);
        }

        List<DeviceSwitchInfoLog> logs = new ArrayList<>();
        List<DeviceSwitchInfoDetailLog> detailLogs = new ArrayList<>();
        for (RealDataEntity l : list) {
            DeviceSwitchInfoLog log = new DeviceSwitchInfoLog();
            String deviceSwitchLogInfoId = CommonUtil.uuid();
            log.setId(deviceSwitchLogInfoId);
            log.setProjectId(l.getProjectId());
            DeviceBoxInfo deviceBoxInfo = deviceBoxMap.get(l.getTerminalId());
            String deviceBoxId = deviceBoxInfo != null ? deviceBoxInfo.getDeviceBoxNum() : "";
            log.setDeviceBoxId(deviceBoxId);
            log.setDeviceSwitchName("");
            //TODO
            log.setAddress("");
            log.setDeviceSwitchStatus(l.getSwitchOnoff());
            log.setSwitchElectric(l.getElectricCurrent());
            log.setSwitchElectriCnt(l.getElectricCnt());
            log.setSwitchVoltage(l.getVoltage());
            log.setSwitchTemperature(l.getTemperature());
            log.setSwitchPower(l.getPower());
            log.setSwitchLeakage(l.getLeakageCurrent());
            Date reportTime = DateUtil.parse(l.getReportTime(), "yyyy-MM-dd HH:mm:ss");
            log.setCreateTime(reportTime);
            log.setUpdateTime(reportTime);
            log.setRemark(SWITCH_INFO_REMARK);
            logs.add(log);

            //detail log
            DeviceSwitchInfoDetailLog detailLog = new DeviceSwitchInfoDetailLog();
            detailLog.setId(CommonUtil.uuid());
            detailLog.setDeviceSwitchInfoLogId(deviceSwitchLogInfoId);
            detailLog.setProjectId(l.getProjectId());
            detailLog.setDeviceBoxId(deviceBoxId);
            detailLog.setAddress("");
            detailLog.setSwitchVoltageA(l.getPhaseVoltageA());
            detailLog.setSwitchVoltageB(l.getPhaseVoltageB());
            detailLog.setSwitchVoltageC(l.getPhaseVoltageC());
            detailLog.setSwitchElectricA(l.getPhaseCurrentA());
            detailLog.setSwitchElectricB(l.getPhaseCurrentB());
            detailLog.setSwitchElectricC(l.getPhaseCurrentC());
            detailLog.setSwitchElectricN(l.getPhaseCurrentN());
            detailLog.setSwitchPowerA(l.getPhasePowerA());
            detailLog.setSwitchPowerB(l.getPhasePowerB());
            detailLog.setSwitchPowerC(l.getPhasePowerC());
            detailLog.setCreateTime(reportTime);
            detailLog.setUpdateTime(reportTime);
            detailLog.setRemark(SWITCH_INFO_DETAIL_REMARK);
            detailLogs.add(detailLog);
        }


        switchInfoLogMapper.batchInsert(logs);
        logger.info("插入device_switch_info_log");

        switchInfoDetailLogMapper.batchInsert(detailLogs);
        logger.info("插入device_switch_info_detail_log");
    }
}
