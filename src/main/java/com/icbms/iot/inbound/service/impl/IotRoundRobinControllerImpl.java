package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.inbound.service.IotRoundRobinController;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.MqttEnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IotRoundRobinControllerImpl implements IotRoundRobinController {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private MqttEnvUtil mqttEnvUtil;

    private static final int DEVICE_COUNT = 10;

    private static final int MAX_TIME = 30;

    private Map<String, List<String>> gateWayDeviceMap = new HashMap<>();

    @PostConstruct
    public void initGateWays() {
        //TODO get devices from db;
        List<String> loraIds = Arrays.asList("3932353052376d03", "3932353073378903", "3932353069378e03", "393235306d378d03", "3932353060378d03",
                "393235306a378a03", "3932353053378d03", "3932353078378e03");
        gateWayDeviceMap.put("10.0.1.70", loraIds);
    }

    @Override
    @Async
    public void roundRobinControl() {
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
                mqttEnvUtil.reset();
                String gatewayIp = e.getKey();
                String gatewayId = "";
                //List<String> loraIds = e.getValue();
                mqttEnvUtil.setCurrentGatewayId(gatewayId);
                CommonResponse resp = loRaCommandService.startRoundRobin();
                long start = System.currentTimeMillis();
                logger.info("开启轮询网关" + gatewayIp + ", 响应：" + resp.getData());
                long timeCost = 0;
                while(!mqttEnvUtil.isSingleGatewayStopped() && timeCost < MAX_TIME) {
                    timeCost = (System.currentTimeMillis() - start) / 1000;
                    continue;
                }
                //mqttEnvUtil.setMqttSwitchOff(true);
                //CommonResponse resp2 = loRaCommandService.stopRoundRobin();
                /*List<String> processedDeviceList = mqttEnvUtil.getProcessedDeviceList();
                ArrayList<String> newList = new ArrayList<>(loraIds);
                newList.removeAll(processedDeviceList);
                logger.info("未处理的设备: " + newList.stream().collect(Collectors.joining(", ")));
                if(CollectionUtils.isNotEmpty(newList)) {
                    newList.stream().forEach(c -> {
                        loRaCommandService.executeCmd(LoRaCommand.QUERY_CMD, c);
                        logger.info("单个设备查询实时空开数据, id: " + c);
                    });
                }*/
                //logger.info("停止轮询网关" + gatewayIp + ", 响应: " + resp2.getData());
                logger.info("网关" + gatewayIp + " 轮询花费: " + timeCost + " seconds.");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
