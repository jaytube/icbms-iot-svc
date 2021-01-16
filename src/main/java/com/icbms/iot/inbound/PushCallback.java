package com.icbms.iot.inbound;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.config.MqttConfig;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import org.apache.catalina.core.ApplicationContext;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

@Component
public class PushCallback implements MqttCallback {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private MqttConfig mqttConfig;

    @Override
    public void connectionLost(Throwable throwable) {
        // After the connection is lost, it is usually reconnected here
        logger.info(throwable.getMessage());
        logger.info("Disconnected, can be reconnected");
        logger.info("开始重连!");
        MqttPushClient mqttPushClient = beanFactory.getBean(MqttPushClient.class);
        logger.info("mqttPushclient: " + mqttPushClient);
        logger.info("mqttConfig: " + mqttConfig);
        mqttPushClient.connect(mqttConfig.getHostUrl(), UUID.randomUUID().toString(),
                mqttConfig.getUsername(), mqttConfig.getPassword(), mqttConfig.getTimeout(), mqttConfig.getKeepAlive());
        mqttPushClient.subscribe(mqttConfig.getTopic(), 0);
        logger.info("重连成功！");
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
