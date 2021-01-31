package com.icbms.iot.inbound.service;

public interface GatewayRunner {

    void run(int id);
    void stop(int id);
}
