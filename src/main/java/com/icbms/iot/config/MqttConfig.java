package com.icbms.iot.config;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.inbound.PushCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ConfigurationProperties("mqtt")
@Data
@Slf4j
public class MqttConfig {

    private String username;

    private String password;

    private String hostUrl;

    private String realtimeTopic;

    private String stopTopic;

    private int timeout;

    private int keepAlive;

    //private String clientId;

    @Autowired
    private PushCallback pushCallback;

    @Bean
    public MqttPushClient getMqttPushClient() {
        log.info("hostUrl: " + hostUrl);
        log.info("username: " + username);
        log.info("password: " + password);
        log.info("timeout: " + timeout);
        log.info("topic: " + realtimeTopic);
        log.info("topic: " + stopTopic);
        log.info("keepalive: " + keepAlive);
        String clientId = UUID.randomUUID().toString();
        log.info("clientId: " + clientId);
        MqttPushClient mqttPushClient = new MqttPushClient();
        try {
            mqttPushClient.setPushCallback(pushCallback);
            pushCallback.setMqttPushClient(mqttPushClient);
            mqttPushClient.connect(hostUrl, clientId, username, password, timeout, keepAlive);
        } catch (Exception e) {
            log.error("连接mqtt server 失败！", e);
        }
        // End with / / to subscribe to all topics starting with test
        int[] Qos = {0, 2};
        String[] topics = {realtimeTopic, stopTopic};
        mqttPushClient.subscribe(topics, Qos);
        return mqttPushClient;
    }

}
