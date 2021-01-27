package com.icbms.iot.inbound.service.impl;

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
import com.icbms.iot.util.MqttEnvUtil;
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
    private MqttEnvUtil mqttEnvUtil;

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
                if(!inboundStopMsgQueue.isEmpty()) {
                    RichMqttMessage stopMsg = inboundStopMsgQueue.poll();
                    String gatewayId = stopMsg.getGatewayId();
                    mqttEnvUtil.setSingleGatewayStopped(true);
                    logger.info("收到停止网关: " + gatewayId + "轮询消息, 开始关闭轮询 。。。");
                }
                if(!alarmDataMsgQueue.isEmpty()) {
                    RealtimeMessage alarmData = alarmDataMsgQueue.poll();
                    alarmDataService.processAlarmData(alarmData);
                }
                if(realtimeMsgQueue.size() >= REAL_DATA_PROCESS_CAPACITY) {
                    List<RealtimeMessage> msgList = new ArrayList<>();
                    IntStream.range(0, realtimeMsgQueue.size())
                            .forEach(i -> msgList.add(realtimeMsgQueue.poll()));

                    realtimeDataService.processRealtimeData(msgList);
                }
            } catch(Exception e) {
                logger.error("消息处理错误: {}", e);
            }
        }
    }

}
