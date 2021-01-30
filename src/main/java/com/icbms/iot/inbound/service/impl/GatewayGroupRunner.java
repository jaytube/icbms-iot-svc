package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.common.component.GatewayKeeper;
import com.icbms.iot.dto.GatewayDto;
import com.icbms.iot.dto.GatewayGroupDto;
import com.icbms.iot.inbound.service.GatewayRunner;
import com.icbms.iot.rest.LoRaCommandService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.icbms.iot.constant.IotConstant.MAX_WAITING_TIME;

@Service("groupRunner")
public class GatewayGroupRunner implements GatewayRunner {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private GatewayKeeper gatewayKeeper;


    public void run(int groupId) {
        GatewayGroupDto gatewayGroup = gatewayKeeper.getByGroupId(groupId);
        if(gatewayGroup == null)
            return;

        logger.info("停止所有网关轮询 ...");
        gatewayGroup.getGateways().stream().forEach(e -> {
            loRaCommandService.stopRoundRobin(e.getIp());
        });
        logger.info("线程:" + Thread.currentThread().getName()+" 开启网关组轮询...");
        while(!gatewayGroup.isStopped()) {
            Set<GatewayDto> gateways = gatewayGroup.getGateways();
            if(CollectionUtils.isNotEmpty(gateways)) {
                for (GatewayDto gateway : gateways) {
                    Integer gatewayId = gateway.getId();
                    String gatewayIp = gateway.getIp();
                    CommonResponse resp = loRaCommandService.startRoundRobin(gatewayIp);
                    gateway.setFinished(false);
                    long start = System.currentTimeMillis();
                    logger.info("开启轮询网关ID: " + gatewayId + ", IP: " + gatewayIp + ", 响应：" + resp.getData());
                    long timeCost = 0;
                    while (!gatewayGroup.isStopped() && !gateway.isFinished() && timeCost < MAX_WAITING_TIME) {
                        timeCost = (System.currentTimeMillis() - start) / 1000;
                        continue;
                    }
                    logger.info("网关ID: " + gatewayId + ", IP: " + gatewayIp + " 单次轮询花费: " + timeCost + " seconds.");
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            gatewayGroup = gatewayKeeper.getByGroupId(groupId);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void stop(int groupId) {
        GatewayGroupDto gatewayGroup = gatewayKeeper.getByGroupId(groupId);
        if(gatewayGroup != null)
            gatewayGroup.setStopped(true);
    }
}
