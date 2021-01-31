package com.icbms.iot.inbound;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.common.service.GatewayConfigService;
import com.icbms.iot.config.MqttConfig;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.inbound.component.InboundStopMsgQueue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.icbms.iot.constant.IotConstant.REAL_TIME;
import static com.icbms.iot.constant.IotConstant.STOP;

@Component
public class PushCallback implements MqttCallback {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MqttPushClient mqttPushClient;

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private InboundMsgQueue inboundMsgQueue;

    @Autowired
    private InboundStopMsgQueue inboundStopMsgQueue;
    
    @Autowired
    private GatewayConfigService gatewayConfigService;

    @Override
    public void connectionLost(Throwable throwable) {
        // After the connection is lost, it is usually reconnected here
        logger.info(throwable.getMessage());
        logger.info("[MQTT] 连接断开，5S之后尝试重连...");
        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                reconnect();
                break;
            } catch (Exception e) {
                logger.error("[MQTT] 重连失败!", e);
                continue;
            }
        }
    }

    private void reconnect() throws Exception {
        logger.info("[MQTT] 开始重连!");
        mqttPushClient.connect(mqttConfig.getHostUrl(), UUID.randomUUID().toString(),
            mqttConfig.getUsername(), mqttConfig.getPassword(), mqttConfig.getTimeout(), mqttConfig.getKeepAlive());
        logger.info("[MQTT] 重连成功!");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        // The message you get after you subscribe will be executed here
        logger.info("订阅主题: " + topic);
        if(StringUtils.isEmpty(topic))
            return;

        logger.info("消息内容: " + new String(mqttMessage.getPayload()));
        String gatewayId = topic.split("\\/")[1];
        try {
            if(topic.contains(REAL_TIME))
                inboundMsgQueue.offer(new RichMqttMessage(gatewayId, mqttMessage));
            else if(topic.contains(STOP))
                inboundStopMsgQueue.offer(new RichMqttMessage(gatewayId, mqttMessage));
        } catch (IotException e) {
            logger.info("数据格式错误...");
        } catch (Exception ex) {
            logger.error("数据处理失败: ", ex);
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
