package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.common.service.DeviceInfoService;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.dto.TerminalStatusDto;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.entity.DeviceAlarmInfoLog;
import com.icbms.iot.entity.DeviceBoxInfo;
import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.mapper.DeviceAlarmInfoLogMapper;
import com.icbms.iot.mapper.DeviceBoxInfoMapper;
import com.icbms.iot.util.CommonUtil;
import com.icbms.iot.util.DateUtil;
import com.icbms.iot.util.RestUtil;
import com.icbms.iot.util.TerminalStatusUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.*;
import static com.icbms.iot.util.TerminalBoxConvertUtil.getTerminalNo;

@Service
public class AlarmDataServiceImpl implements AlarmDataService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DeviceBoxInfoMapper deviceBoxInfoMapper;

    @Autowired
    private DeviceAlarmInfoLogMapper deviceAlarmInfoLogMapper;

    @Value("${icbms.alarm.url}")
    private String alarmUrl;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private DeviceInfoService deviceInfoService;

    public Map<String, Object> generateAlarmData(RealtimeMessage realtimeMessage) {
        List<AlarmDataEntity> list = new ArrayList<>();
        List<AlarmType> alarmTypes = realtimeMessage.getAlarmTypes();
        String boxNo = realtimeMessage.getBoxNo() + "";
        String projectId = deviceInfoService.getProjectIdByDeviceNo(boxNo);
        Map<String, String> map = new HashMap<>();
        if(CollectionUtils.isEmpty(alarmTypes)) {
            AlarmType[] alarmTypeArr = AlarmType.values();
            for (int i=0; i<alarmTypeArr.length; i++) {
                AlarmType alarmType = alarmTypeArr[i];
                String jsonStr = (String) redisTemplate.opsForHash().get(ALARM_DATA, boxNo + "_100_" + (15 - i));
                if (StringUtils.isNotBlank(jsonStr)) {
                    AlarmDataEntity alarmEntity = JSON.parseObject(jsonStr, AlarmDataEntity.class);
                    if (!"0".equals(alarmEntity.getAlarmStatus())) {
                        alarmEntity.setAlarmStatus("0");
                        alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                        alarmEntity.setProjectId(projectId);
                        alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                        alarmEntity.setTerminalId(boxNo);
                        alarmEntity.setSwitchAddr("100");
                        alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                        alarmEntity.setAlarmContent(alarmType.getAlarmContent() + "恢复");
                        alarmEntity.setAlarmType(alarmType.getAlarmContent());
                        map.put(boxNo + "_100_" + (15 - i), JSON.toJSONString(alarmEntity));
                        list.add(alarmEntity);
                    }
                }
            }
        } else {
            for (AlarmType alarmType : alarmTypes) {
                int code = alarmType.getCode();
                String jsonStr = (String) redisTemplate.opsForHash().get(ALARM_DATA, boxNo + "_100_" + (15 - code));
                if (StringUtils.isNotBlank(jsonStr)) {
                    AlarmDataEntity alarmEntity = JSON.parseObject(jsonStr, AlarmDataEntity.class);
                    if (!"1".equals(alarmEntity.getAlarmStatus())) {
                        alarmEntity.setAlarmStatus("1");
                        alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                        alarmEntity.setProjectId(projectId);
                        alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                        alarmEntity.setTerminalId(boxNo);
                        alarmEntity.setSwitchAddr("100");
                        alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                        alarmEntity.setAlarmContent(alarmType.getAlarmContent());
                        alarmEntity.setAlarmType(alarmType.getAlarmContent());
                        map.put(boxNo + "_100_" + (15 - code), JSON.toJSONString(alarmEntity));
                        list.add(alarmEntity);
                    }
                } else {
                    AlarmDataEntity alarmEntity = new AlarmDataEntity();
                    alarmEntity.setAlarmStatus("1");
                    alarmEntity.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                    alarmEntity.setProjectId(projectId);
                    alarmEntity.setGatewayId(realtimeMessage.getGatewayId());
                    alarmEntity.setTerminalId(boxNo);
                    alarmEntity.setSwitchAddr("100");
                    alarmEntity.setAlarmLevel(Objects.toString(alarmType.getLevel(), ""));
                    alarmEntity.setAlarmContent(alarmType.getAlarmContent());
                    alarmEntity.setAlarmType(alarmType.getAlarmContent());
                    map.put(boxNo + "_100_" + (15 - code), JSON.toJSONString(alarmEntity));
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
    @Transactional
    public List<DeviceAlarmInfoLog> saveAlarmDataEntityList(List<AlarmDataEntity> list) {
        if(CollectionUtils.isEmpty(list))
            return new ArrayList<>();

        List<String> projectIdList = list.stream().filter(Objects::nonNull).map(AlarmDataEntity::getProjectId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DeviceBoxInfo> deviceBoxes = deviceBoxInfoMapper.findByProjectIdList(projectIdList);
        //List<DeviceBoxInfo> deviceBoxes = Arrays.asList(mockDeviceBoxInfo());
        List<String> deviceBoxNums = list.stream().filter(Objects::nonNull).map(AlarmDataEntity::getTerminalId).distinct().collect(Collectors.toList());
        deviceBoxes = deviceBoxes.stream().filter(l -> deviceBoxNums.contains(getTerminalNo(l.getDeviceBoxNum())))
                .collect(Collectors.toList());
        Map<String, DeviceBoxInfo> deviceBoxMap = new HashMap<>();
        for (DeviceBoxInfo deviceBox : deviceBoxes) {
            deviceBoxMap.put(getTerminalNo(deviceBox.getDeviceBoxNum()), deviceBox);
        }
        List<DeviceAlarmInfoLog> logs = list.stream().map(l -> {
            DeviceAlarmInfoLog log = new DeviceAlarmInfoLog();
            log.setId(CommonUtil.uuid());
            log.setAlarmStatus(l.getAlarmStatus());
            log.setAlarmLevel(l.getAlarmLevel());
            log.setCreateTime(new Date());
            log.setUpdateTime(new Date());
            log.setRecordTime(DateUtil.parse(l.getReportTime(), "yyyy-MM-dd HH:mm:ss"));
            log.setDeviceBoxId(l.getTerminalId());
            DeviceBoxInfo info = deviceBoxMap.get(l.getTerminalId());
            if(info == null)
                return null;
            log.setDeviceBoxMac(info.getDeviceBoxNum());
            log.setDeviceBoxId(info.getId());
            log.setProjectId(info.getProjectId());
            int i = Integer.parseInt(l.getSwitchAddr()) + 1;
            log.setNode(CIRCUIT + i);
            log.setType(l.getAlarmType());
            log.setRemark(l.getAlarmContent());
            log.setInfo(l.getAlarmContent());
            return log;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if(CollectionUtils.isNotEmpty(logs)) {
            int successCount = deviceAlarmInfoLogMapper.batchInsert(logs);
            logger.info("插入表device_alarm_info_log" + successCount + "条告警数据！");
            return logs;
        }

        return new ArrayList<>();
    }

    @Override
    public void processAlarmData(RealtimeMessage msg) {
        if(msg == null)
            return;
        Map<String, String> alarmDataMap = new HashMap<>();
        List<AlarmDataEntity> alarmDataList = new ArrayList<>();
        Map<String, Object> map = generateAlarmData(msg);
        Map<String, String> redisMap = (Map<String, String>) map.get(REDIS_ALARM);
        alarmDataMap.putAll(redisMap);
        List<AlarmDataEntity> list = (List<AlarmDataEntity>) map.get(MYSQL_ALARM);
        alarmDataList.addAll(list);
        redisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
        saveAndSendAlarms(alarmDataList);
    }


    @Override
    public void saveAndSendAlarms(List<AlarmDataEntity> alarmDataList) {
        List<DeviceAlarmInfoLog> logs = saveAlarmDataEntityList(alarmDataList);
        sendAlarm(logs);
    }

    private void sendAlarm(List<DeviceAlarmInfoLog> logs) {
        if(CollectionUtils.isNotEmpty(logs)) {
            for (DeviceAlarmInfoLog log : logs) {
                if(log == null)
                    continue;
                CompletableFuture.runAsync(() -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", log.getId());
                    CommonResponse<Map> resp = restUtil.doPlainPost(alarmUrl, map);
                    if(resp.getCode() != HttpStatus.OK.value())
                        logger.error("发送告警数据失败，id: {}", log.getId());
                }, taskExecutor);
            }
        }
    }

}
