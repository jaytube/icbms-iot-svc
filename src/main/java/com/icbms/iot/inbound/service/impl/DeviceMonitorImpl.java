package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.dto.DeviceNumEuiDto;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.DeviceMonitor;
import com.icbms.iot.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class DeviceMonitorImpl implements DeviceMonitor {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AlarmDataService alarmDataService;

    @Override
    @Scheduled(fixedDelay = MONITOR_FREQUENCY)
    @Transactional
    public void monitor() {
        logger.info("开始监测设备状态...");
        Cursor<Map.Entry<Object, Object>> cursor = stringRedisTemplate.opsForHash()
                .scan(GATEWAY_ALL, ScanOptions.scanOptions().build());
        while(cursor.hasNext()) {
            Map.Entry<Object, Object> next = cursor.next();
            String gatewayId = (String) next.getKey();
            String val = (String) next.getValue();
            if(StringUtils.isBlank(val))
                continue;

            List<DeviceNumEuiDto> devices = JSON.parseArray(val,
                    DeviceNumEuiDto.class);
            List<AlarmDataEntity> list = new ArrayList<>();
            Map<String, String> alarmDataMap = new HashMap<>();
            for (DeviceNumEuiDto device : devices) {
                String devEUI = device.getDevEUI();
                long currentTime = System.currentTimeMillis();
                Object lastUpdated = stringRedisTemplate.opsForHash().get(REAL_HIS_DATA_STORE_UP_TO_DATE, devEUI);
                if(lastUpdated != null && currentTime - Long.parseLong((String) lastUpdated) > HEART_BEAT) {
                    AlarmDataEntity alarmData = generateAlarmData(device,
                            currentTime - Long.parseLong((String) lastUpdated), gatewayId);
                    list.add(alarmData);
                    String key = alarmData.getTerminalId() + "_0_16";
                    alarmDataMap.put(key, JSON.toJSONString(alarmData));
                }
            }
            alarmDataService.saveAlarmDataEntityList(list);
            stringRedisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
        }
    }

    private AlarmDataEntity generateAlarmData(DeviceNumEuiDto deviceNumEuiDto, long delta, String gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        alarmData.setAlarmContent("第[" + deviceNumEuiDto.getBoxNo() + "]号终端超过" + delta + "ms未连接!");
        alarmData.setAlarmStatus("1");
        alarmData.setSwitchAddr("0");
        alarmData.setAlarmType(DEVICE_NO_SIGNAL);
        alarmData.setAlarmLevel("3");
        alarmData.setProjectId(deviceNumEuiDto.getProjectId());
        alarmData.setTerminalId(Integer.toString(deviceNumEuiDto.getBoxNo()));
        alarmData.setGatewayId(gatewayId);
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }
}
