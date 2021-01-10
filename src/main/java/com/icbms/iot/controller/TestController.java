package com.icbms.iot.controller;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.client.HttpTest;
import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.config.MqttConfig;
import com.icbms.iot.entity.DeviceAlarmInfoLog;
import com.icbms.iot.mapper.DeviceAlarmInfoLogMapper;
import com.icbms.iot.ssl.ApiResult;
import com.icbms.iot.ssl.SSLConnectionSocketUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    private MqttPushClient mqttPushClient;

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private DeviceAlarmInfoLogMapper alarmMapper;

    @GetMapping("/mybatis/{id}")
    public DeviceAlarmInfoLog findById(@PathVariable("id") String id) {
        return alarmMapper.findById(id);
    }

    @GetMapping("/publishTopic")
    public String publishTopic() {
        String topicString = "test";
        DeviceAlarmInfoLog alarmInfoLog = findById("01a4350c9d6a4f46a8f3026abfdc06d8");
        mqttPushClient.publish(0, false, mqttConfig.getTopic(), JSON.toJSONString(alarmInfoLog));
        return "ok";
    }

    @PostMapping("/testconn")
    public String testConn(@RequestBody MqttConfig config) {
        mqttPushClient.connect(config.getHostUrl(), config.getClientID(), config.getUsername(), config.getPassword(), 100, 100);
        mqttPushClient.subscribe("mqttPushClient", 0);
        return "ok";
    }

    @GetMapping("/http")
    public void test() throws Exception {
        HttpTest.test();
    }


    // Send custom message content (using default theme)
    @GetMapping("/publishTopic/{data}")
    public String test1(@PathVariable("data") String data) {
        mqttPushClient.publish(0, false, mqttConfig.getTopic(), data);
        return "ok";
    }

    // Send custom message content and specify subject
    @GetMapping("/publishTopic/{topic}/{data}")
    public String test2(@PathVariable("topic") String topic, @PathVariable("data") String data) {
        mqttPushClient.publish(0, false, mqttConfig.getTopic(), data);
        return "ok";
    }

    @PostMapping("/doPost")
    @ResponseBody
    public ApiResult testSendMessage(@RequestBody TestModel model) {
        try {
            return SSLConnectionSocketUtil.doGet(model.getUrl(), model.getJson(), model.getCode());
        } catch (Exception e) {
            logger.error("send error", e);
        }
        ApiResult apiResult = new ApiResult();
        apiResult.setSuccess(false);
        return apiResult;
    }
}
