package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RealtimeMessage;

public interface RealtimeMessageParser {

    RealtimeMessage parseMessage(byte[] message);
}
