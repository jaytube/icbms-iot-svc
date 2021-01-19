package com.icbms.iot.inbound.component;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class InboundMsgQueue {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ArrayBlockingQueue<MqttMessage> mqttMessageQueue = new ArrayBlockingQueue(1000);

    public void offer(MqttMessage mqttMessage) {
        logger.info("入站队列长度: " + this.mqttMessageQueue.size());
        this.mqttMessageQueue.offer(mqttMessage);
    }

    public MqttMessage poll() {
        logger.info("入站队列长度: " + this.mqttMessageQueue.size());
        return this.mqttMessageQueue.poll();
    }

    public int size() {
        return this.mqttMessageQueue.size();
    }

    public boolean isEmpty() {
        return this.mqttMessageQueue.isEmpty();
    }
}
