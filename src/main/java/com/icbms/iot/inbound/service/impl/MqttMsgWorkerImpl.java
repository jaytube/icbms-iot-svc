package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.inbound.component.AlarmDataMsgQueue;
import com.icbms.iot.inbound.component.RealtimeMsgQueue;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import com.icbms.iot.inbound.service.RealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class MqttMsgWorkerImpl implements MqttMsgWorker {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private RealtimeMsgQueue realtimeMsgQueue;

    @Autowired
    private AlarmDataMsgQueue alarmDataMsgQueue;

    @Autowired
    private AlarmDataService alarmDataService;

    @Autowired
    private RealtimeDataService realtimeDataService;


    @Override
    @Async
    public void processRealtimeMsg(String gatewayId) {
        List<RealtimeMessage> msgList = realtimeMsgQueue.pollBatch(gatewayId);
        logger.debug("线程: " + Thread.currentThread().getName() + " 开始处理实时数据...");
        realtimeDataService.processRealtimeData(msgList);
    }

    @Override
    @Async
    public void processAlarmMsg() {
        RealtimeMessage alarmData = alarmDataMsgQueue.poll();
        if(alarmData != null) {
            logger.debug("线程: " + Thread.currentThread().getName() + " 开始处理告警数据...");
            alarmDataService.processAlarmData(alarmData);
        }
    }

    @Override
    @Async
    public void processStopMsg(RichMqttMessage mqttMsg) {
        String gatewayId = mqttMsg.getGatewayId();
        List<RealtimeMessage> msgList = realtimeMsgQueue.pollAll(gatewayId);
        logger.debug("线程: " + Thread.currentThread().getName() + " 开始处理停止后剩下的实时数据...");
        realtimeDataService.processRealtimeData(msgList);
    }
}
