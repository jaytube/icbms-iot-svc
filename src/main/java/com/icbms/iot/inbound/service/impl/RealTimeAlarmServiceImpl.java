package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.inbound.service.RealTimeAlarmProcessor;
import com.icbms.iot.inbound.service.RealTimeAlarmService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RealTimeAlarmServiceImpl implements RealTimeAlarmService {

    @Autowired
    private RealTimeAlarmProcessor realTimeAlarmProcessor;

    @Override
    public void processRealTimeAlarm(MqttMessage mqttMessage) {
        realTimeAlarmProcessor.resolveParameter(mqttMessage);
    }
}
