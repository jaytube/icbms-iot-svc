package com.icbms.iot.controller;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.entity.DeviceAlarmInfoLog;
import com.icbms.iot.mapper.DeviceAlarmInfoLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    private MqttPushClient mqttPushClient;

    @Autowired
    private DeviceAlarmInfoLogMapper alarmMapper;

    @GetMapping("/mybatis/{id}")
    public DeviceAlarmInfoLog findById(@PathVariable("id") String id) {
        return alarmMapper.findById(id);
    }

    @GetMapping("/publishTopic")
    public String publishTopic() {
        String topicString = "test";
        mqttPushClient.publish(0, false, topicString, "Test posting");
        return "ok";
    }
    // Send custom message content (using default theme)
    @GetMapping("/publishTopic/{data}")
    public String test1(@PathVariable("data") String data) {
        String topicString = "test";
        mqttPushClient.publish(0,false, topicString, data);
        return "ok";
    }

    // Send custom message content and specify subject
    @GetMapping("/publishTopic/{topic}/{data}")
    public String test2(@PathVariable("topic") String topic, @PathVariable("data") String data) {
        mqttPushClient.publish(0,false,topic, data);
        return "ok";
    }
}
