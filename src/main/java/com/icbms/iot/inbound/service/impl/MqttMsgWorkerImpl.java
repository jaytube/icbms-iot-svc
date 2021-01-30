package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.component.GatewayKeeper;
import com.icbms.iot.dto.GatewayDto;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.inbound.component.AlarmDataMsgQueue;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.inbound.component.InboundStopMsgQueue;
import com.icbms.iot.inbound.component.RealtimeMsgQueue;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import com.icbms.iot.inbound.service.RealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static com.icbms.iot.constant.IotConstant.REAL_DATA_PROCESS_CAPACITY;

@Service
public class MqttMsgWorkerImpl implements MqttMsgWorker {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private InboundMsgQueue inboundMsgQueue;

    @Autowired
    private InboundStopMsgQueue inboundStopMsgQueue;

    @Autowired
    private RealtimeMsgQueue realtimeMsgQueue;

    @Autowired
    private AlarmDataMsgQueue alarmDataMsgQueue;

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Autowired
    private AlarmDataService alarmDataService;

    @Autowired
    private RealtimeDataService realtimeDataService;

    @Autowired
    private GatewayKeeper gatewayKeeper;

    @Autowired
    private Executor taskExecutor;

    @Override
    @Async
    public void processMsg() {
        while(true) {
            try {
                process();
            } catch(Exception e) {
                logger.error("消息处理错误: {}", e);
            }
        }
    }

    private void process() {

        CompletableFuture.runAsync(() -> {
            if (!inboundMsgQueue.isEmpty()) {
                RichMqttMessage mqttMsg = inboundMsgQueue.poll();
                realTimeProcessMaster.setParameter(mqttMsg);
                realTimeProcessMaster.performExecute();
            }
        }, taskExecutor);

        CompletableFuture.runAsync(() -> {
            if(!inboundStopMsgQueue.isEmpty()) {
                RichMqttMessage stopMsg = inboundStopMsgQueue.poll();
                String gatewayId = stopMsg.getGatewayId();
                GatewayDto gateway = gatewayKeeper.getById(Integer.parseInt(gatewayId));
                gateway.setFinished(true);
                logger.info("收到停止网关: " + gatewayId + "轮询消息, 开始关闭轮询 。。。");
            }
        }, taskExecutor);

        CompletableFuture.runAsync(() -> {
            if(!alarmDataMsgQueue.isEmpty()) {
                logger.info("线程: " + Thread.currentThread().getName() + " 开始处理告警数据...");
                RealtimeMessage alarmData = alarmDataMsgQueue.poll();
                alarmDataService.processAlarmData(alarmData);
            }
        }, taskExecutor);

        CompletableFuture.runAsync(() -> {
            if(realtimeMsgQueue.size() >= REAL_DATA_PROCESS_CAPACITY) {
                logger.info("线程: " + Thread.currentThread().getName() + " 开始处理实时数据...");
                List<RealtimeMessage> msgList = new ArrayList<>();
                IntStream.range(0, realtimeMsgQueue.size())
                        .forEach(i -> msgList.add(realtimeMsgQueue.poll()));

                realtimeDataService.processRealtimeData(msgList);
            }
        }, taskExecutor);
    }

}
