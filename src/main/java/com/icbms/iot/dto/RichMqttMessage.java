package com.icbms.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RichMqttMessage {

    private String gatewayId;
    private MqttMessage mqttMsg;

}
