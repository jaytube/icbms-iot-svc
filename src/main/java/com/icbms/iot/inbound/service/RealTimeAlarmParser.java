package com.icbms.iot.inbound.service;

public interface RealTimeAlarmParser {

    void parseRealTimeData(byte[] payload);
}
