package com.icbms.iot.client;

import com.icbms.iot.inbound.PushCallback;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class MqttPushClient {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private PushCallback pushCallback;

    private MqttClient client;

    private MqttClient getClient() {
        return client;
    }

    private void setClient(MqttClient client) {
        this.client = client;
    }

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
    public void connect(String host, String clientID, String username, String password, int timeout, int keepAlive) {
        MqttClient client;
        try {
            client = new MqttClient(host, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(keepAlive);
            this.setClient(client);
            client.setCallback(pushCallback);
            logger.info("pushcallback: " + pushCallback);
            client.connect(options);
            logger.info("mqtt 连接成功");
            /*client.subscribe(new String[] {topic}, new int[] {1});
            logger.info("mqtt topic " + topic + " 订阅成功！");*/
        } catch (Exception e) {
            logger.error("mqtt: 连接失败", e);
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
        MqttTopic mTopic = this.getClient().getTopic(topic);
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
        logger.info("Start subscribing to topics" + topic);
        try {
            this.getClient().subscribe(topic, qos);
        } catch (MqttException e) {
            logger.error("subscribe topic");
            e.printStackTrace();
        }
    }

}
