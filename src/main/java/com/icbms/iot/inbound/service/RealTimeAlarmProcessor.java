package com.icbms.iot.inbound.service;


import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface RealTimeAlarmProcessor {

    void resolveParameter(MqttMessage mqttMessage);
    void parsePayload();
    void saveAlarm();
    void postExecute();

}
