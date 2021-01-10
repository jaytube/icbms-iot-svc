package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RealTimeMessage;

public interface RealTimeMessageParser {

    RealTimeMessage parseMessage(byte[] message);
}
