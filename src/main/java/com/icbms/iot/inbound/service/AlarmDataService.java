package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.entity.AlarmDataEntity;

import java.util.List;
import java.util.Map;

public interface AlarmDataService {

    Map<String, Object> generateAlarmData(RealtimeMessage realTimeMessage);

    void saveAlarmDataEntityList(List<AlarmDataEntity> list);

    void processAlarmData(List<RealtimeMessage> realTimeMessages);
}
