package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.inbound.component.InboundStopMsgQueue;
import com.icbms.iot.inbound.service.IotRoundRobinController;
import com.icbms.iot.mapper.GatewayInfoMapper;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.MqttEnvUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IotRoundRobinControllerImpl implements IotRoundRobinController {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private MqttEnvUtil mqttEnvUtil;

    @Autowired
    private InboundStopMsgQueue inboundStopMsgQueue;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    private static final int MAX_TIME = 30;

    private Map<String, GatewayInfo> gateWayDeviceMap = new HashMap<>();

    @PostConstruct
    public void initGateWays() {
        //TODO get devices from db;
        List<String> loraIds = Arrays.asList("3932353052376d03", "3932353073378903", "3932353069378e03", "393235306d378d03", "3932353060378d03",
                "393235306a378a03", "3932353053378d03", "3932353078378e03");
        List<GatewayInfo> all = gatewayInfoMapper.findAll();
        if (CollectionUtils.isNotEmpty(all)) {
            all.forEach(gatewayInfo -> gateWayDeviceMap.put(Objects.toString(gatewayInfo.getGatewayId()), gatewayInfo));
        }
    }

    @Override
    @Async
    public void roundRobinControl() {
        logger.info("Iot 网关控制服务初始化 ...");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("停止所有网关轮询 ...");
        gateWayDeviceMap.entrySet().stream().forEach(e -> {
            String gatewayIp = e.getValue().getIpAddress();
            loRaCommandService.stopRoundRobin(gatewayIp);
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            gateWayDeviceMap.entrySet().stream().forEach(e -> {
                mqttEnvUtil.reset();
                String gatewayIp = e.getValue().getIpAddress();
                String gatewayId = e.getKey();
                //List<String> loraIds = e.getValue();
                mqttEnvUtil.setCurrentGatewayId(gatewayId);
                CommonResponse resp = loRaCommandService.startRoundRobin(gatewayIp);
                long start = System.currentTimeMillis();
                logger.info("开启轮询网关" + gatewayIp + ", 响应：" + resp.getData());
                long timeCost = 0;
                while (!mqttEnvUtil.isSingleGatewayStopped() && timeCost < MAX_TIME) {
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
                logger.info("总共轮询了{}个设备", mqttEnvUtil.getProcessedDeviceList().size());
                logger.info("设备ID:" + mqttEnvUtil.getProcessedDeviceList().stream().collect(Collectors.joining(", ")));
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
