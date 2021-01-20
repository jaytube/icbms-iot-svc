package com.icbms.iot.inbound.service;


import com.icbms.iot.dto.RichMqttMessage;

public interface InBoundMessageMaster {

    void setParameter(RichMqttMessage msg);
    void performExecute();

}
