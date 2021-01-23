package com.icbms.iot.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MqttEnvUtil {

    private volatile AtomicInteger messageReceived = new AtomicInteger(0);

    private volatile AtomicBoolean mqttSwitchOff = new AtomicBoolean(false);

    private List<String> processedDeviceList = new CopyOnWriteArrayList<>();

    private volatile String currentGatewayId;

    private volatile boolean singleGatewayStopped;

    public synchronized String getCurrentGatewayId() {
        return currentGatewayId;
    }

    public synchronized void setCurrentGatewayId(String currentGatewayId) {
        this.currentGatewayId = currentGatewayId;
    }

    public int getMessageReceived() {
        return messageReceived.get();
    }

    public void increment() {
        this.messageReceived.incrementAndGet();
    }

    public synchronized void reset() {
        this.messageReceived = new AtomicInteger(0);
        this.mqttSwitchOff = new AtomicBoolean(false);
        this.processedDeviceList.clear();
        this.currentGatewayId = "";
    }

    public boolean isMqttSwitchOff() {
        return mqttSwitchOff.get();
    }

    public void setMqttSwitchOff(boolean mqttSwitchOff) {
        this.mqttSwitchOff = new AtomicBoolean(mqttSwitchOff);
    }

    public void addEle(String ele) {
        this.processedDeviceList.add(ele);
    }

    public List<String> getProcessedDeviceList() {
        return processedDeviceList;
    }

    public boolean isSingleGatewayStopped() {
        return singleGatewayStopped;
    }

    public void setSingleGatewayStopped(boolean singleGatewayStopped) {
        this.singleGatewayStopped = singleGatewayStopped;
    }
}
