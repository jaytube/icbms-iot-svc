package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.entity.DeviceAlarmInfoLog;

import java.util.List;
import java.util.Map;

public interface AlarmDataService {

    Map<String, Object> generateAlarmData(RealtimeMessage realTimeMessage);

    List<DeviceAlarmInfoLog> saveAlarmDataEntityList(List<AlarmDataEntity> list);

    void processAlarmData(RealtimeMessage realtimeMessage);

    void saveAndSendAlarms(List<AlarmDataEntity> alarmDataList);
}
