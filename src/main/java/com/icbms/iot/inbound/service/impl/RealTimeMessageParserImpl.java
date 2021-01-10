package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.client.MqttPushClient;
import com.icbms.iot.dto.RealTimeMessage;
import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.enums.BoxAlarmType;
import com.icbms.iot.exception.ErrorCodeEnum;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.inbound.service.RealTimeMessageParser;
import com.icbms.iot.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RealTimeMessageParserImpl implements RealTimeMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Override
    public RealTimeMessage parseMessage(byte[] payload) {
        logger.info("实时数据处理============>解析实时数据start!!!!");
        logger.info("]收到的数据域=============>[" + CommonUtil.bytesToHex(payload) + "]");
        int index = 0;
        Integer header = CommonUtil.getInt(payload, index);
        index += 4;
        if(header != 82748)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_HEAD_INCORRECT);

        RealTimeMessage message = new RealTimeMessage();
        int boxNumber = CommonUtil.getShort(payload, index);
        index += 2;
        message.setBoxNo(boxNumber);

        int ram1 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setCircuitVoltage(ram1);

        int ram2 = CommonUtil.getShort(payload, index);
        index += 2;
        double currentLeak = ram2 * 0.0001;
        message.setCurrentLeak(currentLeak);

        int ram3 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setCircuitPower(ram3);

        int ram4 = CommonUtil.getShort(payload, index);
        index += 2;
        double modTemp = ram4 * 0.1;
        message.setModTemp(modTemp);

        int ram5 = CommonUtil.getShort(payload, index);
        index += 2;
        double circuitCurrent = ram5 * 0.01;
        message.setCircuitCurrent(circuitCurrent);

        int ram6 = CommonUtil.getShort(payload, index);
        AlarmType type = AlarmType.getByCode(ram6);
        index += 2;
        message.setAlarmType(type);

        int ram7 = CommonUtil.getShort(payload, index);
        index += 2;
        double lowEle = ram7 * 0.001;
        message.setLowElectric(lowEle);

        int ram8 = CommonUtil.getShort(payload, index);
        index += 2;
        double highEle = ram8 * 0.001;
        message.setHighElectric(highEle);

        int ram9 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setAVoltage(ram9);

        int ram10 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setBVoltage(ram10);

        int ram11 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setCVoltage(ram11);

        int ram12 = CommonUtil.getShort(payload, index);
        index += 2;
        double aCurr = ram12 * 0.01;
        message.setACurrent(aCurr);

        int ram13 = CommonUtil.getShort(payload, index);
        index += 2;
        double bCurr = ram13 * 0.01;
        message.setBCurrent(bCurr);

        int ram14 = CommonUtil.getShort(payload, index);
        index += 2;
        double cCurr = ram14 * 0.01;
        message.setCCurrent(cCurr);

        int ram15 = CommonUtil.getShort(payload, index);
        index += 2;
        double nCurr = ram15 * ram15;
        message.setNCurrent(nCurr);

        int ram16 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setAPower(ram16);

        int ram17 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setBPower(ram17);

        int ram18 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setCPower(ram18);

        int ram19 = CommonUtil.getShort(payload, index);
        index += 2;
        BoxAlarmType aAlarmType = BoxAlarmType.getByCode(ram19);
        message.setAAlarmType(aAlarmType);

        int ram20 = CommonUtil.getShort(payload, index);
        index += 2;
        BoxAlarmType bAlarmType = BoxAlarmType.getByCode(ram20);
        message.setBAlarmType(bAlarmType);

        int ram21 = CommonUtil.getShort(payload, index);
        index += 2;
        BoxAlarmType cAlarmType = BoxAlarmType.getByCode(ram21);
        message.setCAlarmType(cAlarmType);

        int ram22 = CommonUtil.getShort(payload, index);
        index += 2;

        int ram23 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setAPowerFactor(ram23 == 32767 ? 1 : 0);

        int ram24 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setBPowerFactor(ram24 == 32767 ? 1 : 0);

        int ram25 = CommonUtil.getShort(payload, index);
        index += 2;
        message.setCPowerFactor(ram25 == 32767 ? 1 : 0);

        int ram3A = CommonUtil.getShort(payload, index);
        index += 2;
        double aTemp = ram3A * 0.1;
        message.setATemp(aTemp);

        int ram3F = CommonUtil.getShort(payload, index);
        index += 2;
        double bTemp = ram3F * 0.1;
        message.setBTemp(bTemp);

        int ram44 = CommonUtil.getShort(payload, index);
        index += 2;
        double cTemp = ram44 * 0.1;
        message.setCTemp(cTemp);

        int end = CommonUtil.getShort(payload, index);
        index += 2;
        if(end != 8198)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_END_INCORRECT);

        logger.info("实时数据处理============>解析实时数据end!!!!index应该等于"+index/2+"个字节");
        return message;
    }

}
