package com.icbms.iot.inbound;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.config.MqttConfig;
import com.icbms.iot.dto.LoraMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.service.GatewayConfigService;
import com.icbms.iot.util.MqttEnvUtil;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

@Component
public class PushCallback implements MqttCallback {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MqttPushClient mqttPushClient;

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private MqttEnvUtil mqttEnvUtil;

    @Autowired
    private InboundMsgQueue inboundMsgQueue;

    @Autowired
    private GatewayConfigService gatewayConfigService;

    @Override
    public void connectionLost(Throwable throwable) {
        // After the connection is lost, it is usually reconnected here
        logger.info(throwable.getMessage());
        logger.info("[MQTT] 连接断开，5S之后尝试重连...");
        while(true) {
            try {
                Thread.sleep(5000);
                reconnect();
                logger.info("[MQTT] 重连成功!");
                break;
            } catch (Exception e) {
                logger.error("[MQTT] 重连失败!", e);
                continue;
            }
        }
    }

    private void reconnect() throws Exception {
        logger.info("开始重连!");
        mqttPushClient.connect(mqttConfig.getHostUrl(), UUID.randomUUID().toString(),
            mqttConfig.getUsername(), mqttConfig.getPassword(), mqttConfig.getTimeout(), mqttConfig.getKeepAlive());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        // The message you get after you subscribe will be executed here
        //logger.info("Receive message subject : " + topic);
        //logger.info("receive messages Qos : " + mqttMessage.getQos());
        LoraMessage loraMessage = JSON.parseObject(new String(mqttMessage.getPayload()), LoraMessage.class);
        String devEUI = loraMessage.getDevEUI();
        //mqttEnvUtil.addEle(loraMessage.getDevEUI());
        logger.info("消息来自devEUI: " + devEUI);
        try {
            mqttEnvUtil.increment();
            String gatewayId = gatewayConfigService.getGatewayIdByDevEUI(devEUI);
            inboundMsgQueue.offer(new RichMqttMessage(gatewayId, mqttMessage));
        } catch (IotException e) {
            logger.info("data format not correct ...");
        } catch (Exception ex) {
            logger.error("message process error: ", ex);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }

    public void setMqttPushClient(MqttPushClient mqttPushClient) {
        this.mqttPushClient = mqttPushClient;
    }
}
