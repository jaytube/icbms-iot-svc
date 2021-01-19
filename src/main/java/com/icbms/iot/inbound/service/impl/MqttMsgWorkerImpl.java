package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RealTimeMessage;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.inbound.component.ProcessedMsgQueue;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class MqttMsgWorkerImpl implements MqttMsgWorker {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int PROCESS_CAPACITY = 10;

    @Autowired
    private InboundMsgQueue inboundMsgQueue;

    @Autowired
    private ProcessedMsgQueue processedMsgQueue;

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Autowired
    private RedisTemplate<String, RealTimeMessage> redisTemplate;

    @Override
    @Async
    public void processMsg() {
        while(true) {
            try {
                if (!inboundMsgQueue.isEmpty()) {
                    MqttMessage mqttMsg = inboundMsgQueue.poll();
                    realTimeProcessMaster.setParameter(mqttMsg);
                    realTimeProcessMaster.performExecute();
                }
                if(processedMsgQueue.size() >= PROCESS_CAPACITY) {
                    List<RealTimeMessage> list = new ArrayList<>();
                    IntStream.rangeClosed(0, processedMsgQueue.size())
                            .forEach(i -> list.add(processedMsgQueue.poll()));
                    //TODO redis store data
                }
            } catch(Exception e) {}
        }
    }

}
