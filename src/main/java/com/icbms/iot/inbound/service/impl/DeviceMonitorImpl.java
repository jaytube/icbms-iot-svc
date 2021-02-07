package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.GatewayStatusDto;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.entity.GatewayDeviceMap;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.DeviceMonitor;
import com.icbms.iot.mapper.GatewayDeviceMapMapper;
import com.icbms.iot.mapper.GatewayInfoMapper;
import com.icbms.iot.rest.LoRaCommandService;
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
import java.util.*;
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

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Override
    @Scheduled(fixedDelay = MONITOR_DEVICE_FREQUENCY)
    @Transactional
    public void monitorDevice() {
        logger.info("开始监测设备状态...");
        List<GatewayDeviceMap> all = gatewayDeviceMapMapper.findAll();
        if(CollectionUtils.isEmpty(all))
            return;

        Map<Integer, List<GatewayDeviceMap>> map = all.stream().distinct().collect(Collectors.groupingBy(GatewayDeviceMap::getGatewayId));
        for (Map.Entry<Integer, List<GatewayDeviceMap>> cursor : map.entrySet()) {
            String gatewayId = Integer.toString(cursor.getKey());
            List<GatewayDeviceMap> val = cursor.getValue();
            if(CollectionUtils.isEmpty(val))
                continue;

            List<AlarmDataEntity> list = new ArrayList<>();
            Map<String, String> alarmDataMap = new HashMap<>();
            for (GatewayDeviceMap device : val) {
                String hashKey = TerminalBoxConvertUtil.getTerminalNo(device.getDeviceBoxNum()) + "_100";
                long currentTime = System.currentTimeMillis();
                Object lastUpdated = stringRedisTemplate.opsForHash().get(REAL_HIS_DATA_STORE_UP_TO_DATE, hashKey);
                if(lastUpdated != null && currentTime - Long.parseLong((String) lastUpdated) > HEART_BEAT) {
                    AlarmDataEntity alarmData = generateDeviceAlarmData(device,
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

    @Override
    @Scheduled(fixedDelay = MONITOR_GATEWAY_FREQUENCY)
    @Transactional
    public void monitorGateway() {
        List<GatewayInfo> gatewayList = gatewayInfoMapper.findAll();
        if(CollectionUtils.isEmpty(gatewayList))
            return;

        List<AlarmDataEntity> list = new ArrayList<>();
        Map<String, String> alarmDataMap = new HashMap<>();
        for (GatewayInfo gateway : gatewayList) {
            String ip = gateway.getIpAddress();
            int id = gateway.getGatewayId();
            CommonResponse<List<GatewayInfo>> resp = loRaCommandService.getGatewayList(ip);
            if(resp.getCode() != 200) {
                String dto = (String)stringRedisTemplate.opsForHash().get(GATEWAY_STATUS, id);
                long delta = MONITOR_GATEWAY_FREQUENCY;
                if(dto != null) {
                    GatewayStatusDto gatewayStatusDto = JSON.parseObject(dto, GatewayStatusDto.class);
                    Date lastReportDate = DateUtil.parse(gatewayStatusDto.getReportTime(), "yyyy-MM-dd HH:mm:ss");
                    long lastReportTime = lastReportDate.getTime();
                    delta = System.currentTimeMillis() - lastReportTime;
                }
                AlarmDataEntity alarmData = generateGatewayAlarmData(delta, id);
                list.add(alarmData);
                GatewayStatusDto statusDto = generateGatewayStatusDto(alarmData.getAlarmContent(), alarmData.getReportTime(), 1);
                alarmDataMap.put(Integer.toString(id), JSON.toJSONString(statusDto));
            } else {
                String dto = (String)stringRedisTemplate.opsForHash().get(GATEWAY_STATUS, id);
                if(dto != null) {
                    GatewayStatusDto gatewayStatusDto = JSON.parseObject(dto, GatewayStatusDto.class);
                    if(gatewayStatusDto.getStatus() == 1) {
                        AlarmDataEntity alarmData = generateGatewayRecoverAlarmData(id);
                        list.add(alarmData);
                        GatewayStatusDto statusDto = generateGatewayStatusDto(alarmData.getAlarmContent(), alarmData.getReportTime(), 0);
                        alarmDataMap.put(Integer.toString(id), JSON.toJSONString(statusDto));
                    }
                }
            }
        }

        alarmDataService.saveAlarmDataEntityList(list);
        stringRedisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
    }

    private AlarmDataEntity generateDeviceAlarmData(GatewayDeviceMap deviceNumEuiDto, long delta, String gatewayId) {
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

    private GatewayStatusDto generateGatewayStatusDto(String msg, String reportDate, int status) {
        GatewayStatusDto gatewayStatusDto = new GatewayStatusDto();
        gatewayStatusDto.setMsg(msg);
        gatewayStatusDto.setReportTime(reportDate);
        gatewayStatusDto.setStatus(status);

        return gatewayStatusDto;
    }

    private AlarmDataEntity generateGatewayAlarmData(long delta, Integer gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        alarmData.setAlarmContent("第[" + gatewayId + "]号网关超过" + delta + "ms未连接!");
        alarmData.setAlarmStatus("1");
        alarmData.setAlarmType("网关通信中断");
        alarmData.setAlarmLevel("3");
        alarmData.setProjectId(Integer.toString(gatewayId));
        alarmData.setGatewayId(Integer.toString(gatewayId));
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }

    private AlarmDataEntity generateGatewayRecoverAlarmData(Integer gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        alarmData.setAlarmContent("第[" + gatewayId + "]号网关恢复正常状态!");
        alarmData.setAlarmStatus("0");
        alarmData.setAlarmType("网关通信中断");
        alarmData.setAlarmLevel("3");
        alarmData.setProjectId(Integer.toString(gatewayId));
        alarmData.setGatewayId(Integer.toString(gatewayId));
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }
}
