package com.icbms.iot.inbound.service;

public interface IBaseMessageProcessor {

    void decode();
    void parse();
    void execute();
    void postExecute();

}
