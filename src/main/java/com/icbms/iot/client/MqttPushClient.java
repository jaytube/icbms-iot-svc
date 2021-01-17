package com.icbms.iot.client;

import com.icbms.iot.inbound.PushCallback;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class MqttPushClient {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PushCallback pushCallback;

    public static MqttClient client;

    /**
     * Client connection
     *
     * @param host      ip+port
     * @param clientID  Client Id
     * @param username  User name
     * @param password  Password
     * @param timeout   Timeout time
     * @param keepAlive Retention number
     */
    public void connect(String host, String clientID, String username, String password, int timeout, int keepAlive) throws Exception {
        logger.info("mqtt client 开始连接 ");
        try {
            if (client == null) {
                client = new MqttClient(host, clientID, new MemoryPersistence());
                client.setCallback(pushCallback);
                logger.info("pushcallback: " + pushCallback);
            }
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(keepAlive);
            options.setAutomaticReconnect(true);
            client.connect(options);
            logger.info("连接成功！");
        } catch (Exception e) {
            logger.error("mqtt: 连接失败", e);
            throw e;
        }
    }


    /**
     * Release
     *
     * @param qos         Connection mode
     * @param retained    Whether to retain
     * @param topic       theme
     * @param pushMessage Message body
     */
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = client.getTopic(topic);
        if (null == mTopic) {
            logger.error("topic not exist");
        }
        MqttDeliveryToken token;
        try {
            token = mTopic.publish(message);
            token.waitForCompletion();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to a topic
     *
     * @param topic theme
     * @param qos   Connection mode
     */
    public void subscribe(String topic, int qos) {
        logger.info("Start subscribing to topics " + topic);
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            logger.error("subscribe topic");
            e.printStackTrace();
        }
    }

    public void setPushCallback(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }
}
