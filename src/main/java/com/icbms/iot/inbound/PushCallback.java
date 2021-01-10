package com.icbms.iot.inbound;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PushCallback implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Override
    public void connectionLost(Throwable throwable) {
        // After the connection is lost, it is usually reconnected here
        logger.info("Disconnected, can be reconnected");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        // The message you get after you subscribe will be executed here
        logger.info("Receive message subject : " + topic);
        logger.info("receive messages Qos : " + mqttMessage.getQos());
        logger.info("Receive message content : " + new String(mqttMessage.getPayload()));
        realTimeProcessMaster.setParameter(mqttMessage);
        realTimeProcessMaster.performExecute();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }

}
