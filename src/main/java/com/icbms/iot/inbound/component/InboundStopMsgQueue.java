package com.icbms.iot.inbound.component;

import com.icbms.iot.dto.RichMqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class InboundStopMsgQueue {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ArrayBlockingQueue<RichMqttMessage> mqttStopMessageQueue = new ArrayBlockingQueue(1000);

    public void offer(RichMqttMessage mqttMessage) {
        this.mqttStopMessageQueue.offer(mqttMessage);
        logger.debug("入站停止轮询消息队列长度: " + this.mqttStopMessageQueue.size());
    }

    public RichMqttMessage poll() {
        try {
            return this.mqttStopMessageQueue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("获取停止消息超时, {}", e);
            return null;
        }
    }

    public int size() {
        return this.mqttStopMessageQueue.size();
    }

    public boolean isEmpty() {
        return this.mqttStopMessageQueue.isEmpty();
    }
}
