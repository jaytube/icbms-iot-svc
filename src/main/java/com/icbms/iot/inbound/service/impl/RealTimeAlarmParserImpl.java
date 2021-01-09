package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.exception.ErrorCodeEnum;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.inbound.service.RealTimeAlarmParser;
import com.icbms.iot.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RealTimeAlarmParserImpl implements RealTimeAlarmParser {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Override
    public void parseRealTimeData(byte[] payload) {
        this.logger.info("实时数据处理============>解析实时数据start!!!!");
        this.logger.info("]收到的数据域=============>[" + CommonUtil.bytesToHex(payload) + "]");
        int index = 0;
        Integer header = CommonUtil.getInt(payload, index);
        if(header == 14344)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_HEAD_INCORRECT);

        index += 4;
        int boxNumber = CommonUtil.getShort(payload, index);
        index += 2;
        int ram1 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram2 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram3 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram4 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram5 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram6 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram7 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram8 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram9 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram10 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram11 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram12 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram13 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram14 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram15 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram16 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram17 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram18 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram19 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram20 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram21 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram22 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram23 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram24 = CommonUtil.getShort(payload, index);
        index += 2;
        int ram25 = CommonUtil.getShort(payload, index);
        index += 2;
        long empty = CommonUtil.getLong(payload, index);
        index += 4;
        short addr1 = CommonUtil.getShort(payload, index);
        index += 2;
        short addr2 = CommonUtil.getShort(payload, index);
        index += 2;
        short addr3 = CommonUtil.getShort(payload, index);
        index += 2;
        short end = CommonUtil.getShort(payload, index);
        index += 2;
        if(end != 8198)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_END_INCORRECT);

        this.logger.info("实时数据处理============>解析实时数据end!!!!index应该等于"+index/2+"个字节");
    }
}
