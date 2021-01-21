package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.entity.DeviceAlarmInfoLog;
import com.icbms.iot.entity.DeviceBoxInfo;
import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.mapper.DeviceAlarmInfoLogMapper;
import com.icbms.iot.mapper.DeviceBoxInfoMapper;
import com.icbms.iot.service.GatewayConfigService;
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
public class AlarmDataServiceImpl implements AlarmDataService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GatewayConfigService gatewayConfigService;

    @Autowired
    private DeviceBoxInfoMapper deviceBoxInfoMapper;

    @Autowired
    private DeviceAlarmInfoLogMapper deviceAlarmInfoLogMapper;

    public Map<String, Object> generateAlarmData(RealtimeMessage realtimeMessage) {
        List<AlarmDataEntity> list = new ArrayList<>();
        List<AlarmType> alarmTypes = realtimeMessage.getAlarmTypes();
        String boxNo = realtimeMessage.getBoxNo() + "";
        String projectId = gatewayConfigService.getProjectIdByTerminalId(realtimeMessage.getBoxNo() + "");
        Map<String, String> map = new HashMap<>();
        if(CollectionUtils.isEmpty(alarmTypes)) {
            AlarmType[] alarmTypeArr = AlarmType.values();
            for (int i=0; i<alarmTypeArr.length; i++) {
                AlarmType alarmType = alarmTypeArr[i];
                String jsonStr = (String) redisTemplate.opsForHash().get(ALARM_DATA, boxNo + "_" + (15 - i));
                if (StringUtils.isNotBlank(jsonStr)) {
                    AlarmDataEntity alarmEntity = JSON.parseObject(jsonStr, AlarmDataEntity.class);
                    if (!"0".equals(alarmEntity.getAlarmStatus())) {
                        alarmEntity.setAlarmStatus("0");
                        alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                        alarmEntity.setProjectId(projectId);
                        alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                        alarmEntity.setTerminalId(boxNo);
                        alarmEntity.setSwitchAddr("");
                        alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                        alarmEntity.setAlarmContent(alarmType.getAlarmContent() + "恢复");
                        alarmEntity.setAlarmType(alarmType.getAlarmContent());
                        map.put(boxNo + "_" + (15 - i), JSON.toJSONString(alarmEntity));
                        list.add(alarmEntity);
                    }
                }
            }
        } else {
            for (AlarmType alarmType : alarmTypes) {
                int code = alarmType.getCode();
                String jsonStr = (String) redisTemplate.opsForHash().get(ALARM_DATA, boxNo + "_" + (15 - code));
                if (StringUtils.isNotBlank(jsonStr)) {
                    AlarmDataEntity alarmEntity = JSON.parseObject(jsonStr, AlarmDataEntity.class);
                    if (!"1".equals(alarmEntity.getAlarmStatus())) {
                        alarmEntity.setAlarmStatus("1");
                        alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                        alarmEntity.setProjectId(projectId);
                        alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                        alarmEntity.setTerminalId(boxNo);
                        alarmEntity.setSwitchAddr("");
                        alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                        alarmEntity.setAlarmContent(alarmType.getAlarmContent());
                        alarmEntity.setAlarmType(alarmType.getAlarmContent());
                        map.put(boxNo + "_" + (15 - code), JSON.toJSONString(alarmEntity));
                        list.add(alarmEntity);
                    }
                } else {
                    AlarmDataEntity alarmEntity = new AlarmDataEntity();
                    alarmEntity.setAlarmStatus("1");
                    alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                    alarmEntity.setProjectId(projectId);
                    alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                    alarmEntity.setTerminalId(boxNo);
                    alarmEntity.setSwitchAddr("");
                    alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                    alarmEntity.setAlarmContent(alarmType.getAlarmContent());
                    alarmEntity.setAlarmType(alarmType.getAlarmContent());
                    map.put(boxNo + "_" + (15 - code), JSON.toJSONString(alarmEntity));
                    list.add(alarmEntity);
                }
            }

        }
        //redisTemplate.opsForHash().putAll(ALARM_DATA, resultMap);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(REDIS_ALARM, map);
        resultMap.put(MYSQL_ALARM, list);

        return resultMap;

    }

    @Override
    public void saveAlarmDataEntityList(List<AlarmDataEntity> list) {
        if(CollectionUtils.isEmpty(list))
            return;

        List<String> projectIdList = list.stream().filter(Objects::nonNull).map(AlarmDataEntity::getProjectId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DeviceBoxInfo> deviceBoxes = deviceBoxInfoMapper.findByProjectIdList(projectIdList);
        List<String> deviceBoxNums = list.stream().filter(Objects::nonNull).map(AlarmDataEntity::getTerminalId).collect(Collectors.toList());
        deviceBoxes = deviceBoxes.stream().filter(l -> deviceBoxNums.contains(l.getDeviceBoxNum()))
                .collect(Collectors.toList());
        Map<String, DeviceBoxInfo> deviceBoxMap = new HashMap<>();
        for (DeviceBoxInfo deviceBox : deviceBoxes) {
            deviceBoxMap.put(deviceBox.getDeviceBoxNum(), deviceBox);
        }
        List<DeviceAlarmInfoLog> logs = list.stream().map(l -> {
            DeviceAlarmInfoLog log = new DeviceAlarmInfoLog();
            log.setId(UUID.randomUUID().toString());
            log.setAlarmStatus(l.getAlarmStatus());
            log.setAlarmLevel(l.getAlarmLevel());
            log.setCreateTime(new Date());
            log.setUpdateTime(new Date());
            log.setRecordTime(DateUtil.parse(l.getReportTime(), "yyyy-MM-dd HH:mm:ss"));
            log.setDeviceBoxId(l.getTerminalId());
            DeviceBoxInfo info = deviceBoxMap.get(l.getTerminalId());
            log.setDeviceBoxMac(info.getDeviceBoxNum());
            log.setDeviceBoxId(info.getId());
            log.setNode("");
            log.setType(l.getAlarmType());
            log.setRemark(l.getAlarmContent());
            log.setInfo(l.getAlarmContent());
            return log;
        }).collect(Collectors.toList());

        deviceAlarmInfoLogMapper.batchInsert(logs);
    }

    @Override
    public void processAlarmData(List<RealtimeMessage> msgList) {
        Map<String, String> alarmDataMap = new HashMap<>();
        List<AlarmDataEntity> alarmDataList = new ArrayList<>();
        for(RealtimeMessage msg : msgList) {
            Map<String, Object> map = generateAlarmData(msg);
            Map<String, String> redisMap = (Map<String, String>) map.get(REDIS_ALARM);
            alarmDataMap.putAll(redisMap);
            List<AlarmDataEntity> list = (List<AlarmDataEntity>) map.get(MYSQL_ALARM);
            alarmDataList.addAll(list);
        }

        redisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
        saveAlarmDataEntityList(alarmDataList);
    }
}
