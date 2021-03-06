package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RealtimeMessage;
import com.icbms.iot.enums.AlarmType;
import com.icbms.iot.enums.BoxAlarmType;
import com.icbms.iot.inbound.service.RealtimeMessageParser;
import com.icbms.iot.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RealtimeMessageParserImpl implements RealtimeMessageParser {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static HashMap<String, AlarmType> alarmTypesMap = new HashMap<>();
    private static HashMap<String, BoxAlarmType> boxAlarmTypesMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Arrays.stream(AlarmType.values()).forEach(a -> {
            alarmTypesMap.put(Integer.toString(a.getCode()), a);
        });
        Arrays.stream(BoxAlarmType.values()).forEach(a -> {
            boxAlarmTypesMap.put(Integer.toString(a.getCode()), a);
        });
    }

    @Override
    public RealtimeMessage parseMessage(byte[] payload) {
        logger.debug("实时数据处理============>解析实时数据start!!!!");
        logger.info("]收到的数据域=============>[" + CommonUtil.bytesToHex(payload) + "]");
        int index = 0;
        Integer header = CommonUtil.getInt(payload, index);
        index += 4;

        RealtimeMessage message = new RealtimeMessage();
        int boxNumber = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("Box No: " + boxNumber);
        message.setBoxNo(boxNumber);

        int ram1 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("电路电压：" + ram1);
        message.setCircuitVoltage(ram1);

        int ram2 = CommonUtil.getShort(payload, index);
        index += 2;
        double currentLeak = ram2 * 0.0001;
        String currentLeakStr = BigDecimal.valueOf(currentLeak).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("泄漏电流：" + currentLeakStr);
        message.setCurrentLeak(currentLeakStr);

        int ram3 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("电路功率：" + ram3);
        message.setCircuitPower(ram3);

        int ram4 = CommonUtil.getShort(payload, index);
        index += 2;
        double modTemp = ram4 * 0.1;
        logger.debug("模块温度：" + modTemp);
        message.setModTemp(modTemp);

        int ram5 = CommonUtil.getShort(payload, index);
        index += 2;
        double circuitCurrent = ram5 * 0.01;
        String circuitCurrentStr = BigDecimal.valueOf(circuitCurrent).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("电路电流：" + circuitCurrentStr);
        message.setCircuitCurrent(circuitCurrentStr);

        short ram6 = CommonUtil.getShort(payload, index);
        index += 2;
        List<AlarmType> alarmTypes = parseAlarmTypes(ram6);
        logger.debug("告警类别：" + alarmTypes.stream().map(AlarmType::getAlarmContent).collect(Collectors.joining(", ")));
        message.setAlarmTypes(alarmTypes);

        /*if(CollectionUtils.isNotEmpty(alarmTypes))
            message.setDataType(DataType.ALARM_DATA);*/

        //long ele = CommonUtil.getLong(payload, index);
        byte[] elecByteArr = new byte[4];
        System.arraycopy(payload, index, elecByteArr, 2, 2);
        index += 2;
        System.arraycopy(payload, index, elecByteArr, 0, 2);
        index += 2;
        int val = CommonUtil.bytesToInt(elecByteArr);
        double electric = val * 0.001;
        //index += 4;
        String electricStr = BigDecimal.valueOf(electric).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("电量：" + electricStr);
        message.setElectric(electricStr);

        int ram9 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("A箱电压：" + ram9);
        message.setAVoltage(ram9);

        int ram10 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("B箱电压：" + ram10);
        message.setBVoltage(ram10);

        int ram11 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("C箱电压：" + ram11);
        message.setCVoltage(ram11);

        int ram12 = CommonUtil.getShort(payload, index);
        index += 2;
        double aCurr = ram12 * 0.01;
        String aCurrStr = BigDecimal.valueOf(aCurr).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("A箱电流：" + aCurrStr);
        message.setACurrent(aCurrStr);

        int ram13 = CommonUtil.getShort(payload, index);
        index += 2;
        double bCurr = ram13 * 0.01;
        String bCurrStr = BigDecimal.valueOf(bCurr).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("B箱电流：" + bCurrStr);
        message.setBCurrent(bCurrStr);

        int ram14 = CommonUtil.getShort(payload, index);
        index += 2;
        double cCurr = ram14 * 0.01;
        String cCurrStr = BigDecimal.valueOf(cCurr).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("C箱电流：" + cCurrStr);
        message.setCCurrent(cCurrStr);

        int ram15 = CommonUtil.getShort(payload, index);
        index += 2;
        double nCurr = ram15 * 0.01;
        String nCurrStr = BigDecimal.valueOf(nCurr).setScale(2, RoundingMode.HALF_UP).toString();
        logger.debug("N箱电流：" + nCurrStr);
        message.setNCurrent(nCurrStr);

        int ram16 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("A箱功率：" + ram16);
        message.setAPower(ram16);

        int ram17 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("B箱功率：" + ram17);
        message.setBPower(ram17);

        int ram18 = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("C箱功率：" + ram18);
        message.setCPower(ram18);

        short ram19 = CommonUtil.getShort(payload, index);
        index += 2;
        List<BoxAlarmType> aAlarmTypes = parseBoxAlarmTypes(ram19);
        logger.debug("A箱告警类型：" + aAlarmTypes.stream().map(BoxAlarmType::toString).collect(Collectors.joining(", ")));
        message.setAAlarmTypes(aAlarmTypes);

        short ram20 = CommonUtil.getShort(payload, index);
        index += 2;
        List<BoxAlarmType> bAlarmTypes = parseBoxAlarmTypes(ram20);
        logger.debug("B箱告警类型：" + bAlarmTypes.stream().map(BoxAlarmType::toString).collect(Collectors.joining(", ")));
        message.setBAlarmTypes(bAlarmTypes);

        short ram21 = CommonUtil.getShort(payload, index);
        index += 2;
        List<BoxAlarmType> cAlarmTypes = parseBoxAlarmTypes(ram21);
        logger.debug("C箱告警类型：" + cAlarmTypes.stream().map(BoxAlarmType::toString).collect(Collectors.joining(", ")));
        message.setCAlarmTypes(cAlarmTypes);

        byte ram22 = CommonUtil.getByte(payload, index);
        index += 1;
        CommonUtil.getByte(payload, index);
        index += 1;
        if(ram22 == 90)
            message.setSwitchFlag(true);
        else if(ram22 == -91)
            message.setSwitchFlag(false);
        logger.debug("开关分合：" + Objects.toString(message.getSwitchFlag(), null));

        int ram23 = CommonUtil.getShort(payload, index);
        index += 2;
        int aFactor = ram23 == 32767 ? 1 : 0;
        logger.debug("A相功率因数：" + aFactor);
        message.setAPowerFactor(aFactor);

        int ram24 = CommonUtil.getShort(payload, index);
        index += 2;
        int bFactor = ram24 == 32767 ? 1 : 0;
        logger.debug("B相功率因数：" + bFactor);
        message.setBPowerFactor(bFactor);

        int ram25 = CommonUtil.getShort(payload, index);
        index += 2;
        int cFactor = ram25 == 32767 ? 1 : 0;
        logger.debug("C相功率因数：" + cFactor);
        message.setCPowerFactor(cFactor);

        int ram3A = CommonUtil.getShort(payload, index);
        index += 2;
        double aTemp = ram3A * 0.1;
        logger.debug("A相温度：" + aTemp);
        message.setATemp(aTemp);

        int ram3F = CommonUtil.getShort(payload, index);
        index += 2;
        double bTemp = ram3F * 0.1;
        logger.debug("B相温度：" + bTemp);
        message.setBTemp(bTemp);

        int ram44 = CommonUtil.getShort(payload, index);
        index += 2;
        double cTemp = ram44 * 0.1;
        logger.debug("C相温度：" + cTemp);
        message.setCTemp(cTemp);

        int end = CommonUtil.getShort(payload, index);
        index += 2;
        logger.debug("结束符：" + end);
        /*if(end != 8198)
            throw new IotException(ErrorCodeEnum.IOT_MESSAGE_END_INCORRECT);*/

        logger.debug("实时数据处理============>解析实时数据end!!!!一共"+index+"个字节");
        return message;
    }

    private List<AlarmType> parseAlarmTypes(short alarmValue) {
        //return Arrays.stream(AlarmType.values()).collect(Collectors.toList());
        String alarmStr = String.format("%16s", Integer.toBinaryString(alarmValue & 0xFFFF))
                .replace(" ", "0");
        List<AlarmType> result = new ArrayList<>();
        if(StringUtils.isBlank(alarmStr))
            return result;

        char[] chars = alarmStr.toCharArray();
        for(int i=0; i<chars.length; i++) {
            String c = chars[i] + "";
            if("1".equals(c)) {
                AlarmType type = alarmTypesMap.getOrDefault(i + "", null);
                if(type != null)
                    result.add(type);
            }
        }

        return result;
    }

    public List<BoxAlarmType> parseBoxAlarmTypes(short boxAlarmValue) {
        String alarmStr = Integer.toBinaryString(boxAlarmValue);
        List<BoxAlarmType> result = new ArrayList<>();
        if(StringUtils.isBlank(alarmStr))
            return result;

        char[] chars = alarmStr.toCharArray();
        for(int i=0; i<chars.length; i++) {
            String c = chars[i] + "";
            if("1".equals(c)) {
                BoxAlarmType type = boxAlarmTypesMap.getOrDefault(i + "", null);
                if(type != null)
                    result.add(type);
            }
        }

        return result;
    }
}
