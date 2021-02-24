package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RichMqttMessage;

public interface InboundMsgProcessor {

    void processMqttRealtimeMsg(RichMqttMessage msg);

    void processMqttStopMsg(RichMqttMessage msg);

}
