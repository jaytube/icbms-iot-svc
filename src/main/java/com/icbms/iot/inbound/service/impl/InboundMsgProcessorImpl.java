package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import com.icbms.iot.inbound.service.InboundMsgProcessor;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class InboundMsgProcessorImpl implements InboundMsgProcessor {

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Autowired
    private MqttMsgWorker mqttMsgWorker;

    @Override
    public void processMqttRealtimeMsg(RichMqttMessage msg) {
        realTimeProcessMaster.setParameter(msg);
        realTimeProcessMaster.performExecute();
        String gatewayId = msg.getGatewayId();
        mqttMsgWorker.processRealtimeMsg(gatewayId);
        mqttMsgWorker.processAlarmMsg();
    }

    @Override
    public void processMqttStopMsg(RichMqttMessage msg) {
        mqttMsgWorker.processStopMsg(msg);
    }
}
