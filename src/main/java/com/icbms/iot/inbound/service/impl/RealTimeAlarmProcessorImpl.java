package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.inbound.service.RealTimeAlarmProcessor;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RealTimeAlarmProcessorImpl implements RealTimeAlarmProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    private ThreadLocal<byte[]> payloadThreadLocal = new ThreadLocal<>();

    @Override
    public void resolveParameter(MqttMessage mqttMessage) {
        byte[] payload = mqttMessage.getPayload();
        payloadThreadLocal.set(payload);
    }

    @Override
    public void parsePayload() {

    }

    @Override
    public void saveAlarm() {

    }

    @Override
    public void postExecute() {
        payloadThreadLocal.remove();
    }
}
