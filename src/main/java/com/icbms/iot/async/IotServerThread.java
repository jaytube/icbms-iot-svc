package com.icbms.iot.async;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.MqttEnvUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public class IotServerThread implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private LoRaCommandService loRaCommandService;

    private static final int DEVICE_COUNT = 10;

    private static final int MAX_TIME = 30;

    private Map<String, List<String>> gateWayDeviceMap = new HashMap<>();

    @PostConstruct
    public void initGateWays() {
        List<String> loraIds = Arrays.asList("3932353052376d03", "3932353073378903", "3932353069378e03", "393235306d378d03", "3932353060378d03",
                "393235306a378a03", "3932353053378d03", "3932353078378e03");
        gateWayDeviceMap.put("10.0.1.70", loraIds);
    }

    @Autowired
    private MqttEnvUtil mqttEnvUtil;

    public void run() {
        logger.info("Iot 服务初始化 ...");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("停止所有网关轮询 ...");
        gateWayDeviceMap.entrySet().stream().forEach(e -> {
            String gateway = e.getKey();
            loRaCommandService.stopRoundRobin();
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true) {
            gateWayDeviceMap.entrySet().stream().forEach(e -> {
                String gatewayIp = e.getKey();
                List<String> loraIds = e.getValue();
                mqttEnvUtil.reset();
                CommonResponse resp1 = loRaCommandService.startRoundRobin();
                long start = System.currentTimeMillis();
                logger.info("开启轮询网关" + gatewayIp + ", 响应：" + resp1.getData());
                long timeCost = 0;
                while(mqttEnvUtil.getMessageProcessed() < DEVICE_COUNT && timeCost < MAX_TIME) {
                    timeCost = (System.currentTimeMillis() - start) / 1000;
                    continue;
                }
                mqttEnvUtil.setMqttSwitchOff(true);
                CommonResponse resp2 = loRaCommandService.stopRoundRobin();
                List<String> processedDeviceList = mqttEnvUtil.getProcessedDeviceList();
                ArrayList<String> newList = new ArrayList<>(loraIds);
                newList.removeAll(processedDeviceList);
                logger.info("未处理的设备: " + newList.stream().collect(Collectors.joining(", ")));
                if(CollectionUtils.isNotEmpty(newList)) {
                    newList.stream().forEach(c -> {
                        loRaCommandService.executeCmd(LoRaCommand.QUERY_CMD, c);
                        logger.info("单个设备查询实时空开数据, id: " + c);
                    });
                }
                logger.info("停止轮询网关" + gatewayIp + ", 响应: " + resp2.getData());
                logger.info("网关" + gatewayIp + " 轮询花费: " + timeCost + " seconds.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
