package com.icbms.iot.controller;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.client.HttpTest;
import com.icbms.iot.client.HttpTestStart;
import com.icbms.iot.client.HttpTestStop;
import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.config.MqttConfig;
import com.icbms.iot.entity.DeviceAlarmInfoLog;
import com.icbms.iot.mapper.DeviceAlarmInfoLogMapper;
import com.icbms.iot.ssl.ApiResult;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class TestController {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    public String testConn(@RequestBody MqttConfig config) throws Exception{
        mqttPushClient.connect(config.getHostUrl(), System.currentTimeMillis() + "", config.getUsername(), config.getPassword(), 100, 100);
        mqttPushClient.subscribe("mqttPushClient", 0);
        return "ok";
    }

    @GetMapping("/http/{command}/{deviceid}")
    public void test(@PathVariable("command") String command, @PathVariable("deviceid") String deviceid) throws Exception {
        HttpTest.test(command, deviceid);
    }

    @GetMapping("/stop")
    public void test2() throws Exception {
        HttpTestStop.test();
    }

    @GetMapping("/start")
    public void test3() throws Exception {
        HttpTestStart.test();
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
        ApiResult apiResult = new ApiResult();
        try {
            apiResult.setData(HttpTest.doPost(model.getUrl(), model.getJson()));
            apiResult.setSuccess(true);
            return apiResult;
        } catch (Exception e) {
            logger.error("send error", e);
        }

        apiResult.setSuccess(false);
        return apiResult;
    }
}
