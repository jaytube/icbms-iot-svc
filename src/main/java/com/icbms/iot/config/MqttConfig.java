package com.icbms.iot.config;

import com.icbms.iot.client.MqttPushClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("mqtt")
@Data
public class MqttConfig {

    @Autowired
    private MqttPushClient mqttPushClient;

    private String username;

    private String password;

    private String hostUrl;

    private String clientID;

    private String topic;

    private int timeout;

    private int keepAlive;

    @Bean
    public MqttPushClient getMqttPushClient() {
        System.out.println("hostUrl: "+ hostUrl);
        System.out.println("clientID: "+ clientID);
        System.out.println("username: "+ username);
        System.out.println("password: "+ password);
        System.out.println("timeout: "+timeout);
        System.out.println("keepalive: "+ keepAlive);
        mqttPushClient.connect(hostUrl, clientID, username, password, timeout, keepAlive);
        // End with / / to subscribe to all topics starting with test
        mqttPushClient.subscribe(topic, 0);
        return mqttPushClient;
    }
}
