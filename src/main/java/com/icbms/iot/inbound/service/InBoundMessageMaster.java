package com.icbms.iot.inbound.service;


import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface InBoundMessageMaster {

    void setParameter(MqttMessage msg);
    void performExecute();

}
