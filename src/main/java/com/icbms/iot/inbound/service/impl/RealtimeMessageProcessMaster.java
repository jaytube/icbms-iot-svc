package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.dto.LoraMessage;
import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.exception.ErrorCodeEnum;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.inbound.component.AlarmDataMsgQueue;
import com.icbms.iot.inbound.component.RealtimeMsgQueue;
import com.icbms.iot.inbound.service.AbstractMessageProcessor;
import com.icbms.iot.inbound.service.RealtimeMessageParser;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service("realTimeMessageProcessMaster")
@Transactional
public class RealtimeMessageProcessMaster extends AbstractMessageProcessor {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ThreadLocal<RichMqttMessage> msgThreadLocal = new ThreadLocal<>();

    private ThreadLocal<LoraMessage> loraMsgThreadLocal = new ThreadLocal<>();

    private ThreadLocal<RealtimeMessage> realTimeMsgThreadLocal = new ThreadLocal<>();

    @Autowired
    private RealtimeMessageParser realtimeMessageParser;

    @Autowired
    private RealtimeMsgQueue realtimeMsgQueue;

    @Autowired
    private AlarmDataMsgQueue alarmDataMsgQueue;

    @Override
    public void setParameter(RichMqttMessage message) {
        msgThreadLocal.set(message);
    }

    @Override
    public void decode() {
        RichMqttMessage message = msgThreadLocal.get();
        if(message == null)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_NULL);

        String messageJson = new String(message.getMqttMsg().getPayload());
        String gatewayId = message.getGatewayId();
        LoraMessage loraMessage = JSON.parseObject(messageJson, LoraMessage.class);
        loraMessage.setGatewayId(gatewayId);
        logger.info("消息来自devEUI: " + loraMessage.getDevEUI() + ", 网关ID: " + message.getGatewayId());
        loraMsgThreadLocal.set(loraMessage);
    }

    @Override
    public void validate() {
        LoraMessage message = loraMsgThreadLocal.get();
        String dataStr = message.getData();
        byte[] data = Base64Util.decrypt(dataStr);
        logger.debug("实时告警数据长度：" + data.length);
        Integer header = CommonUtil.getInt(data, 0);
        String hexHeader = Integer.toHexString(header);
        logger.debug("数据头: " + hexHeader);
        if(!"3c430100".equalsIgnoreCase(hexHeader))
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_HEAD_INCORRECT);
    }

    @Override
    public void parse() {
        LoraMessage loraMessage = loraMsgThreadLocal.get();
        String dataStr = loraMessage.getData();
        byte[] data = Base64Util.decrypt(dataStr);
        RealtimeMessage realTimeMessage = realtimeMessageParser.parseMessage(data);
        realTimeMessage.setGatewayId(loraMessage.getGatewayId());
        realTimeMsgThreadLocal.set(realTimeMessage);
    }

    @Override
    public void execute() {
        RealtimeMessage realTimeMessage = realTimeMsgThreadLocal.get();
        realtimeMsgQueue.offer(realTimeMessage);
        alarmDataMsgQueue.offer(realTimeMessage);
    }

    @Override
    public void postExecute() {
        msgThreadLocal.remove();
        loraMsgThreadLocal.remove();
        realTimeMsgThreadLocal.remove();
    }
}
