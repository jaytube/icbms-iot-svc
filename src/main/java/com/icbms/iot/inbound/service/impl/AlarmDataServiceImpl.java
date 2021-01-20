package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.dto.RealTimeMessage;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.service.GatewayConfigService;
import com.icbms.iot.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class AlarmDataServiceImpl implements AlarmDataService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GatewayConfigService gatewayConfigService;

    public Map<String, Object> generateAlarmData(RealTimeMessage realTimeMessage) {
        List<AlarmDataEntity> list = new ArrayList<>();
        List<AlarmType> alarmTypes = realTimeMessage.getAlarmTypes();
        String boxNo = realTimeMessage.getBoxNo() + "";
        String projectId = gatewayConfigService.getProjectIdByTerminalId(realTimeMessage.getBoxNo() + "");
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
                        alarmEntity.setGatewayId(realTimeMessage.getGatewayId());
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
                String jsonStr = (String) this.redisTemplate.opsForHash().get(ALARM_DATA, boxNo + "_" + (15 - code));
                if (StringUtils.isNotBlank(jsonStr)) {
                    AlarmDataEntity alarmEntity = JSON.parseObject(jsonStr, AlarmDataEntity.class);
                    if (!"1".equals(alarmEntity.getAlarmStatus())) {
                        alarmEntity.setAlarmStatus("1");
                        alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                        alarmEntity.setProjectId(projectId);
                        alarmEntity.setGatewayId(realTimeMessage.getGatewayId());
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
                    alarmEntity.setGatewayId(realTimeMessage.getGatewayId());
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

    }
}
