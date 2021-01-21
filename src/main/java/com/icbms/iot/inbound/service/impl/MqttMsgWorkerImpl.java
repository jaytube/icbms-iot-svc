package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.inbound.component.ProcessedMsgQueue;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private AlarmDataService alarmDataService;


    @Override
    @Async
    public void processMsg() {
        while(true) {
            try {
                if (!inboundMsgQueue.isEmpty()) {
                    RichMqttMessage mqttMsg = inboundMsgQueue.poll();
                    realTimeProcessMaster.setParameter(mqttMsg);
                    realTimeProcessMaster.performExecute();
                }
                if(processedMsgQueue.size() >= PROCESS_CAPACITY) {
                    List<RealtimeMessage> msgList = new ArrayList<>();
                    IntStream.rangeClosed(0, processedMsgQueue.size())
                            .forEach(i -> msgList.add(processedMsgQueue.poll()));

                    //TODO redis store data
                    alarmDataService.processAlarmData(msgList);

                }
            } catch(Exception e) {}
        }
    }

}
