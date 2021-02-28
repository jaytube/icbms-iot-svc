package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RichMqttMessage;

public interface MqttMsgWorker {

    void processRealtimeMsg(String gatewayId);

    void processAlarmMsg();

    void processStopMsg(RichMqttMessage mqttMsg);

}
