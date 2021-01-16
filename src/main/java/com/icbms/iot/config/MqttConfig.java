package com.icbms.iot.config;

import com.icbms.iot.client.MqttPushClient;
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

    @Autowired
    private MqttPushClient mqttPushClient;

    private String username;

    private String password;

    private String hostUrl;

    private String topic;

    private int timeout;

    private int keepAlive;

    @Bean
    public MqttPushClient getMqttPushClient() {
        log.info("hostUrl: " + hostUrl);
        log.info("username: " + username);
        log.info("password: " + password);
        log.info("timeout: " + timeout);
        log.info("topic: " + topic);
        log.info("keepalive: " + keepAlive);
        String clientID = UUID.randomUUID().toString();
        mqttPushClient.connect(hostUrl, clientID, username, password, timeout, keepAlive);
        // End with / / to subscribe to all topics starting with test
        mqttPushClient.subscribe(topic, 0);
        return mqttPushClient;
    }
}
