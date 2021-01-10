package com.icbms.iot.inbound.service;

public abstract class AbstractMessageProcessor implements IBaseMessageProcessor, InBoundMessageMaster {

    @Override
    public void performExecute() {
        decode();
        parse();
        execute();
        postExecute();
    }
}
