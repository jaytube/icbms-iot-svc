package com.icbms.iot.async;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;

public class RealDataHandleThread implements Runnable {

    public ArrayBlockingQueue<MqttMessage> mqttMessageQueue = new ArrayBlockingQueue(1000);

    @Autowired

    @Override
    public void run() {
        while(true) {
            try {
                if (!mqttMessageQueue.isEmpty()) {

                }
            } catch(Exception e) {}
        }
    }
}
