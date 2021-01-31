package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.entity.GatewayDeviceMap;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.DeviceMonitor;
import com.icbms.iot.mapper.GatewayDeviceMapMapper;
import com.icbms.iot.util.DateUtil;
import com.icbms.iot.util.TerminalBoxConvertUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class DeviceMonitorImpl implements DeviceMonitor {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AlarmDataService alarmDataService;

    @Autowired
    private GatewayDeviceMapMapper gatewayDeviceMapMapper;

    @Override
    @Scheduled(fixedDelay = MONITOR_FREQUENCY)
    @Transactional
    public void monitor() {
        logger.info("开始监测设备状态...");
        List<GatewayDeviceMap> all = gatewayDeviceMapMapper.findAll();
        if(CollectionUtils.isEmpty(all))
            return;

        Map<Integer, List<GatewayDeviceMap>> map = all.stream().collect(Collectors.groupingBy(GatewayDeviceMap::getGatewayId));
        for (Map.Entry<Integer, List<GatewayDeviceMap>> cursor : map.entrySet()) {
            String gatewayId = Integer.toString(cursor.getKey());
            List<GatewayDeviceMap> val = cursor.getValue();
            if(CollectionUtils.isEmpty(val))
                continue;

            List<AlarmDataEntity> list = new ArrayList<>();
            Map<String, String> alarmDataMap = new HashMap<>();
            for (GatewayDeviceMap device : val) {
                String devEUI = device.getDeviceSn();
                long currentTime = System.currentTimeMillis();
                Object lastUpdated = stringRedisTemplate.opsForHash().get(REAL_HIS_DATA_STORE_UP_TO_DATE, devEUI);
                if(lastUpdated != null && currentTime - Long.parseLong((String) lastUpdated) > HEART_BEAT) {
                    AlarmDataEntity alarmData = generateAlarmData(device,
                            currentTime - Long.parseLong((String) lastUpdated), gatewayId);
                    list.add(alarmData);
                    String key = alarmData.getTerminalId() + "_100_16";
                    alarmDataMap.put(key, JSON.toJSONString(alarmData));
                }
            }
            alarmDataService.saveAlarmDataEntityList(list);
            stringRedisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
        }
    }

    private AlarmDataEntity generateAlarmData(GatewayDeviceMap deviceNumEuiDto, long delta, String gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        String boxNo = TerminalBoxConvertUtil.getTerminalNo(deviceNumEuiDto.getDeviceBoxNum());
        alarmData.setAlarmContent("第[" + boxNo + "]号终端超过" + delta + "ms未连接!");
        alarmData.setAlarmStatus("1");
        alarmData.setSwitchAddr("100");
        alarmData.setAlarmType(DEVICE_NO_SIGNAL);
        alarmData.setAlarmLevel("3");
        alarmData.setProjectId(deviceNumEuiDto.getProjectId());
        alarmData.setTerminalId(boxNo);
        alarmData.setGatewayId(gatewayId);
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }
}
